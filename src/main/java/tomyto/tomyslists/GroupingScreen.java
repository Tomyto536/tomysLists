package tomyto.tomyslists;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;

public class GroupingScreen extends BaseOwoScreen<FlowLayout> {

    private FlowLayout groupingsList;
    private FlowLayout previewList;
    private TextBoxComponent textBox;
    private TextBoxComponent ignoreBox;

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

        // Top bar
        rootComponent.child(
                Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24))
                        .child(Components.label(Component.literal("Groupings"))
                                .sizing(Sizing.fill(100), Sizing.content())
                                .margins(Insets.both(10, 6)))
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10, 5))
        );

        // Main content area - groupings on left, preview on right
        FlowLayout mainArea = Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(77));

        // Left side - existing groupings
        FlowLayout leftPanel = Containers.verticalFlow(Sizing.fill(50), Sizing.fill(100));

        leftPanel.child(
                Components.label(Component.literal("Groupings"))
                        .margins(Insets.both(5, 4))
        );

        groupingsList = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        leftPanel.child(
                Containers.verticalScroll(Sizing.fill(100), Sizing.fill(85), groupingsList)
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(5, 2))
        );

        // Right side - preview of matching items
        FlowLayout rightPanel = Containers.verticalFlow(Sizing.fill(50), Sizing.fill(100));

        rightPanel.child(
                Components.label(Component.literal("Preview"))
                        .margins(Insets.both(5, 4))
        );

        previewList = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        rightPanel.child(
                Containers.verticalScroll(Sizing.fill(100), Sizing.fill(85), previewList)
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(5, 2))
        );

        mainArea.child(leftPanel);
        mainArea.child(rightPanel);

        rootComponent.child(
                mainArea
                        .margins(Insets.both(10, 5))
        );

        // Bottom bar - text input and buttons
        rootComponent.child(
                Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(8))
                        .child(
                                textBox = (TextBoxComponent) Components.textBox(Sizing.fill(40))
                                        .margins(Insets.both(10, 5))
                        )
                        .child(
                                ignoreBox = (TextBoxComponent) Components.textBox(Sizing.fill(30))
                                        .margins(Insets.both(5, 5))
                        )
                        .child(
                                Components.button(Component.literal("Add"), btn -> {
                                            addGrouping(textBox.getValue());
                                        })
                                        .margins(Insets.both(5, 5))
                                        .sizing(Sizing.fixed(40), Sizing.fill(80))
                        )
                        .child(
                                Components.button(Component.literal("Back"), btn -> {
                                            Minecraft.getInstance().setScreen(new ListMainScreen());
                                        })
                                        .margins(Insets.both(5, 5))
                                        .sizing(Sizing.fixed(40), Sizing.fill(80))
                        )
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10, 5))
        );

        loadGroupings();
    }

    private void addGrouping(String grouping) {
        if (grouping == null || grouping.isBlank()) return;

        List<String> ignored = new ArrayList<>();
        if (!ignoreBox.getValue().isBlank()) {
            for (String term : ignoreBox.getValue().split(",")) {
                ignored.add(term.trim());
            }
        }

        Map<String, List<String>> groupings = loadGroupingsList();
        groupings.put(grouping, ignored);
        saveGroupings(groupings);

        groupingsList.clearChildren();
        loadGroupings();
        textBox.setValue("");
        ignoreBox.setValue("");
        updatePreview("", new ArrayList<>());
    }

    private void deleteGrouping(String grouping) {
        Map<String, List<String>> groupings = loadGroupingsList();
        groupings.remove(grouping);
        saveGroupings(groupings);
        groupingsList.clearChildren();
        loadGroupings();
    }

    private void loadGroupings() {
        Map<String, List<String>> groupings = loadGroupingsList();
        groupings.forEach((grouping, ignored) -> addGroupingRow(grouping, ignored));
    }

    private void addGroupingRow(String grouping, List<String> ignored) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
        row.verticalAlignment(VerticalAlignment.CENTER);

        String displayText = ignored.isEmpty() ? grouping : grouping + " (excl: " + String.join(", ", ignored) + ")";

        row.child(
                Components.label(Component.literal(displayText))
                        .sizing(Sizing.fill(60), Sizing.content())
                        .margins(Insets.both(5, 4))
        );

        // Edit button
        row.child(
                Components.button(Component.literal("Edit"), btn -> {
                            textBox.setValue(grouping);
                            ignoreBox.setValue(String.join(", ", ignored));
                            deleteGrouping(grouping);
                        })
                        .sizing(Sizing.fixed(30), Sizing.fixed(16))
                        .margins(Insets.both(3, 4))
        );

        // Delete button
        row.child(
                Components.button(Component.literal("X"), btn -> deleteGrouping(grouping))
                        .sizing(Sizing.fixed(20), Sizing.fixed(16))
                        .margins(Insets.both(3, 4))
        );

        row.surface(Surface.DARK_PANEL).margins(Insets.both(3, 1));

        row.mouseDown().subscribe((click, doubled) -> {
            updatePreview(grouping, ignored);
            return true;
        });

        groupingsList.child(row);
    }

    private void updatePreview(String grouping, List<String> ignored) {
        previewList.clearChildren();
        if (grouping == null || grouping.isBlank()) return;

        BuiltInRegistries.ITEM.forEach(item -> {
            String itemName = new ItemStack(item).getHoverName().getString();
            if (itemName.toLowerCase().contains(grouping.toLowerCase())) {
                // Check if any ignored term matches
                boolean isIgnored = ignored.stream()
                        .anyMatch(term -> itemName.toLowerCase().contains(term.toLowerCase()));
                if (!isIgnored) {
                    previewList.child(
                            Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24))
                                    .child(Components.item(new ItemStack(item))
                                            .sizing(Sizing.fixed(16), Sizing.fixed(16))
                                            .margins(Insets.both(4, 4)))
                                    .child(Components.label(Component.literal(itemName))
                                            .sizing(Sizing.fill(80), Sizing.content())
                                            .margins(Insets.both(5, 4)))
                                    .surface(Surface.DARK_PANEL)
                                    .margins(Insets.both(3, 1))
                    );
                }
            }
        });
    }

    // Read groupings from line 2 onwards in config file
    private Map<String, List<String>> loadGroupingsList() {
        Map<String, List<String>> groupings = new LinkedHashMap<>();
        Path configPath = schematicFolder.resolve(configFile);
        if (!Files.exists(configPath)) return groupings;

        try {
            List<String> lines = Files.readAllLines(configPath);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isBlank()) continue;

                String[] parts = line.split("\\|");
                String grouping = parts[0].trim();
                List<String> ignored = new ArrayList<>();
                if (parts.length > 1 && !parts[1].isBlank()) {
                    for (String term : parts[1].split(",")) {
                        ignored.add(term.trim());
                    }
                }
                groupings.put(grouping, ignored);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return groupings;
    }

    // Write groupings back to file, preserving line 1
    private void saveGroupings(Map<String, List<String>> groupings) {
        Path configPath = schematicFolder.resolve(configFile);
        try {
            String firstLine = "";
            if (Files.exists(configPath)) {
                List<String> lines = Files.readAllLines(configPath);
                if (!lines.isEmpty()) firstLine = lines.get(0);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(firstLine).append("\n");
            groupings.forEach((grouping, ignored) -> {
                sb.append(grouping);
                if (!ignored.isEmpty()) {
                    sb.append("|").append(String.join(",", ignored));
                }
                sb.append("\n");
            });

            Files.write(configPath, sb.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
