package tomyto.tomyslists;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CheckedOffScreen extends BaseOwoScreen<FlowLayout> {

    private final Path materialFile;
    private FlowLayout scrollContent;

    public CheckedOffScreen(Path materialFile) {
        super(Component.literal("Checked Off Items"));
        this.materialFile = materialFile;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.TOP);

        // Top bar
        rootComponent.child(
                UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(32))
                        .child(UIComponents.label(Component.literal("Checked Off Items"))
                                .margins(Insets.both(10, 5))
                        )
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10, 5))
        );

        // Scroll content
        scrollContent = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());
        scrollContent.padding(Insets.top(4));

        rootComponent.child(
                UIContainers.verticalScroll(Sizing.fill(100), Sizing.fill(79), scrollContent)
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10, 5))
        );

        // Bottom bar
        rootComponent.child(
                UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(32))
                        .child(UIComponents.button(Component.literal("Back"), btn ->
                                                Minecraft.getInstance().gui.setScreen(new ListMainScreen())
                                        ).sizing(Sizing.content(), Sizing.fill(80))
                                        .margins(Insets.both(10, 5))
                        )
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10, 5))
        );

        loadCheckedOffItems();
    }

    private void loadCheckedOffItems() {
        scrollContent.clearChildren();
        List<String> items = CheckOffItems.getCheckedOffItems(materialFile);

        if (items.isEmpty()) {
            scrollContent.child(
                    UIComponents.label(Component.literal("No checked off items"))
                            .margins(Insets.of(10))
            );
            return;
        }

        int rowIndex = 0;
        for (String line : items) {
            String[] parts = line.split(",");
            if (parts.length < 2) continue;

            String name = parts[0].trim();
            int total = Integer.parseInt(parts[1].trim());

            Identifier itemId = Identifier.tryParse(
                    "minecraft:" + name.toLowerCase().replace(" ", "_")
            );
            Item item = BuiltInRegistries.ITEM.getValue(itemId);
            ItemStack stack = new ItemStack(item);

            FlowLayout row = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
            row.verticalAlignment(VerticalAlignment.CENTER);

            row.child(UIComponents.item(stack)
                    .sizing(Sizing.fixed(16), Sizing.fixed(16))
                    .margins(Insets.both(4, 4))
            );

            row.child(UIComponents.label(Component.literal(name))
                    .sizing(Sizing.fill(20), Sizing.content())
                    .margins(Insets.both(5, 4))
            );

            row.child(UIComponents.label(Component.literal(FileUtils.formatAmount(total)))
                    .sizing(Sizing.fill(60), Sizing.content())
                    .margins(Insets.both(5, 4))
            );

            row.child(UIContainers.horizontalFlow(Sizing.expand(), Sizing.fill(100))
                    .child(UIComponents.button(Component.literal("↩"), btn -> {
                                        CheckOffItems.bringBackSpecific(materialFile, name);
                                        loadCheckedOffItems();
                                    }).sizing(Sizing.fixed(20), Sizing.fill(80))
                                    .margins(Insets.both(3, 5))
                                    .tooltip(Component.literal("Bring back to list"))
                    )
                    .horizontalAlignment(HorizontalAlignment.RIGHT)
                    .verticalAlignment(VerticalAlignment.CENTER)
                    .margins(Insets.right(10))
            );

            row.surface(Surface.flat(rowIndex % 2 == 0 ? RowStyle.ROW_COLOR_EVEN : RowStyle.ROW_COLOR_ODD))
                    .margins(Insets.both(5, 0));

            scrollContent.child(row);
            rowIndex++;
        }
    }
}