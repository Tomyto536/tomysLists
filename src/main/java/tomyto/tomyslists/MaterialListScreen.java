package tomyto.tomyslists;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.CheckboxComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import io.wispforest.owo.ui.component.CheckboxComponent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MaterialListScreen extends BaseOwoScreen<FlowLayout> {

    private String selectedFile = null;
    private FlowLayout scrollContent;
    public String configFile = "tomyslistconfig.txt";

    Path schematicFolder = Minecraft.getInstance().gameDirectory.toPath()
            .resolve("config")
            .resolve("litematica");


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
                Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24))
                        .child((Components.label(Component.literal("Choose one material list")))
                                .sizing(Sizing.fill(100), Sizing.content())
                                .margins(Insets.both(10,6)))
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10,5))
        );

        // Create and store the inner container
        scrollContent = Containers.verticalFlow(Sizing.fill(100), Sizing.content());

        //Container for material list
        rootComponent.child(
                Containers.verticalScroll(Sizing.fill(100), Sizing.fill(77), scrollContent)
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10,5))
        );

        //Button bar
        rootComponent.child(
                Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(8))
                        .child(Components.button(Component.literal("Go back"),buttonComponent -> {Minecraft.getInstance().setScreen(new ListMainScreen());})
                                .margins(Insets.both(10,5))
                                .sizing(Sizing.content(), Sizing.fill(100))
                        )
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10,5))

        );

        loadSchematicFiles();
    }


    public void addItem(String text) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.horizontalAlignment(HorizontalAlignment.LEFT);

        row.child(
                Components.label(Component.literal(text))
                        .verticalTextAlignment(VerticalAlignment.CENTER)
                        .sizing(Sizing.fill(90), Sizing.content())
                        .margins(Insets.both(5,4))
        );

        row.child(
                Components.button(Component.literal("Select"), button -> {
                    saveSelectedFile(text);
                    Minecraft.getInstance().setScreen(new ListMainScreen());
                }).sizing(Sizing.fixed(50), Sizing.fixed(20))
        );

        row.surface(Surface.DARK_PANEL)
                .margins(Insets.both(5, 0));

        scrollContent.child(row);
    }

    public void loadSchematicFiles() {



        if (!Files.exists(schematicFolder)) {
            return;
        }
        if (!Files.exists(schematicFolder.resolve(configFile))) {
            try {
                Files.createFile(schematicFolder.resolve(configFile));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        try {
            Files.list(schematicFolder)
                    .filter(path -> path.toString().endsWith(".txt") && !path.toString().equals(schematicFolder.resolve(configFile).toString()))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String displayName = fileName.replace(".txt", "");
                        addItem(displayName);
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveSelectedFile(String fileName) {
        Path configPath = schematicFolder.resolve(configFile);
        try {
            List<String> existingLines = new ArrayList<>();
            if (Files.exists(configPath)) {
                existingLines = Files.readAllLines(configPath);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(fileName).append("\n");

            // Preserve lines 2+ (groupings)
            for (int i = 1; i < existingLines.size(); i++) {
                sb.append(existingLines.get(i)).append("\n");
            }

            Files.write(configPath, sb.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
