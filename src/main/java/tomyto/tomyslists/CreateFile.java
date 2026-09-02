package tomyto.tomyslists;

import fi.dy.masa.litematica.materials.MaterialListBase;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.litematica.materials.MaterialListSorter;
import fi.dy.masa.malilib.data.DataDump;

import java.nio.file.Path;
import java.util.ArrayList;

public class CreateFile {

    public static Path writeToFile(MaterialListBase materialList, Path outputDir, String fileName) {
        DataDump dump = getMaterialListDump(materialList);
        return DataDump.dumpDataToFile(outputDir, fileName, ".txt", dump.getLines());
    }

    private static DataDump getMaterialListDump(MaterialListBase materialList) {
        DataDump dump = new DataDump(4, DataDump.Format.ASCII);
        int multiplier = materialList.getMultiplier();

        //Sort from most amount to least
        materialList.setSortCriteria(MaterialListBase.SortCriteria.COUNT_TOTAL);

        ArrayList<MaterialListEntry> list = new ArrayList<>(materialList.getMaterialsFiltered(false));
        list.sort(new MaterialListSorter(materialList));

        for (MaterialListEntry entry : list) {
            int total = entry.getCountTotal() * multiplier;
            int missing = multiplier > 1 ? total : entry.getCountMissing();
            int available = entry.getCountAvailable();
            dump.addData(
                    entry.getStack().getHoverName().getString(),
                    String.valueOf(total),
                    String.valueOf(missing),
                    String.valueOf(available)
            );
        }

        String titleTotal = multiplier > 1 ? String.format("Total (x%d)", multiplier) : "Total";
        dump.addTitle("Item", titleTotal, "Missing", "Available");
        dump.addHeader(materialList.getTitle());
        dump.setColumnProperties(1, DataDump.Alignment.RIGHT, true);
        dump.setColumnProperties(2, DataDump.Alignment.RIGHT, true);
        dump.setColumnProperties(3, DataDump.Alignment.RIGHT, true);
        dump.setSort(false);
        dump.setUseColumnSeparator(true);

        return dump;
    }
}
