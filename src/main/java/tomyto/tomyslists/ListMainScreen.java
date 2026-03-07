package tomyto.tomyslists;

import java.awt.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;

import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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

                // Save to file
                try {
                    String selectedFileName = Files.readString(schematicFolder.resolve(configFile)).trim();
                    Path materialFile = schematicFolder.resolve(selectedFileName + ".txt");
                    CheckOffItems.checkOff(materialFile, name);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        if (bringBackKey.matches(input)) {
            try {
                String selectedFileName = Files.readString(schematicFolder.resolve(configFile)).trim();
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

            } catch (IOException e) {
                e.printStackTrace();
            }
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

        scrollContainer = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(77), scrollContent);
        rootComponent.child(
                scrollContainer
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10,5))
        );

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

    //Scroll to selected row on opening
    @Override
    public void init() {
        super.init();

        if (selectedIndex >= 0 && selectedIndex < rows.size()) {
            int targetIndex = Math.max(0, selectedIndex -3);
            scrollContainer.scrollTo(rows.get(targetIndex));
        }
    }


    @Override
    public void onClose() {
        try {
            String selectedFileName = Files.readString(schematicFolder.resolve(configFile)).trim();
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
            String selectedFileName = Files.readString(configPath).trim();
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

        // Item icon
        row.child(
                Components.item(stack)
                        .sizing(Sizing.fixed(16), Sizing.fixed(16))
                        .margins(Insets.both(4, 4))
        );

        // Item name
        row.child(
                Components.label(Component.literal(name))
                        .sizing(Sizing.fill(40), Sizing.content())
                        .margins(Insets.both(5, 4))
        );

        // Total count
        row.child(
                Components.label(Component.literal(FileUtils.formatAmount(total)))
                        .horizontalTextAlignment(HorizontalAlignment.RIGHT)
                        .sizing(Sizing.fill(45), Sizing.content())
                        .margins(Insets.both(5, 4))
        );

        row.surface(Surface.DARK_PANEL)
                .margins(Insets.both(5, 0));

        rows.add(row);
        rowNames.add(name);
        scrollContent.child(row);

        int rowIndex = rows.size() - 1; // capture index before adding
        row.mouseDown().subscribe((click, doubled) -> {

            selectedIndex = rowIndex;
            Effects.select(rows, rowIndex);
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

}
