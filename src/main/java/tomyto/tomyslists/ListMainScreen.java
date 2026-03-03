package tomyto.tomyslists;

import java.awt.*;

import java.util.concurrent.Flow;


import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.core.Insets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.jetbrains.annotations.NotNull;

import net.minecraft.network.chat.Component;
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
        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.TOP);

        //Top bar
        rootComponent.child(
                Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(10))
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10,5))
        );

        //Container for material list
        rootComponent.child(
                Containers.verticalScroll(Sizing.fill(100), Sizing.fill(77),
                                Containers.verticalFlow(Sizing.fill(100), Sizing.content())  // <-- child container
                                        .surface(Surface.DARK_PANEL)
                        )
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10,5))
        );

        //Button bar
        rootComponent.child(
                Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(8))
                        .child(Components.button(Component.literal("Test"),buttonComponent -> {System.out.println("Test");})
                                .margins(Insets.both(10,5))
                                .sizing(Sizing.fill(30), Sizing.fill(100))
                        )
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10,5))

        );


    }

}
