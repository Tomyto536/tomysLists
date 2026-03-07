package tomyto.tomyslists;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class FileUtils {

    public static Map<String, Integer> loadMaterialList(Path filePath) {
        Map<String, Integer> materials = new LinkedHashMap<>();

        try {
            String firstLine = Files.readAllLines(filePath).stream()
                    .filter(l -> !l.isBlank())
                    .findFirst()
                    .orElse("");

            // If first non-blank line starts with +, it's the litematica table format
            if (firstLine.startsWith("+")) {
                materials = parseLitematicaFormat(filePath);
                if (!materials.isEmpty()) {
                    saveSimpleFormat(filePath, materials);
                }
            } else {
                materials = parseSimpleFormat(filePath);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return materials;
    }

    private static Map<String, Integer> parseLitematicaFormat(Path filePath) {
        Map<String, Integer> materials = new LinkedHashMap<>();

        try {
            for (String line : Files.readAllLines(filePath)) {
                if (line.startsWith("+") || line.contains("Item") || line.isBlank()) continue;

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

    private static Map<String, Integer> parseSimpleFormat(Path filePath) {
        Map<String, Integer> materials = new LinkedHashMap<>();

        try {
            for (String line : Files.readAllLines(filePath)) {
                if (line.isBlank()) continue;

                String[] parts = line.split(",");
                if (parts.length < 2) continue;

                String name = parts[0].trim();
                int total = Integer.parseInt(parts[1].trim());
                materials.put(name, total);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return materials;
    }

    public static void saveSimpleFormat(Path filePath, Map<String, Integer> materials) {
        StringBuilder sb = new StringBuilder();
        materials.forEach((name, total) -> sb.append(name).append(",").append(total).append("\n"));

        try {
            Files.writeString(filePath, sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}