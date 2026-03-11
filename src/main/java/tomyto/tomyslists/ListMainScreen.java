package tomyto.tomyslists;

import java.awt.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;
import java.util.HashSet;
import java.util.Set;

import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import static tomyto.tomyslists.tomyslistsClient.scrollUpKey;
import static tomyto.tomyslists.tomyslistsClient.scrollDownKey;
import static tomyto.tomyslists.tomyslistsClient.checkoffKey;
import static tomyto.tomyslists.tomyslistsClient.bringBackKey;

public class ListMainScreen extends BaseOwoScreen<FlowLayout> {

    private FlowLayout scrollContent;
    public String configFile = "tomyslistconfig.txt";
    public final List<FlowLayout> rows = new ArrayList<>();
    private int selectedIndex = -1;
    private io.wispforest.owo.ui.container.ScrollContainer<?> scrollContainer;
    public final List<String> rowNames = new ArrayList<>();
    private boolean skipInitScroll = false;
    private List<List<String>> undoStack = new ArrayList<>();

    Path schematicFolder = Minecraft.getInstance().gameDirectory.toPath()
            .resolve("config")
            .resolve("litematica");

    @Override
    public boolean keyPressed(KeyEvent input) {

        if (openListMainScreenKey.matches(input)) {
            Minecraft.getInstance().setScreen(null);
            onClose();
            return true;
        }

        if (scrollUpKey.matches(input)) {
            selectedIndex = Math.max(0, selectedIndex - 1);
            Effects.select(rows, selectedIndex);
            scrollToRow(selectedIndex);
            return true;
        }

        if (scrollDownKey.matches(input)) {
            selectedIndex = Math.min(rows.size() - 1, selectedIndex + 1);
            Effects.select(rows, selectedIndex);
            scrollToRow(selectedIndex);
            return true;
        }

        if (checkoffKey.matches(input)) {
            checkOffItem();
            return true;
        }

        if (bringBackKey.matches(input)) {
            bringBackLastItem();
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
                Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(32))


                        .child(Components.button(Component.literal("Group"), btn -> groupSelectedItem())
                                .margins(Insets.both(10,5))
                                .sizing(Sizing.fill(10), Sizing.fill(80))
                        )

                        .child(Components.button(Component.literal("Auto Group"), btn -> autoGroup())
                                .margins(Insets.both(10,5))
                                .sizing(Sizing.fill(12), Sizing.fill(80))
                        )


                        .child(Components.button(Component.literal("Undo"), btn -> undoGrouping())
                                .margins(Insets.both(10,5))
                                .sizing(Sizing.fill(10), Sizing.fill(80))
                        )


                        .child(Containers.horizontalFlow(Sizing.expand(), Sizing.fill(100))
                                .child(Components.button(Component.literal("↩"), btn -> bringBackLastItem())
                                        .sizing(Sizing.fixed(20), Sizing.fill(80))
                                        .margins(Insets.both(3, 5))
                                )
                                .horizontalAlignment(HorizontalAlignment.RIGHT)
                                .verticalAlignment(VerticalAlignment.CENTER)
                                .margins(Insets.right(15))
                        )

                        .verticalAlignment(VerticalAlignment.CENTER)
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10,5))

                );


        //Scroll content
        scrollContent = Containers.verticalFlow(Sizing.fill(100), Sizing.content());

        scrollContainer = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(79), scrollContent);
        rootComponent.child(
                scrollContainer
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10,5))
        );

        //Bottom bar
        rootComponent.child(
                Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(32))
                        .child(Components.button(Component.literal("Open new material list"),buttonComponent -> {Minecraft.getInstance().setScreen(new MaterialListScreen());})
                                .margins(Insets.both(10,5))
                                .sizing(Sizing.content(), Sizing.fill(80))
                        )
                        .child(Components.button(Component.literal("Groupings"), buttonComponent -> {Minecraft.getInstance().setScreen(new GroupingScreen());})
                                .margins(Insets.both(10, 5))
                                .sizing(Sizing.fill(10), Sizing.fill(80))
                        )
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10,5))


        );

        loadMaterialList();
    }

    //Scroll to selected row on opening
    @Override
    public void init() {
        super.init();

        System.out.println("IM CALLED " + skipInitScroll);

        if (skipInitScroll) {
            skipInitScroll = false;
            return;
        }

        if (selectedIndex >= 0 && selectedIndex < rows.size()) {
            int targetIndex = Math.max(0, selectedIndex - 3);
            scrollContainer.scrollTo(rows.get(targetIndex));
        }
    }


    @Override
    public void onClose() {
        try {
            String selectedFileName = Files.readAllLines(schematicFolder.resolve(configFile)).get(0).trim();
            if (!selectedFileName.isBlank()) {
                Path materialFile = schematicFolder.resolve(selectedFileName + ".txt");
                Map<String, Integer> materials = FileUtils.loadMaterialList(materialFile);
                FileUtils.saveSimpleFormat(materialFile, materials, selectedIndex);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onClose();
    }


    private void loadMaterialList() {
        Path configPath = schematicFolder.resolve(configFile);

        if (!Files.exists(configPath)) return;

        try {
            String selectedFileName = Files.readAllLines(configPath).get(0).trim();
            if (selectedFileName.isBlank()) return;

            Path materialFile = schematicFolder.resolve(selectedFileName + ".txt");
            if (!Files.exists(materialFile)) return;

            Map<String, Integer> materials = FileUtils.loadMaterialList(materialFile);
            if (materials.isEmpty()) return;

            materials.forEach((name, total) -> addRow(name, total));

            int savedIndex = FileUtils.getSelectedIndex(materialFile);
            if (savedIndex >= 0) {
                selectedIndex = savedIndex;
                Effects.select(rows, selectedIndex);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void addRow(String name, int total) {
        //Skip the line if item is checked off
        if (name.startsWith(CheckOffItems.CHECKEDOFF_MARKER)) return;




        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
        row.verticalAlignment(VerticalAlignment.CENTER);

        // Convert name to item stack
        ResourceLocation itemId = ResourceLocation.tryParse(
                "minecraft:" + name.toLowerCase().replace(" ", "_")
        );
        Item item = BuiltInRegistries.ITEM.getValue(itemId);
        ItemStack stack = new ItemStack(item);

        //Decide color of text
        int playerCount = Minecraft.getInstance().player.getInventory().countItem(item);
        int textColor = playerCount >= total ? 0x55FF55 : 0xFFFFFF;

        // Item icon
        row.child(
                Components.item(stack)
                        .sizing(Sizing.fixed(16), Sizing.fixed(16))
                        .margins(Insets.both(4, 4))
        );

        // Item name
        row.child(
                Components.label(Component.literal(name))
                        .sizing(Sizing.fill(20), Sizing.content())
                        .margins(Insets.both(5, 4))
        );

        // Total count
        row.child(
                Components.label(Component.literal(FileUtils.formatAmount(total)))
                        .horizontalTextAlignment(HorizontalAlignment.LEFT)
                        .color(Color.ofRgb(textColor))
                        .sizing(Sizing.fill(65), Sizing.content())
                        .margins(Insets.both(5, 4))
        );

        row.child(Containers.horizontalFlow(Sizing.expand(), Sizing.fill(100))
                .child(Components.button(Component.literal("✓"), btn -> checkOffItem())
                        .sizing(Sizing.fixed(20), Sizing.fill(80))
                        .margins(Insets.both(3, 5))
                )
                .horizontalAlignment(HorizontalAlignment.RIGHT)
                .verticalAlignment(VerticalAlignment.CENTER)
                .margins(Insets.right(10))
        );

        row.surface(Surface.DARK_PANEL)
                .margins(Insets.both(5, 0));

        rows.add(row);
        rowNames.add(name);
        scrollContent.child(row);

        int rowIndex = rows.size() - 1; // capture index before adding
        row.mouseDown().subscribe((click, doubled) -> {
            selectedIndex = rows.indexOf(row);
            Effects.select(rows, rows.indexOf(row));
            return true;
        });
    }

    private void scrollToRow(int index) {
        if (index < 0 || index >= rows.size()) return;

        int viewportTop = scrollContainer.y();
        int viewportBottom = viewportTop + scrollContainer.height();
        int rowHeight = 24;
        int viewportHeight = scrollContainer.height();
        int contentHeight = scrollContent.height();

        io.wispforest.owo.ui.core.Component row = rows.get(index);
        int rowTop = row.y();
        int rowBottom = rowTop + row.height();



        if (rowTop < viewportTop) {
            int targetIndex = Math.max(0, index - 3);
            scrollContainer.scrollTo(rows.get(targetIndex));
        } else if (rowBottom > viewportBottom) {
            int targetY = ((index + 3) * rowHeight) - viewportHeight;
            double progress = (double) targetY / (contentHeight - viewportHeight);
            scrollContainer.scrollTo(Math.max(0, Math.min(1, progress)));
        }
    }

    private void groupSelectedItem() {
        if (selectedIndex < 0 || selectedIndex >= rowNames.size()) return;

        undoStack.add(new ArrayList<>(rowNames));

        String selectedName = rowNames.get(selectedIndex);
        try {
            String selectedFileName = Files.readAllLines(schematicFolder.resolve(configFile)).get(0).trim();
            Path materialFile = schematicFolder.resolve(selectedFileName + ".txt");
            GroupingUtils.groupItemsAfterSelected(materialFile, schematicFolder.resolve(configFile), selectedName);

            // Find which rows match the grouping and need to move
            Map<String, List<String>> groupings = GroupingUtils.loadGroupings(schematicFolder.resolve(configFile));
            Map.Entry<String, List<String>> matchedGrouping = GroupingUtils.findGroupingForItem(selectedName, groupings);
            if (matchedGrouping == null) return;

            // Find matching row indices (excluding selected)
            List<Integer> matchingIndices = new ArrayList<>();
            for (int i = 0; i < rowNames.size(); i++) {
                if (i == selectedIndex) continue;
                String name = rowNames.get(i);
                boolean matches = name.toLowerCase().contains(matchedGrouping.getKey().toLowerCase());
                boolean isIgnored = matchedGrouping.getValue().stream()
                        .anyMatch(term -> name.toLowerCase().contains(term.toLowerCase()));
                if (matches && !isIgnored) matchingIndices.add(i);
            }

            // Remove matching rows from their current positions (in reverse to preserve indices)
            List<FlowLayout> matchingRows = new ArrayList<>();
            List<String> matchingNames = new ArrayList<>();
            for (int i = matchingIndices.size() - 1; i >= 0; i--) {
                int idx = matchingIndices.get(i);
                matchingRows.add(0, rows.remove(idx));
                matchingNames.add(0, rowNames.remove(idx));
                scrollContent.removeChild(matchingRows.get(0));
                if (idx < selectedIndex) selectedIndex--;
            }

            // Insert matching rows right after selected
            int insertAt = selectedIndex + 1;
            for (int i = 0; i < matchingRows.size(); i++) {
                rows.add(insertAt + i, matchingRows.get(i));
                rowNames.add(insertAt + i, matchingNames.get(i));
                scrollContent.child(insertAt + i, matchingRows.get(i));
            }

            Effects.select(rows, selectedIndex);

            // Save new order to file
            Map<String, Integer> materials = FileUtils.loadMaterialList(materialFile);
            FileUtils.saveSimpleFormat(materialFile, materials, selectedIndex);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void undoGrouping() {
        if (undoStack.isEmpty()) return;

        List<String> previousOrder = undoStack.remove(undoStack.size() - 1);

        // Reorder rows and rowNames to match previous order
        List<FlowLayout> newRows = new ArrayList<>();
        List<String> newRowNames = new ArrayList<>();

        for (String name : previousOrder) {
            int idx = rowNames.indexOf(name);
            if (idx >= 0) {
                newRows.add(rows.get(idx));
                newRowNames.add(rowNames.get(idx));
            }
        }

        // Update scroll content order
        scrollContent.clearChildren();
        rows.clear();
        rowNames.clear();

        for (int i = 0; i < newRows.size(); i++) {
            rows.add(newRows.get(i));
            rowNames.add(newRowNames.get(i));
            scrollContent.child(newRows.get(i));
        }

        Effects.select(rows, selectedIndex);

        // Save to file
        try {
            String selectedFileName = Files.readAllLines(schematicFolder.resolve(configFile)).get(0).trim();
            Path materialFile = schematicFolder.resolve(selectedFileName + ".txt");
            Map<String, Integer> materials = FileUtils.loadMaterialList(materialFile);
            FileUtils.saveSimpleFormat(materialFile, materials, selectedIndex);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void autoGroup() {
        String selectedName = selectedIndex >= 0 ? rowNames.get(selectedIndex) : null;
        Set<String> alreadyGrouped = new HashSet<>();

        for (int i = 0; i < rowNames.size(); i++) {
            String name = rowNames.get(i);
            if (alreadyGrouped.contains(name)) continue;

            Map<String, List<String>> groupings = GroupingUtils.loadGroupings(schematicFolder.resolve(configFile));
            Map.Entry<String, List<String>> matchedGrouping = GroupingUtils.findGroupingForItem(name, groupings);
            if (matchedGrouping == null) continue;

            if (alreadyGrouped.isEmpty()) undoStack.add(new ArrayList<>(rowNames));

            selectedIndex = i;
            groupSelectedItem();

            for (String rowName : rowNames) {
                boolean matches = rowName.toLowerCase().contains(matchedGrouping.getKey().toLowerCase());
                boolean isIgnored = matchedGrouping.getValue().stream()
                        .anyMatch(term -> rowName.toLowerCase().contains(term.toLowerCase()));
                if (matches && !isIgnored) alreadyGrouped.add(rowName);
            }

            i = rowNames.indexOf(name);
        }

        // Restore selected row by name
        if (selectedName != null) {
            int restoredIndex = rowNames.indexOf(selectedName);
            if (restoredIndex >= 0) {
                selectedIndex = restoredIndex;
                Effects.select(rows, selectedIndex);
            }
        }

        try {
            String selectedFileName = Files.readAllLines(schematicFolder.resolve(configFile)).get(0).trim();
            Path materialFile = schematicFolder.resolve(selectedFileName + ".txt");
            Map<String, Integer> materials = FileUtils.loadMaterialList(materialFile);
            FileUtils.saveSimpleFormat(materialFile, materials, selectedIndex);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void bringBackLastItem() {
        try {
            String selectedFileName = Files.readAllLines(schematicFolder.resolve(configFile)).get(0).trim();
            Path materialFile = schematicFolder.resolve(selectedFileName + ".txt");
            CheckOffItems.bringBack(materialFile);

            // Clear and reload
            rows.clear();
            rowNames.clear();
            scrollContent.clearChildren();
            loadMaterialList();

            selectedIndex = 0;
            Effects.select(rows, selectedIndex);
            scrollContainer.scrollTo(rows.get(selectedIndex));

            Minecraft.getInstance().player.playSound(SoundEvents.STONE_BREAK);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkOffItem() {

        if (selectedIndex >= 0 && selectedIndex < rows.size()) {
            // Get the name from the selected row's label
            String name = rowNames.get(selectedIndex);

            // Remove from display
            scrollContent.removeChild(rows.get(selectedIndex));
            rows.remove(selectedIndex);
            rowNames.remove(selectedIndex);

            // Clamp selected index
            selectedIndex = Math.min(selectedIndex, rows.size() - 1);
            if (selectedIndex >= 0) {
                Effects.select(rows, selectedIndex);
            }

            Minecraft.getInstance().player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);

            // Save to file
            try {
                String selectedFileName = Files.readAllLines(schematicFolder.resolve(configFile)).get(0).trim();
                Path materialFile = schematicFolder.resolve(selectedFileName + ".txt");
                CheckOffItems.checkOff(materialFile, name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
