package tomyto.tomyslists;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class OverlayState {

    private OverlayState() {}

    private static boolean visible = false;

    /**
     * Each entry is a String[4]:
     *   [0] display name
     *   [1] minecraft item id (e.g. "minecraft:grass_block")
     *   [2] formatted count string (e.g. "2 Stacks + 28 Items")
     *   [3] raw diff as a string (e.g. "-132", "+32", or "0")
     */
    private static final List<String[]> items = new ArrayList<>();

    /** Stores the full ordered list of row names as last set by ListMainScreen */
    private static List<String> cachedRowNames = new ArrayList<>();

    /** The currently selected index in the full list */
    private static int cachedSelectedIndex = -1;

    /** Total amounts needed, keyed by display name */
    private static final Map<String, Integer> totalCache = new LinkedHashMap<>();

    public static boolean isVisible() { return visible; }
    public static void setVisible(boolean v) { visible = v; }
    public static void toggle() { visible = !visible; }
    public static List<String[]> getItems() { return items; }
    public static Map<String, Integer> getTotalCache() { return totalCache; }

    public static void setTotalCache(Map<String, Integer> totals) {
        totalCache.clear();
        totalCache.putAll(totals);
    }

    /**
     * Called by ListMainScreen after any load/change to keep the overlay in sync.
     * Pushes up to 4 entries starting at selectedIndex.
     */
    public static void update(List<String> rowNames,
                              int selectedIndex,
                              Function<String, Integer> totalLookup,
                              Function<String, Integer> playerCountLookup) {
        cachedRowNames = new ArrayList<>(rowNames);
        cachedSelectedIndex = selectedIndex;

        items.clear();
        if (rowNames.isEmpty() || selectedIndex < 0) return;

        int start = Math.max(0, selectedIndex);
        int end   = Math.min(rowNames.size(), start + 4);

        for (int i = start; i < end; i++) {
            String name = rowNames.get(i);
            int total   = totalLookup.apply(name);
            int have    = playerCountLookup.apply(name);
            int diff    = have - total;
            String itemId = "minecraft:" + name.toLowerCase().replace(" ", "_");
            items.add(new String[]{ name, itemId, FileUtils.formatAmount(total), String.valueOf(diff) });
        }
    }

    /**
     * Refreshes only the player-count-dependent fields (diff) using cached row names and totals.
     * Called every tick so the overlay stays current without opening ListMainScreen.
     */
    public static void refreshFromInventory(Function<String, Integer> playerCountLookup) {
        items.clear();
        if (cachedRowNames.isEmpty() || cachedSelectedIndex < 0) return;

        int start = Math.max(0, cachedSelectedIndex);
        int end   = Math.min(cachedRowNames.size(), start + 4);

        for (int i = start; i < end; i++) {
            String name  = cachedRowNames.get(i);
            int total    = totalCache.getOrDefault(name, 0);
            int have     = playerCountLookup.apply(name);
            int diff     = have - total;
            String itemId = "minecraft:" + name.toLowerCase().replace(" ", "_");
            items.add(new String[]{ name, itemId, FileUtils.formatAmount(total), String.valueOf(diff) });
        }
    }
}