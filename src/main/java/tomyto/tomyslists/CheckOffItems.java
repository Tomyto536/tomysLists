package tomyto.tomyslists;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CheckOffItems {

    public static final String CHECKEDOFF_MARKER = "@";

    public static void checkOff(Path filePath, String itemName) {
        try {
            List<String> lines = Files.readAllLines(filePath);
            List<String> active = new ArrayList<>();
            List<String> checkedOff = new ArrayList<>();

            int activeIndex = 0;
            for (String line : lines) {
                if (line.isBlank()) continue;
                if (line.startsWith(CHECKEDOFF_MARKER)) {
                    checkedOff.add(line); // already checked off, keep at bottom
                } else if (line.replace(FileUtils.SELECTED_MARKER, "").split(",")[0].trim().equals(itemName)) {
                    // Store the original index with the marker
                    String stripped = line.replace(FileUtils.SELECTED_MARKER, "");
                    checkedOff.add(CHECKEDOFF_MARKER + activeIndex + "," + stripped);
                } else {
                    active.add(line);
                    activeIndex++;
                }
            }

            List<String> result = new ArrayList<>();
            result.addAll(active);
            result.addAll(checkedOff);
            StringBuilder sb = new StringBuilder();
            for (String line : result) sb.append(line).append("\n");
            Files.writeString(filePath, sb.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String bringBack(Path filePath) {
        try {
            List<String> lines = Files.readAllLines(filePath);
            List<String> active = new ArrayList<>();
            List<String> checkedOff = new ArrayList<>();

            for (String line : lines) {
                if (line.isBlank()) continue;
                if (line.startsWith(CHECKEDOFF_MARKER)) {
                    checkedOff.add(line);
                } else {
                    active.add(line);
                }
            }

            if (checkedOff.isEmpty()) return null;

            // Get the first checked off item
            String lastCheckedOff = checkedOff.remove(0);
            // Format is @index,name,count — strip the marker first
            String withoutMarker = lastCheckedOff.substring(1);
            // Extract the original index
            int commaPos = withoutMarker.indexOf(",");
            int originalIndex = Integer.parseInt(withoutMarker.substring(0, commaPos));
            // The rest is the actual line content
            String restored = withoutMarker.substring(commaPos + 1);

            // Insert back at original index, clamped to list size
            int insertIndex = Math.min(originalIndex, active.size());
            active.add(insertIndex, restored);

            // Write back
            StringBuilder sb = new StringBuilder();
            for (String line : active) sb.append(line).append("\n");
            for (String line : checkedOff) sb.append(line).append("\n");
            Files.writeString(filePath, sb.toString());

            return restored.replace(FileUtils.SELECTED_MARKER, "").split(",")[0].trim();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Returns list of checked off items as "name,count" strings without the marker
    public static List<String> getCheckedOffItems(Path filePath) {
        List<String> result = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(filePath)) {
                if (line.startsWith(CHECKEDOFF_MARKER)) {
                    // Strip marker and index prefix: @index,name,count -> name,count
                    String withoutMarker = line.substring(1);
                    int commaPos = withoutMarker.indexOf(",");
                    result.add(withoutMarker.substring(commaPos + 1));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Brings back a specific item by name
    public static String bringBackSpecific(Path filePath, String itemName) {
        try {
            List<String> lines = Files.readAllLines(filePath);
            List<String> active = new ArrayList<>();
            List<String> checkedOff = new ArrayList<>();

            for (String line : lines) {
                if (line.isBlank()) continue;
                if (line.startsWith(CHECKEDOFF_MARKER)) {
                    checkedOff.add(line);
                } else {
                    active.add(line);
                }
            }

            String targetLine = null;
            for (String line : checkedOff) {
                String withoutMarker = line.substring(1);
                int commaPos = withoutMarker.indexOf(",");
                String name = withoutMarker.substring(commaPos + 1).split(",")[0].trim();
                if (name.equals(itemName)) {
                    targetLine = line;
                    break;
                }
            }

            if (targetLine == null) return null;
            checkedOff.remove(targetLine);

            String withoutMarker = targetLine.substring(1);
            int commaPos = withoutMarker.indexOf(",");
            int originalIndex = Integer.parseInt(withoutMarker.substring(0, commaPos));
            String restored = withoutMarker.substring(commaPos + 1);

            int insertIndex = Math.min(originalIndex, active.size());
            active.add(insertIndex, restored);

            StringBuilder sb = new StringBuilder();
            for (String line : active) sb.append(line).append("\n");
            for (String line : checkedOff) sb.append(line).append("\n");
            Files.writeString(filePath, sb.toString());

            return restored.split(",")[0].trim();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}