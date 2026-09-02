package tomyto.tomyslists;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.lwjgl.glfw.GLFW;

import tomyto.tomyslists.ListMainScreen;

public class tomyslistsClient implements ClientModInitializer {

    public static KeyMapping openListMainScreenKey;
    public static KeyMapping scrollUpKey;
    public static KeyMapping scrollDownKey;
    public static KeyMapping checkoffKey;
    public static KeyMapping bringBackKey;
    private static final KeyMapping.Category TomyListsCategory =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("tomys-lists", ""));

    @Override
    public void onInitializeClient() {

        openListMainScreenKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "Open material list screen",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                TomyListsCategory
        ));

        scrollUpKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "Scroll up the material list",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_W,
                TomyListsCategory
        ));

        scrollDownKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "Scroll down the material list",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_S,
                TomyListsCategory
        ));

        bringBackKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "Bring back checked off item",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_D,
                TomyListsCategory
        ));

        checkoffKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "Check item off the list",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_A,
                TomyListsCategory
        ));

        // Register the HUD overlay
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("tomyslists", "list_overlay"),
                ListOverlayRenderer::render
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Open screen keybind
            while (openListMainScreenKey.consumeClick()) {
                if (client.gui.screen() == null) {
                    client.gui.setScreen(new ListMainScreen());
                }
            }

            // Refresh overlay inventory counts every tick
            if (OverlayState.isVisible() && client.player != null && !OverlayState.getTotalCache().isEmpty()) {
                OverlayState.refreshFromInventory(name -> {
                    Identifier id = Identifier.tryParse(
                            "minecraft:" + name.toLowerCase().replace(" ", "_"));
                    Item item = BuiltInRegistries.ITEM.getValue(id);
                    return FileUtils.countItemInInventory(item);
                });
            }
        });
    }
}