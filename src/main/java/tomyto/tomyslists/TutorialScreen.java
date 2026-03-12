package tomyto.tomyslists;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TutorialScreen extends BaseOwoScreen<FlowLayout> {

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {

        float aspectRatio = 1093f / 1695f;
        float inverseRatio = 1695f / 1093f;

        int imageWidth = (int) (this.width * 0.8f);
        int imageHeight = (int) (imageWidth * aspectRatio);

        // If too tall, constrain by height instead
        if (imageHeight > this.height * 0.8f) {
            imageHeight = (int) (this.height * 0.8f);
            imageWidth = (int) (imageHeight * inverseRatio);
        }


        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        rootComponent.child(
                Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100))

                        .child(
                                Components.texture(
                                        ResourceLocation.fromNamespaceAndPath("tomys-lists", "textures/tutorial.png"),
                                        0, 0, 1695, 1093, 1695, 1093
                                ).sizing(Sizing.fixed(imageWidth), Sizing.fixed(imageHeight))
                        )

                        .child(
                                Components.button(Component.literal("Got it!"), btn -> {
                                            markTutorialSeen();
                                            Minecraft.getInstance().setScreen(new ListMainScreen());
                                        })
                                        .margins(Insets.top(10))
                        )
                        .horizontalAlignment(HorizontalAlignment.CENTER)
                        .verticalAlignment(VerticalAlignment.CENTER)
        );
    }

    private void markTutorialSeen() {
        Path configFile = Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config").resolve("litematica")
                .resolve("tomyslistconfig.txt");
        try {
            if (!Files.exists(configFile)) return;
            List<String> lines = new ArrayList<>(Files.readAllLines(configFile));
            if (lines.isEmpty() || !lines.get(0).trim().isEmpty()) return; // only write if first line is empty
            lines.set(0, "seen");
            Files.write(configFile, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}