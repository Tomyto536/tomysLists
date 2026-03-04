package tomyto.tomyslists;

import java.awt.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
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

    private FlowLayout scrollContent;
    public String configFile = "tomyslistconfig.txt";

    Path schematicFolder = Minecraft.getInstance().gameDirectory.toPath()
            .resolve("config")
            .resolve("litematica");

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

        //Scroll content
        scrollContent = Containers.verticalFlow(Sizing.fill(100), Sizing.content());

        rootComponent.child(
                Containers.verticalScroll(Sizing.fill(100), Sizing.fill(77), scrollContent)
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10, 5))
        );

//        //Container for material list
//        rootComponent.child(
//                Containers.verticalScroll(Sizing.fill(100), Sizing.fill(77),
//                                Containers.verticalFlow(Sizing.fill(100), Sizing.fill())
//                                        .surface(Surface.DARK_PANEL)
//                        )
//                        .surface(Surface.DARK_PANEL)
//                        .margins(Insets.both(10,5))
//        );

        //Button bar
        rootComponent.child(
                Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(8))
                        .child(Components.button(Component.literal("Open new material list"),buttonComponent -> {Minecraft.getInstance().setScreen(new MaterialListScreen());})
                                .margins(Insets.both(10,5))
                                .sizing(Sizing.content(), Sizing.fill(100))
                        )
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10,5))

        );

        loadMaterialList();
    }

    private void loadMaterialList() {
        Path configPath = schematicFolder.resolve(configFile);

        if (!Files.exists(configPath)) return;

        try {
            String selectedFileName = Files.readString(configPath).trim();
            if (selectedFileName.isBlank()) return;

            Path materialFile = schematicFolder.resolve(selectedFileName + ".txt");
            if (!Files.exists(materialFile)) return;

            Map<String, Integer> materials = FileUtils.parseMaterialList(materialFile);

            materials.forEach((name, total) -> addRow(name, total));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addRow(String name, int total) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
        row.verticalAlignment(VerticalAlignment.CENTER);

        row.child(
                Components.label(Component.literal(name))
                        .sizing(Sizing.fill(70), Sizing.content())
                        .margins(Insets.both(5,4))
        );

        row.child(
                Components.label(Component.literal("x" + total))
                        .sizing(Sizing.fill(30), Sizing.content())
                        .margins(Insets.both(5,4))
        );

        row.surface(Surface.DARK_PANEL)
                .margins(Insets.both(5,0));

        scrollContent.child(row);
    }
}
