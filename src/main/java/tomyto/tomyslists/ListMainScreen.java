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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.core.Insets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.jetbrains.annotations.NotNull;

import net.minecraft.network.chat.Component;
import tomyto.tomyslists.tomyslistsClient;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.nbt.ListTag;
import java.util.stream.Collectors;

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
    private boolean isAutoGrouping = false;
    private final Map<String, Integer> totalMap = new java.util.LinkedHashMap<>();

    Path schematicFolder = Minecraft.getInstance().gameDirectory.toPath()
            .resolve("config")
            .resolve("litematica");

    /**
     * Safely reads the currently-selected file name from the config file.
     * Returns null (instead of throwing) if the config file doesn't exist,
     * is empty, or its first line is blank.
     */
    private String getSelectedFileName() throws IOException {
        Path configPath = schematicFolder.resolve(configFile);
        if (!Files.exists(configPath)) return null;

        List<String> lines = Files.readAllLines(configPath);
        if (lines.isEmpty()) return null;

        String name = lines.get(0).trim();
        return name.isBlank() ? null : name;
    }

    /**
     * Delegates to {@link RowStyle#updateRowAppearances} and also keeps
     * {@link Effects} in sync so any other screen reading
     * {@code Effects.getSelectedRow()} stays accurate.
     */
    private void updateRowAppearances() {
        Effects.setSelectedRow(selectedIndex);
        RowStyle.updateRowAppearances(rows, selectedIndex);
        OverlayState.setTotalCache(totalMap);   // <-- add this line
        OverlayState.update(rowNames, selectedIndex,
                name -> totalMap.getOrDefault(name, 0),
                name -> {
                    net.minecraft.resources.Identifier id =
                            net.minecraft.resources.Identifier.tryParse(
                                    "minecraft:" + name.toLowerCase().replace(" ", "_"));
                    net.minecraft.world.item.Item item =
                            net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(id);
                    return FileUtils.countItemInInventory(item);
                });
    }

    @Override
    public boolean keyPressed(KeyEvent input) {

        if (openListMainScreenKey.matches(input)) {
            Minecraft.getInstance().gui.setScreen(null);
            onClose();
            return true;
        }

        if (scrollUpKey.matches(input)) {
            selectedIndex = Math.max(0, selectedIndex - 1);
            updateRowAppearances();
            scrollToRow(selectedIndex);
            return true;
        }

        if (scrollDownKey.matches(input)) {
            selectedIndex = Math.min(rows.size() - 1, selectedIndex + 1);
            updateRowAppearances();
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
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void drawComponentTooltip(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float tickDelta) {
        // Temporarily disabled: owo-lib has an unpatched NPE in its tooltip/hover-effect
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.TOP);

        //Top bar
        rootComponent.child(
                UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(32))

                        .child(UIComponents.button(Component.literal("Group"), btn -> groupSelectedItem())
                                .margins(Insets.both(10,5))
                                .sizing(Sizing.fill(10), Sizing.fill(80))
                                .tooltip(Component.literal("Bring items with similar names near the selected item"))
                        )

                        .child(UIComponents.button(Component.literal("Auto Group"), btn -> autoGroup())
                                .margins(Insets.both(10,5))
                                .sizing(Sizing.fill(12), Sizing.fill(80))
                                .tooltip(Component.literal("Groups all the items in the material list"))
                        )

                        .child(UIComponents.button(Component.literal("Undo"), btn -> undoGrouping())
                                .margins(Insets.both(10,5))
                                .sizing(Sizing.fill(10), Sizing.fill(80))
                                .tooltip(Component.literal("Undo the last grouping"))
                        )

                        .child(UIContainers.horizontalFlow(Sizing.expand(), Sizing.fill(100))
                                .child(UIComponents.button(Component.literal("↩"), btn -> bringBackLastItem())
                                        .sizing(Sizing.fixed(20), Sizing.fill(80))
                                        .margins(Insets.both(3, 5))
                                        .tooltip(Component.literal("Bring back last checked off item"))
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
        scrollContent = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());
        scrollContent.padding(Insets.top(4));

        scrollContainer = UIContainers.verticalScroll(Sizing.fill(100), Sizing.expand(), scrollContent);
        rootComponent.child(
                scrollContainer
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10,5))
        );

        //Bottom bar
        rootComponent.child(
                UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(32))
                        .child(UIComponents.button(Component.literal("Open new material list"), buttonComponent -> {
                                            Minecraft.getInstance().gui.setScreen(new LitematicaImportScreen());
                                        })
                                        .margins(Insets.both(10,5))
                                        .sizing(Sizing.content(), Sizing.fill(80))
                        )
                        .child(UIComponents.button(Component.literal("Groupings"), buttonComponent -> {
                                            Minecraft.getInstance().gui.setScreen(new GroupingScreen());
                                        })
                                        .margins(Insets.both(10, 5))
                                        .sizing(Sizing.fill(10), Sizing.fill(80))
                                        .tooltip(Component.literal("Manage groupings"))
                        )
                        .child(UIComponents.button(Component.literal("Checked Off"), btn -> {
                                            try {
                                                String selectedFileName = getSelectedFileName();
                                                if (selectedFileName == null) return;
                                                Path materialFile = schematicFolder.resolve(selectedFileName + ".txt");
                                                Minecraft.getInstance().gui.setScreen(new CheckedOffScreen(materialFile));
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        })
                                        .sizing(Sizing.content(), Sizing.fill(80))
                                        .margins(Insets.both(10, 5))
                        )

                        .child(UIContainers.horizontalFlow(Sizing.expand(), Sizing.fill(100))
                                .child(UIComponents.button(
                                                Component.literal(OverlayState.isVisible() ? "HUD ✓" : "HUD"),
                                                btn -> {
                                                    OverlayState.toggle();
                                                    btn.setMessage(Component.literal(OverlayState.isVisible() ? "HUD ✓" : "HUD"));
                                                    updateRowAppearances(); // push current state to overlay
                                                })
                                        .sizing(Sizing.fixed(40), Sizing.fill(80))
                                        .margins(Insets.both(3, 5))
                                        .tooltip(Component.literal("Toggle HUD overlay"))
                                )
                                .horizontalAlignment(HorizontalAlignment.RIGHT)
                                .verticalAlignment(VerticalAlignment.CENTER)
                                .margins(Insets.right(15))
                        )

                        .verticalAlignment(VerticalAlignment.CENTER)
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10,5))
        );

        loadMaterialList();
    }

    @Override
    public void init() {
        super.init();

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
            String selectedFileName = getSelectedFileName();
            if (selectedFileName != null) {
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
        try {
            String selectedFileName = getSelectedFileName();
            if (selectedFileName == null) return;

            Path materialFile = schematicFolder.resolve(selectedFileName + ".txt");
            if (!Files.exists(materialFile)) return;

            Map<String, Integer> materials = FileUtils.loadMaterialList(materialFile);
            if (materials.isEmpty()) return;

            materials.forEach((name, total) -> addRow(name, total));

            int savedIndex = FileUtils.getSelectedIndex(materialFile);
            if (savedIndex >= 0) {
                selectedIndex = savedIndex;
                updateRowAppearances();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addRow(String name, int total) {
        // Skip checked-off items
        if (name.startsWith(CheckOffItems.CHECKEDOFF_MARKER)) return;
        totalMap.put(name, total);

        FlowLayout row = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
        row.verticalAlignment(VerticalAlignment.CENTER);

        // Convert name to item stack
        Identifier itemId = Identifier.tryParse(
                "minecraft:" + name.toLowerCase().replace(" ", "_")
        );
        Item item = BuiltInRegistries.ITEM.getValue(itemId);
        ItemStack stack = new ItemStack(item);

        // Inventory count and diff
        int playerCount = FileUtils.countItemInInventory(item);
        int textColor = playerCount >= total ? 0x55FF55 : 0xFFFFFF;
        int diff = playerCount - total;
        int diffColor = diff >= 0 ? 0x55FF55 : 0xFF5555;

        // Hover tooltip: break the absolute diff into shulkers, stacks, and items
        int absDiff = Math.abs(diff);
        int shulkers = absDiff / 1728;
        int remainAfterShulkers = absDiff % 1728;
        int stacks = remainAfterShulkers / 64;
        int items = remainAfterShulkers % 64;
        StringBuilder diffTooltip = new StringBuilder();
        if (diff != 0) {
            diffTooltip.append(diff >= 0 ? "Surplus: +" : "Missing: ");
            if (shulkers > 0) diffTooltip.append(shulkers).append(" shulker").append(shulkers > 1 ? "s" : "").append(" ");
            if (stacks > 0)   diffTooltip.append(stacks).append(" stack").append(stacks > 1 ? "s" : "").append(" ");
            if (items > 0 || (shulkers == 0 && stacks == 0)) diffTooltip.append(items).append(" item").append(items != 1 ? "s" : "");
        }

        // Item icon
        row.child(
                UIComponents.item(stack)
                        .sizing(Sizing.fixed(16), Sizing.fixed(16))
                        .margins(Insets.both(4, 4))
        );

        // Item name
        row.child(
                UIComponents.label(Component.literal(name))
                        .sizing(Sizing.fill(20), Sizing.content())
                        .margins(Insets.both(5, 4))
        );

        // Count + optional diff in parens, as two labels inside a right-aligned container
        FlowLayout countCell = UIContainers.horizontalFlow(Sizing.fill(65), Sizing.content());
        countCell.horizontalAlignment(HorizontalAlignment.RIGHT);
        countCell.verticalAlignment(VerticalAlignment.CENTER);
        countCell.margins(Insets.both(5, 4));

        countCell.child(
                UIComponents.label(Component.literal(FileUtils.formatAmount(total)))
                        .horizontalTextAlignment(HorizontalAlignment.RIGHT)
                        .color(Color.ofRgb(textColor))
                        .sizing(Sizing.content(), Sizing.content())
        );

        if (diff != 0) {
            String diffText = " (" + (diff > 0 ? "+" : "") + diff + ")";
            countCell.child(
                    UIComponents.label(Component.literal(diffText))
                            .horizontalTextAlignment(HorizontalAlignment.RIGHT)
                            .color(Color.ofRgb(diffColor))
                            .tooltip(Component.literal(diffTooltip.toString().trim()))
                            .sizing(Sizing.content(), Sizing.content())
            );
        }

        row.child(countCell);

        // Checkmark button — no background chip, just the button flush to the right
        row.child(
                UIContainers.horizontalFlow(Sizing.expand(), Sizing.fill(100))
                        .child(
                                UIComponents.button(Component.literal("\u2713"), btn -> {
                                            selectedIndex = rows.indexOf(row);
                                            updateRowAppearances();
                                            checkOffItem();
                                        })
                                        .sizing(Sizing.fixed(20), Sizing.fill(80))
                                        .margins(Insets.both(2, 0))
                        )
                        .horizontalAlignment(HorizontalAlignment.RIGHT)
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .margins(Insets.right(10))
        );

        int rowIndex = rows.size();
        row.surface(Surface.flat(rowIndex % 2 == 0 ? RowStyle.ROW_COLOR_EVEN : RowStyle.ROW_COLOR_ODD))
                .margins(Insets.both(5, 0));

        rows.add(row);
        rowNames.add(name);
        scrollContent.child(row);

        row.mouseDown().subscribe((click, doubled) -> {
            selectedIndex = rows.indexOf(row);
            updateRowAppearances();
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

        io.wispforest.owo.ui.core.UIComponent row = rows.get(index);
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

        if (!isAutoGrouping) undoStack.add(new ArrayList<>(rowNames));

        String selectedName = rowNames.get(selectedIndex);
        try {
            String selectedFileName = getSelectedFileName();
            if (selectedFileName == null) return;
            Path materialFile = schematicFolder.resolve(selectedFileName + ".txt");
            GroupingUtils.groupItemsAfterSelected(materialFile, schematicFolder.resolve(configFile), selectedName);

            Map<String, List<String>> groupings = GroupingUtils.loadGroupings(schematicFolder.resolve(configFile));
            Map.Entry<String, List<String>> matchedGrouping = GroupingUtils.findGroupingForItem(selectedName, groupings);
            if (matchedGrouping == null) return;

            List<Integer> matchingIndices = new ArrayList<>();
            for (int i = 0; i < rowNames.size(); i++) {
                if (i == selectedIndex) continue;
                String name = rowNames.get(i);
                boolean matches = name.toLowerCase().contains(matchedGrouping.getKey().toLowerCase());
                boolean isIgnored = matchedGrouping.getValue().stream()
                        .anyMatch(term -> name.toLowerCase().contains(term.toLowerCase()));
                if (matches && !isIgnored) matchingIndices.add(i);
            }

            List<FlowLayout> matchingRows = new ArrayList<>();
            List<String> matchingNames = new ArrayList<>();
            for (int i = matchingIndices.size() - 1; i >= 0; i--) {
                int idx = matchingIndices.get(i);
                matchingRows.add(0, rows.remove(idx));
                matchingNames.add(0, rowNames.remove(idx));
                scrollContent.removeChild(matchingRows.get(0));
                if (idx < selectedIndex) selectedIndex--;
            }

            int insertAt = selectedIndex + 1;
            for (int i = 0; i < matchingRows.size(); i++) {
                rows.add(insertAt + i, matchingRows.get(i));
                rowNames.add(insertAt + i, matchingNames.get(i));
                scrollContent.child(insertAt + i, matchingRows.get(i));
            }

            updateRowAppearances();

            Map<String, Integer> materials = FileUtils.loadMaterialList(materialFile);
            FileUtils.saveSimpleFormat(materialFile, materials, selectedIndex);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void undoGrouping() {
        if (undoStack.isEmpty()) return;

        List<String> previousOrder = undoStack.remove(undoStack.size() - 1);

        List<FlowLayout> newRows = new ArrayList<>();
        List<String> newRowNames = new ArrayList<>();

        for (String name : previousOrder) {
            int idx = rowNames.indexOf(name);
            if (idx >= 0) {
                newRows.add(rows.get(idx));
                newRowNames.add(rowNames.get(idx));
            }
        }

        scrollContent.clearChildren();
        rows.clear();
        rowNames.clear();
        totalMap.clear();

        for (int i = 0; i < newRows.size(); i++) {
            rows.add(newRows.get(i));
            rowNames.add(newRowNames.get(i));
            scrollContent.child(newRows.get(i));
        }

        updateRowAppearances();

        try {
            String selectedFileName = getSelectedFileName();
            if (selectedFileName == null) return;
            Path materialFile = schematicFolder.resolve(selectedFileName + ".txt");
            Map<String, Integer> materials = FileUtils.loadMaterialList(materialFile);
            FileUtils.saveSimpleFormat(materialFile, materials, selectedIndex);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void autoGroup() {
        String selectedName = selectedIndex >= 0 ? rowNames.get(selectedIndex) : null;
        isAutoGrouping = true;
        undoStack.add(new ArrayList<>(rowNames));
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

        isAutoGrouping = false;

        if (selectedName != null) {
            int restoredIndex = rowNames.indexOf(selectedName);
            if (restoredIndex >= 0) {
                selectedIndex = restoredIndex;
                updateRowAppearances();
            }
        }

        try {
            String selectedFileName = getSelectedFileName();
            if (selectedFileName == null) return;
            Path materialFile = schematicFolder.resolve(selectedFileName + ".txt");
            Map<String, Integer> materials = FileUtils.loadMaterialList(materialFile);
            FileUtils.saveSimpleFormat(materialFile, materials, selectedIndex);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void bringBackLastItem() {
        try {
            String selectedFileName = getSelectedFileName();
            if (selectedFileName == null) return;
            Path materialFile = schematicFolder.resolve(selectedFileName + ".txt");
            String restoredName = CheckOffItems.bringBack(materialFile);

            rows.clear();
            rowNames.clear();
            totalMap.clear();
            scrollContent.clearChildren();
            loadMaterialList();

            if (restoredName != null) {
                int restoredIndex = rowNames.indexOf(restoredName);
                if (restoredIndex >= 0) {
                    selectedIndex = restoredIndex;
                    updateRowAppearances();
                }
            }

            Minecraft.getInstance().player.playSound(SoundEvents.STONE_BREAK);
            scrollToRow(selectedIndex);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkOffItem() {
        if (selectedIndex >= 0 && selectedIndex < rows.size()) {
            String name = rowNames.get(selectedIndex);

            scrollContent.removeChild(rows.get(selectedIndex));
            rows.remove(selectedIndex);
            rowNames.remove(selectedIndex);

            selectedIndex = Math.min(selectedIndex, rows.size() - 1);
            if (selectedIndex >= 0) {
                updateRowAppearances();
            }

            Minecraft.getInstance().player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);

            try {
                String selectedFileName = getSelectedFileName();
                if (selectedFileName == null) return;
                Path materialFile = schematicFolder.resolve(selectedFileName + ".txt");
                CheckOffItems.checkOff(materialFile, name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}