package tomyto.tomyslists;

import java.awt.*;

import java.util.concurrent.Flow;


import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Surface;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.jetbrains.annotations.NotNull;

import tomyto.tomyslists.tomyslistsClient;

import static tomyto.tomyslists.tomyslistsClient.openListMainScreenKey;

public class ListMainScreen extends BaseOwoScreen<FlowLayout> {

    @Override
    public boolean keyPressed(KeyEvent input) {

        if (openListMainScreenKey.matches(input)) {
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        return super.keyPressed(input);
    }

        @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT);
    }

}
