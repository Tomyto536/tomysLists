package tomyto.tomyslists;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

public class ExistingFilesPopupScreen extends BaseOwoScreen<FlowLayout> {

    private final LitematicaImportScreen parent;
    private final List<Path> existingFiles;
    private final String selectedFile;

    public ExistingFilesPopupScreen(LitematicaImportScreen parent, List<Path> existingFiles, String selectedFile) {
        super(Component.literal("Existing files"));
        this.parent = parent;
        this.existingFiles = existingFiles;
        this.selectedFile = selectedFile;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        FlowLayout popup = UIContainers.verticalFlow(Sizing.fill(40), Sizing.content());
        popup.surface(Surface.DARK_PANEL);
        popup.padding(Insets.of(10));
        popup.horizontalAlignment(HorizontalAlignment.CENTER);

        popup.child(
                UIComponents.label(Component.literal("Existing files found for \"" + selectedFile + "\":"))
                        .margins(Insets.bottom(6))
        );

        for (Path file : existingFiles) {
            String fileName = file.getFileName().toString().replace(".txt", "");
            popup.child(
                    UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24))
                            .child(
                                    UIComponents.label(Component.literal(fileName))
                                            .sizing(Sizing.fill(70), Sizing.content())
                                            .margins(Insets.both(4, 4))
                            )
                            .child(
                                    UIComponents.button(Component.literal("Use this"), btn -> {
                                                parent.saveSelectedFile(file.getFileName().toString());
                                                Minecraft.getInstance().gui.setScreen(new ListMainScreen());
                                            }).sizing(Sizing.fill(30), Sizing.fixed(16))
                                            .margins(Insets.both(3, 4))
                            )
                            .surface(Surface.flat(0x44FFFFFF))
                            .margins(Insets.bottom(2))
            );
        }

        popup.child(UIComponents.box(Sizing.fill(100), Sizing.fixed(1))
                .margins(Insets.vertical(6))
        );

        popup.child(
                UIComponents.button(Component.literal("Create new material list"), btn -> {
                            Minecraft.getInstance().gui.setScreen(parent);
                            parent.doImport();
                        }).sizing(Sizing.fill(100), Sizing.fixed(20))
                        .margins(Insets.bottom(4))
        );

        popup.child(
                UIComponents.button(Component.literal("Cancel"), btn ->
                        Minecraft.getInstance().gui.setScreen(parent)
                ).sizing(Sizing.fill(100), Sizing.fixed(20))
        );

        rootComponent.child(popup);
    }
}
