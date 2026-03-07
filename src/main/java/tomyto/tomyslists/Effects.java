package tomyto.tomyslists;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Surface;

import java.util.List;

public class Effects {

    private static int selectedRow = -1;

    public static void highlight(List<FlowLayout> rows, int index) {
        // Reset all rows first
        for (FlowLayout row : rows) {
            row.surface(Surface.DARK_PANEL);
        }

        // Highlight the target row
        if (index >= 0 && index < rows.size()) {
            rows.get(index).surface(Surface.outline(0xFFFFFF00));
        }
    }

    public static void select(List<FlowLayout> rows, int index) {
        // Deselect previous
        if (selectedRow >= 0 && selectedRow < rows.size()) {
            rows.get(selectedRow).surface(Surface.DARK_PANEL);
        }

        // Select new row
        if (index >= 0 && index < rows.size()) {
            selectedRow = index;
            rows.get(index).surface(Surface.flat(0xFF2A5C8A)); // blue background
        }
    }

    public static void clearAll(List<FlowLayout> rows) {
        for (FlowLayout row : rows) {
            row.surface(Surface.DARK_PANEL);
        }
    }

    public static int getSelectedRow() {
        return selectedRow;
    }

}