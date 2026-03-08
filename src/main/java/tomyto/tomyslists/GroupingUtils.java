package tomyto.tomyslists;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GroupingUtils {

    // Returns the grouping that matches the given item name, or null
    public static Map.Entry<String, List<String>> findGroupingForItem(String itemName, Map<String, List<String>> groupings) {
        for (Map.Entry<String, List<String>> entry : groupings.entrySet()) {
            String grouping = entry.getKey();
            List<String> ignored = entry.getValue();

            if (itemName.toLowerCase().contains(grouping.toLowerCase())) {
                boolean isIgnored = ignored.stream()
                        .anyMatch(term -> itemName.toLowerCase().contains(term.toLowerCase()));
                if (!isIgnored) return entry;
            }
        }
        return null;
    }

    // Moves all items matching the same grouping as the selected item to right after it
    public static void groupItemsAfterSelected(Path materialFile, Path configFile, String selectedItemName) {
        try {
            // Load groupings
            List<String> configLines = Files.readAllLines(configFile);
            Map<String, List<String>> groupings = new LinkedHashMap<>();
            for (int i = 1; i < configLines.size(); i++) {
                String line = configLines.get(i).trim();
                if (line.isBlank()) continue;
                String[] parts = line.split("\\|");
                String grouping = parts[0].trim();
                List<String> ignored = new ArrayList<>();
                if (parts.length > 1) {
                    for (String term : parts[1].split(",")) ignored.add(term.trim());
                }
                groupings.put(grouping, ignored);
            }

            // Find which grouping the selected item belongs to
            Map.Entry<String, List<String>> matchedGrouping = findGroupingForItem(selectedItemName, groupings);
            if (matchedGrouping == null) return;

            // Read material file lines
            List<String> lines = Files.readAllLines(materialFile);
            List<String> before = new ArrayList<>();
            List<String> matching = new ArrayList<>();
            List<String> after = new ArrayList<>();
            String selectedLine = null;
            boolean pastSelected = false;

            for (String line : lines) {
                if (line.isBlank()) continue;

                String cleanLine = line.replace(FileUtils.SELECTED_MARKER, "")
                        .replace(CheckOffItems.CHECKEDOFF_MARKER, "");
                String name = cleanLine.split(",")[0].trim();

                if (name.equals(selectedItemName)) {
                    selectedLine = line;
                    pastSelected = true;
                    continue;
                }

                // Check if this line matches the grouping
                boolean matches = name.toLowerCase().contains(matchedGrouping.getKey().toLowerCase());
                boolean isIgnored = matchedGrouping.getValue().stream()
                        .anyMatch(term -> name.toLowerCase().contains(term.toLowerCase()));

                if (matches && !isIgnored && !line.startsWith(CheckOffItems.CHECKEDOFF_MARKER)) {
                    matching.add(line);
                } else if (!pastSelected) {
                    before.add(line);
                } else {
                    after.add(line);
                }
            }

            if (selectedLine == null) return;

            // Rebuild: before + selected + matching + after
            StringBuilder sb = new StringBuilder();
            for (String line : before) sb.append(line).append("\n");
            sb.append(selectedLine).append("\n");
            for (String line : matching) sb.append(line).append("\n");
            for (String line : after) sb.append(line).append("\n");


            Files.write(materialFile, sb.toString().getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, List<String>> loadGroupings(Path configFile) {
        Map<String, List<String>> groupings = new LinkedHashMap<>();
        try {
            List<String> lines = Files.readAllLines(configFile);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isBlank()) continue;
                String[] parts = line.split("\\|");
                String grouping = parts[0].trim();
                List<String> ignored = new ArrayList<>();
                if (parts.length > 1) {
                    for (String term : parts[1].split(",")) ignored.add(term.trim());
                }
                groupings.put(grouping, ignored);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return groupings;
    }
}