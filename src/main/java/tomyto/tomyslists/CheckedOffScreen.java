package tomyto.tomyslists;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
                Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(32))
                        .child(Components.label(Component.literal("Checked Off Items"))
                                .margins(Insets.both(10, 5))
                        )
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10, 5))
        );

        // Scroll content
        scrollContent = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        scrollContent.padding(Insets.top(4));

        rootComponent.child(
                Containers.verticalScroll(Sizing.fill(100), Sizing.fill(79), scrollContent)
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10, 5))
        );

        // Bottom bar
        rootComponent.child(
                Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(32))
                        .child(Components.button(Component.literal("Back"), btn ->
                                                Minecraft.getInstance().setScreen(new ListMainScreen())
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
                    Components.label(Component.literal("No checked off items"))
                            .margins(Insets.of(10))
            );
            return;
        }

        for (String line : items) {
            String[] parts = line.split(",");
            if (parts.length < 2) continue;

            String name = parts[0].trim();
            int total = Integer.parseInt(parts[1].trim());

            ResourceLocation itemId = ResourceLocation.tryParse(
                    "minecraft:" + name.toLowerCase().replace(" ", "_")
            );
            Item item = BuiltInRegistries.ITEM.getValue(itemId);
            ItemStack stack = new ItemStack(item);

            FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
            row.verticalAlignment(VerticalAlignment.CENTER);

            row.child(Components.item(stack)
                    .sizing(Sizing.fixed(16), Sizing.fixed(16))
                    .margins(Insets.both(4, 4))
            );

            row.child(Components.label(Component.literal(name))
                    .sizing(Sizing.fill(20), Sizing.content())
                    .margins(Insets.both(5, 4))
            );

            row.child(Components.label(Component.literal(FileUtils.formatAmount(total)))
                    .sizing(Sizing.fill(60), Sizing.content())
                    .margins(Insets.both(5, 4))
            );

            row.child(Containers.horizontalFlow(Sizing.expand(), Sizing.fill(100))
                    .child(Components.button(Component.literal("↩"), btn -> {
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

            row.surface(Surface.DARK_PANEL)
                    .margins(Insets.both(5, 0));

            scrollContent.child(row);
        }
    }
}
