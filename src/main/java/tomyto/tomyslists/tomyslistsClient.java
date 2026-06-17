package tomyto.tomyslists;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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
            KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath("tomys-lists", ""));

    @Override
    public void onInitializeClient() {

        openListMainScreenKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "Open material list screen",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                TomyListsCategory
        ));

        scrollUpKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "Scroll up the material list",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_W,
                TomyListsCategory
        ));

        scrollDownKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "Scroll down the material list",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_S,
                TomyListsCategory
        ));

        bringBackKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "Bring back checked off item",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_D,
                TomyListsCategory
        ));

        checkoffKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "Check item off the list",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_A,
                TomyListsCategory
        ));

        // Register the HUD overlay
        HudRenderCallback.EVENT.register(new ListOverlayRenderer());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Open screen keybind
            while (openListMainScreenKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new ListMainScreen());
                }
            }

            // Refresh overlay inventory counts every tick
            if (OverlayState.isVisible() && client.player != null && !OverlayState.getTotalCache().isEmpty()) {
                OverlayState.refreshFromInventory(name -> {
                    ResourceLocation id = ResourceLocation.tryParse(
                            "minecraft:" + name.toLowerCase().replace(" ", "_"));
                    Item item = BuiltInRegistries.ITEM.getValue(id);
                    return FileUtils.countItemInInventory(item);
                });
            }
        });
    }
}