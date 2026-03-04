package tomyto.tomyslists;

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

import java.util.ArrayList;
import java.util.List;

public class MaterialListScreen extends BaseOwoScreen<FlowLayout> {

    private FlowLayout scrollContent;
    private final List<CheckboxComponent> checkboxes = new ArrayList<>();


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
                        .child(Components.button(Component.literal("Test"),buttonComponent -> {addItem("Test");})
                                .margins(Insets.both(10,5))
                                .sizing(Sizing.fill(30), Sizing.fill(100))
                        )
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(10,5))

        );



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

        CheckboxComponent checkbox = (CheckboxComponent) Components.checkbox(Component.literal(""))
                .sizing(Sizing.fixed(20), Sizing.fixed(20))
                .margins(Insets.both(0,1));


        row.child(checkbox);

        row.surface(Surface.DARK_PANEL)
                .margins(Insets.both(5, 0));

        checkboxes.add(checkbox);
        scrollContent.child(row);
    }
}
