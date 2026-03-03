package tomyto.tomyslists;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import tomyto.tomyslists.ListMainScreen;

public class tomyslistsClient implements ClientModInitializer {

    public static KeyMapping openListMainScreenKey;
    private static final KeyMapping.Category TomyListsCategory = KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath("tomys-lists", ""));


    @Override
    public void onInitializeClient() {


        openListMainScreenKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "Open material list screen", // Translation key
                InputConstants.Type.KEYSYM,    // Type: Keyboard
                GLFW.GLFW_KEY_R,              // Default key: R
                TomyListsCategory   // Category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openListMainScreenKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new ListMainScreen());
                }
            }
        });

    }

}

