package tomyto.tomyslists;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Draws the material-list overlay in the bottom-right corner of the HUD.
 * Renders over regular gameplay and all container screens.
 *
 * Register via: HudRenderCallback.EVENT.register(new ListOverlayRenderer());
 */
public class ListOverlayRenderer implements HudRenderCallback {

    private static final int ROW_HEIGHT   = 20;
    private static final int PADDING      = 6;
    private static final int ICON_SIZE    = 16;
    private static final int BG_COLOR     = 0xB0000000; // semi-transparent black
    private static final int TEXT_WHITE   = 0xFFFFFFFF;
    private static final int TEXT_GREEN   = 0xFF55FF55;
    private static final int TEXT_RED     = 0xFFFF5555;

    @Override
    public void onHudRender(GuiGraphics graphics, net.minecraft.client.DeltaTracker deltaTracker) {
        if (!OverlayState.isVisible()) return;

        List<String[]> items = OverlayState.getItems();
        if (items.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        int screenW  = mc.getWindow().getGuiScaledWidth();
        int screenH  = mc.getWindow().getGuiScaledHeight();

        // Measure the widest count+diff string to size the panel
        int maxTextWidth = 0;
        for (String[] entry : items) {
            int diff = Integer.parseInt(entry[3]);
            String countStr = entry[2] + (diff == 0 ? "" : (diff > 0 ? " (+" : " (") + diff + ")");
            int w = mc.font.width(countStr);
            if (w > maxTextWidth) maxTextWidth = w;
        }

        int panelW = PADDING + ICON_SIZE + PADDING + maxTextWidth + PADDING;
        int panelH = PADDING + items.size() * ROW_HEIGHT + PADDING;
        int panelX = screenW - panelW - 4;
        int panelY = screenH - panelH - 4;

        // Background
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, BG_COLOR);

        // Rows
        for (int i = 0; i < items.size(); i++) {
            String[] entry = items.get(i);
            String name   = entry[0];
            String itemId = entry[1];
            int diff      = Integer.parseInt(entry[3]);

            int rowY = panelY + PADDING + i * ROW_HEIGHT;
            int iconX = panelX + PADDING;
            int iconY = rowY + (ROW_HEIGHT - ICON_SIZE) / 2;

            // Item icon
            ResourceLocation loc = ResourceLocation.tryParse(itemId);
            Item item = BuiltInRegistries.ITEM.getValue(loc);
            graphics.renderItem(new ItemStack(item), iconX, iconY);

            // Count text
            int textX = iconX + ICON_SIZE + PADDING;
            int textY = rowY + (ROW_HEIGHT - mc.font.lineHeight) / 2;

            int countColor = diff >= 0 ? TEXT_GREEN : TEXT_WHITE;
            String countStr = entry[2];
            graphics.drawString(mc.font, countStr, textX, textY, countColor, true);

            // Diff in parens right after, colored
            if (diff != 0) {
                String diffStr = (diff > 0 ? " (+" : " (") + diff + ")";
                int countW = mc.font.width(countStr);
                int diffColor = diff > 0 ? TEXT_GREEN : TEXT_RED;
                graphics.drawString(mc.font, diffStr, textX + countW, textY, diffColor, true);
            }
        }
    }
}