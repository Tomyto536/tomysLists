package tomyto.tomyslists;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class FileUtils {

    public static Map<String, Integer> parseMaterialList(Path filePath) {
        Map<String, Integer> materials = new LinkedHashMap<>();

        try {
            for (String line : Files.readAllLines(filePath)) {
                // Skip headers and separator lines (start with + or | Item)
                if (line.startsWith("+") || line.contains("Item") || line.isBlank()) {
                    continue;
                }

                // Line containing actual information
                String[] parts = line.split("\\|");
                if (parts.length < 3) continue;

                String name = parts[1].trim();
                String totalStr = parts[2].trim();

                if (name.isBlank() || totalStr.isBlank()) continue;

                int total = Integer.parseInt(totalStr);
                materials.put(name, total);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return materials;
    }
}
