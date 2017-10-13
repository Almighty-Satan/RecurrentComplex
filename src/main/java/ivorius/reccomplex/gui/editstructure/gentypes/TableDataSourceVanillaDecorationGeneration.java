/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.RCGuiTables;
import ivorius.reccomplex.gui.TableDataSourceBlockPos;
import ivorius.reccomplex.gui.editstructure.TableDataSourceBiomeGenList;
import ivorius.reccomplex.gui.editstructure.TableDataSourceDimensionGenList;
import ivorius.reccomplex.gui.table.TableCells;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellEnum;
import ivorius.reccomplex.gui.table.cell.TableCellMultiBuilder;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.decoration.RCBiomeDecorator;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.VanillaDecorationGeneration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 07.10.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceVanillaDecorationGeneration extends TableDataSourceSegmented
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private VanillaDecorationGeneration generationInfo;

    public TableDataSourceVanillaDecorationGeneration(TableNavigator navigator, TableDelegate delegate, VanillaDecorationGeneration generationInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = delegate;
        this.generationInfo = generationInfo;

        addSegment(0, new TableDataSourceGenerationType(generationInfo, navigator, delegate));

        addSegment(1, () -> {
            TableCellEnum<RCBiomeDecorator.DecorationType> cell = new TableCellEnum<>("type", generationInfo.type, TableCellEnum.options(RCBiomeDecorator.DecorationType.values(), "reccomplex.generationInfo.decoration.types.", true));
            cell.addListener(v -> generationInfo.type = v);
            return new TitledCell(IvTranslations.get("reccomplex.generationInfo.vanilla.type"), cell);
        });

        addSegment(2, () -> {
            return RCGuiTables.defaultWeightElement(val -> generationInfo.generationWeight = TableCells.toDouble(val), generationInfo.generationWeight);
        });

        TableCellMultiBuilder tableCellMultiBuilder1 = TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceBiomeGenList(generationInfo.biomeWeights, delegate, navigator));
        addSegment(3, tableCellMultiBuilder1.withTitle(IvTranslations.get("reccomplex.gui.biomes")).buildDataSource());

        TableCellMultiBuilder tableCellMultiBuilder = TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceDimensionGenList(generationInfo.dimensionWeights, delegate, navigator));
        addSegment(4, tableCellMultiBuilder.withTitle(IvTranslations.get("reccomplex.gui.dimensions")).buildDataSource());

        addSegment(5, new TableDataSourceBlockPos(generationInfo.spawnShift, generationInfo::setSpawnShift,
                IvTranslations.get("reccomplex.gui.blockpos.shift"), IvTranslations.getLines("reccomplex.gui.blockpos.shift.tooltip")));
    }
}
