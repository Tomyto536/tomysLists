package tomyto.tomyslists;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Surface;

import java.util.List;

/**
 * Shared row visual style constants and helpers.
 * Import this in any screen that renders a scrollable list of rows.
 */
public final class RowStyle {

    private RowStyle() {}

    // Subtle alternating stripe colors so adjacent rows are easy to tell apart.
    public static final int ROW_COLOR_EVEN     = 0xFF2C2C32;
    public static final int ROW_COLOR_ODD      = 0xFF242429;
    // Softer, muted highlight for the selected row.
    public static final int ROW_COLOR_SELECTED = 0xFF3E5878;

    /**
     * Recolors every row based on its position (stripe) and whether it is
     * the selected row (highlight). Call this whenever {@code selectedIndex}
     * changes or rows are added / removed / reordered.
     *
     * @param rows          the ordered list of row containers
     * @param selectedIndex the index of the currently-selected row, or -1
     */
    public static void updateRowAppearances(List<FlowLayout> rows, int selectedIndex) {
        for (int i = 0; i < rows.size(); i++) {
            boolean selected = (i == selectedIndex);
            int color = selected
                    ? ROW_COLOR_SELECTED
                    : (i % 2 == 0 ? ROW_COLOR_EVEN : ROW_COLOR_ODD);
            rows.get(i).surface(Surface.flat(color));
        }
    }
}