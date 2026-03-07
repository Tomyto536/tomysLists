package tomyto.tomyslists;

import net.minecraft.server.jsonrpc.methods.ServerStateService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CheckOffItems {

    public static final String CHECKEDOFF_MARKER = "@";

    public static void checkOff(Path filePath, String itemName) {
        try {
            List<String> lines = Files.readAllLines(filePath);
            List<String> active = new ArrayList<>();
            List<String> checkedOff = new ArrayList<>();

            for (String line : lines) {
                if (line.isBlank()) continue;
                if (line.startsWith(CHECKEDOFF_MARKER)) {
                    checkedOff.add(line); // already checked off, keep at bottom
                } else if (line.replace(FileUtils.SELECTED_MARKER, "").split(",")[0].trim().equals(itemName)) {
                    // This is the line to check off
                    String stripped = line.replace(FileUtils.SELECTED_MARKER, "");
                    checkedOff.add(CHECKEDOFF_MARKER + stripped);
                } else {
                    active.add(line);
                }
            }

            System.out.println("Active: " + active);
            System.out.println("CheckedOff: " + checkedOff);

            // Write active lines first, then checked off at bottom
            List<String> result = new ArrayList<>();
            result.addAll(active);
            result.addAll(checkedOff);
            StringBuilder sb = new StringBuilder();
            for (String line : result) {
                sb.append(line).append("\n");
            }
            Files.writeString(filePath, sb.toString());;

            System.out.println(sb.toString());

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

            // Get the last checked off item and strip the marker
            String lastCheckedOff = checkedOff.remove(0);
            String restored = lastCheckedOff.substring(1); // remove the # character

            // Add it to the top of active list
            active.add(0, restored);

            // Write back
            StringBuilder sb = new StringBuilder();
            for (String line : active) sb.append(line).append("\n");
            for (String line : checkedOff) sb.append(line).append("\n");
            Files.write(filePath, sb.toString().getBytes());

            // Return the name so we can add the row back
            return restored.replace(FileUtils.SELECTED_MARKER, "").split(",")[0].trim();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}