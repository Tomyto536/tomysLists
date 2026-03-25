package tomyto.tomyslists;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileUtils {

    public static final String SELECTED_MARKER = "*";
    public static final String CHECKEDOFF_MARKER = "@";

    public static Map<String, Integer> loadMaterialList(Path filePath) {

        Path configFile = Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config").resolve("litematica")
                .resolve("tomyslistconfig.txt");
        FileUtils.writeDefaultGroupings(configFile);

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
                    saveSimpleFormat(filePath, materials, -1);
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
                //if (line.startsWith(CHECKEDOFF_MARKER)) continue;

                // Strip marker if present
                if (line.startsWith(SELECTED_MARKER)) {
                    line = line.substring(1);
                }

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

    public static void saveSimpleFormat(Path filePath, Map<String, Integer> materials, int selectedIndex) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, Integer> entry : materials.entrySet()) {
            if (i == selectedIndex) {
                sb.append(SELECTED_MARKER);
            }
            sb.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
            i++;
        }

        try {
            Files.writeString(filePath, sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String formatAmount(int total) {
        int shulkers = total / (27 * 64);
        int remainder = total % (27 * 64);
        int stacks = remainder / 64;
        int items = remainder % 64;

        StringBuilder sb = new StringBuilder();

        if (shulkers > 0) sb.append(shulkers).append(" Shulker").append(shulkers > 1 ? "s" : "");
        if (stacks > 0) {
            if (!sb.isEmpty()) sb.append(" + ");
            sb.append(stacks).append(" Stack").append(stacks > 1 ? "s" : "");
        }
        if (items > 0) {
            if (!sb.isEmpty()) sb.append(" + ");
            sb.append(items).append(" Item").append(items > 1 ? "s" : "");
        }

        // Edge case: total is 0
        if (sb.isEmpty()) sb.append("0 Items");

        return sb.toString();
    }

    public static int getSelectedIndex(Path filePath) {
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith(SELECTED_MARKER)) {
                    return i;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void writeDefaultGroupings(Path configFile) {
        try {
            if (!Files.exists(configFile)) return;
            List<String> lines = new ArrayList<>(Files.readAllLines(configFile));

            // Only write defaults if there are no groupings yet (only line 1 or empty)
            if (lines.size() > 1) return;

            List<String> defaults = List.of(
                    // Wood types
                    "oak|leave,sapling, pale, dark",
                    "spruce|leave,sapling",
                    "birch|leave,sapling",
                    "jungle|leave,sapling",
                    "acacia|leave,sapling",
                    "dark oak|leave,sapling",
                    "mangrove|leave,propagule",
                    "cherry|leave,sapling",
                    "bamboo|leave,sapling",
                    "crimson|fungus, nylium, roots",
                    "warped|fungus, roots, nylium",
                    "pale|leave,sapling",

                    // Leaves and saplings
                    "leave|",
                    "sapling|",

                    // Stone variants
                    "stone|redstone, sand, glowstone, end, black, cutter, lode, dripstone, cobble",
                    "cobblestone|",
                    "andesite|",
                    "diorite|",
                    "granite|",
                    "deepslate|",
                    "blackstone|",
                    "basalt|",
                    "tuff|",
                    "dripstone|",
                    "mud",
                    "prismarine",
                    "resin",
                    "quartz",

                    // Bricks
                    "brick|stone, deepslate, mud, nether, tuff, prismarine, resin, quartz",
                    "nether brick|",

                    // Sandstone
                    "sand|soul, stone",
                    "sandstone|",

                    // Concrete
                    "concrete|",

                    // Terracotta
                    "terracotta|",

                    // Glass
                    "glass|",

                    // Wool and carpet
                    "wool|",
                    "carpet|moss",

                    // Copper
                    "copper|",

                    //Misc
                    "lantern",
                    "candle"
            );

            lines.addAll(defaults);
            Files.write(configFile, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}