package tomyto.tomyslists;

import java.awt.*;
import java.util.concurrent.Flow;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Surface;
import org.jetbrains.annotations.NotNull;

import tomyto.tomyslists.tomyslistsClient;

import static tomyto.tomyslists.tomyslistsClient.openListMainScreenKey;

public class ListMainScreen extends BaseOwoScreen<FlowLayout> {

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        while (openListMainScreenKey.consumeClick()) {
            if (client.screen == null) {
                client.setScreen(new ListMainScreen());
            }
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
