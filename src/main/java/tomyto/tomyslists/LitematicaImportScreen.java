package tomyto.tomyslists;

import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.materials.MaterialListSchematic;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LitematicaImportScreen extends BaseOwoScreen<FlowLayout> {

    private final Path schematicsFolder;
    private final Path configFolder;
    private String selectedFile = null;
    private FlowLayout scrollContent;
    private Path selectedFilePath = null;

    public LitematicaImportScreen() {
        super(Component.literal("Import from Litematica"));
        this.schematicsFolder = Minecraft.getInstance().gameDirectory.toPath().resolve("schematics");
        this.configFolder = Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config").resolve("litematica");
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
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(10));

        rootComponent.child(
                UIComponents.label(Component.literal("Select a litematica file"))
                        .horizontalTextAlignment(HorizontalAlignment.LEFT)
                        .margins(Insets.bottom(8))
        );

        scrollContent = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());
        scrollContent.padding(Insets.both(5,5));

        loadFileList();

        rootComponent.child(
                UIContainers.verticalScroll(Sizing.fill(90), Sizing.fill(75), scrollContent)
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .surface(Surface.DARK_PANEL)
                        .margins(Insets.both(5,5))
        );

        // Bottom bar
        rootComponent.child(
                UIContainers.horizontalFlow(Sizing.fill(90), Sizing.fixed(24))
                        .child(
                                UIComponents.button(Component.literal("Back"), btn ->
                                        Minecraft.getInstance().gui.setScreen(new MaterialListScreen())
                                ).sizing(Sizing.fill(20), Sizing.fixed(20))
                        )
                        .child(
                                UIComponents.button(Component.literal("Open specific txt file"), btn ->
                                        Minecraft.getInstance().gui.setScreen(new MaterialListScreen())
                                ).sizing(Sizing.content(), Sizing.fixed(20))
                        )

                        .child(UIContainers.horizontalFlow(Sizing.expand(), Sizing.fill(100))
                                .child(
                                        UIComponents.button(Component.literal("Import selected"), btn ->
                                                importSelected()
                                        ).sizing(Sizing.fill(30), Sizing.fixed(20))
                                )
                                .horizontalAlignment(HorizontalAlignment.RIGHT)
                                .verticalAlignment(VerticalAlignment.CENTER)
                        )
        );
    }

    private void loadFileList() {
        scrollContent.clearChildren();

        List<Path> files = getLitematicaFiles();

        if (files.isEmpty()) {
            scrollContent.child(
                    UIComponents.label(Component.literal("No litematica files found"))
                            .margins(Insets.of(10))
            );
            return;
        }

        for (Path file : files) {
            String relativePath = schematicsFolder.relativize(file).toString().replace(".litematic", "");
            String displayName = file.getFileName().toString().replace(".litematic", "");
            String folder = file.getParent().equals(schematicsFolder) ? ""
                    : schematicsFolder.relativize(file.getParent()).toString();

            FlowLayout row = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));

            // Show folder in gray if in a subfolder
            if (!folder.isEmpty()) {
                row.child(
                        UIComponents.label(Component.literal(folder + "/").withStyle(style -> style.withColor(0x888888)))
                                .sizing(Sizing.content(), Sizing.content())
                                .margins(Insets.both(5, 4))
                );
            }

            row.child(
                    UIComponents.label(Component.literal(displayName))
                            .sizing(Sizing.fill(100), Sizing.content())
                            .margins(Insets.both(folder.isEmpty() ? 5 : 0, 4))
            );

            row.mouseDown().subscribe((x, y) -> {
                selectedFile = displayName;
                selectedFilePath = file; // store full path
                for (var child : scrollContent.children()) {
                    ((FlowLayout) child).surface(Surface.flat(0x44FFFFFF));
                }
                row.surface(Surface.flat(0x884499FF));
                return true;
            });

            row.surface(Surface.flat(0x44FFFFFF));
            row.margins(Insets.both(5, 2));
            scrollContent.child(row);
        }
    }

    private List<Path> getLitematicaFiles() {
        try {
            if (!Files.exists(schematicsFolder)) return List.of();
            return Files.walk(schematicsFolder)
                    .filter(p -> p.getFileName().toString().endsWith(".litematic"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private void importSelected() {
        if (selectedFilePath == null) return;
        List<Path> existing = findExistingTxtFiles(selectedFile);
        if (!existing.isEmpty()) {
            Minecraft.getInstance().gui.setScreen(new ExistingFilesPopupScreen(this, existing, selectedFile));
        } else {
            doImport();
        }
    }

    private void showExistingFilesPopup(List<Path> existingFiles) {
        Minecraft.getInstance().gui.setScreen(new ExistingFilesPopupScreen(this, existingFiles, selectedFile));
    }

    public void doImport() {
        if (selectedFilePath == null) return;

        try {
            LitematicaSchematic schematic = LitematicaSchematic.createFromFile(
                    selectedFilePath.getParent(),
                    selectedFilePath.getFileName().toString()
            );

            if (schematic == null) {
                System.out.println("Failed to load schematic: " + selectedFile);
                return;
            }
            MaterialListSchematic materialList = new MaterialListSchematic(schematic, true);
            materialList.reCreateMaterialList();

            Path outputFile = CreateFile.writeToFile(materialList, configFolder, selectedFile);

            if (outputFile != null) {
                saveSelectedFile(outputFile.getFileName().toString());
                Minecraft.getInstance().gui.setScreen(new ListMainScreen());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveSelectedFile(String fileName) {
        Path configFile = configFolder.resolve("tomyslistconfig.txt");
        fileName = fileName.replace(".txt", "");
        try {
            List<String> lines;
            if (Files.exists(configFile)) {
                lines = new ArrayList<>(Files.readAllLines(configFile));
                if (lines.isEmpty()) lines.add(fileName);
                else lines.set(0, fileName);
            } else {
                lines = new ArrayList<>();
                lines.add(fileName);
            }
            Files.write(configFile, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Path> findExistingTxtFiles(String baseName) {
        try {
            if (!Files.exists(configFolder)) return List.of();
            return Files.list(configFolder)
                    .filter(p -> p.getFileName().toString().endsWith(".txt"))
                    .filter(p -> p.getFileName().toString().startsWith(baseName))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}