/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.placement.rays;

import com.google.gson.annotations.SerializedName;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.WorldCache;
import ivorius.reccomplex.structures.generic.placement.FactorLimit;
import ivorius.reccomplex.structures.generic.placement.StructurePlaceContext;

/**
 * Created by lukas on 19.09.16.
 */
public class RayDynamicPosition extends FactorLimit.Ray
{
    public Type type;

    public RayDynamicPosition()
    {
        this(null, Type.BEDROCK);
    }

    public RayDynamicPosition(Float weight, Type type)
    {
        super(weight);
        this.type = type;
    }

    @Override
    public int cast(WorldCache cache, StructurePlaceContext context, int y)
    {
        switch (type)
        {
            case BEDROCK:
                return 0;
            case SEALEVEL:
                return cache.world.getSeaLevel();
            case WORLD_HEIGHT:
                return cache.world.getHeight() - 1;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public String displayString()
    {
        return IvTranslations.get("reccomplex.placer.factors.limit.rays.dynpos.type." + IvGsonHelper.serializedName(type));
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceSegmented(rayTableDataSource(navigator, delegate), new TableDataSourceSupplied(() ->
        {
            TableCellEnum<Type> cell = new TableCellEnum<>(null, type, TableCellEnum.options(Type.values(), "reccomplex.placer.factors.limit.rays.dynpos.type.", true));
            cell.addPropertyConsumer(v -> type = v);
            return new TableElementCell(IvTranslations.get("reccomplex.placer.factors.limit.rays.dynpos.type"), cell);
        }));
    }

    public enum Type
    {
        @SerializedName("bedrock")
        BEDROCK,
        @SerializedName("sealevel")
        SEALEVEL,
        @SerializedName("world_height")
        WORLD_HEIGHT
    }
}
