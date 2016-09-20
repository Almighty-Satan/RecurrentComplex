/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.placement;

import com.google.common.math.DoubleMath;
import com.google.gson.*;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.editstructure.placer.TableDataSourceFactorMatch;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.generic.WorldCache;
import ivorius.reccomplex.structures.generic.matchers.BlockMatcher;
import ivorius.reccomplex.structures.generic.matchers.PositionedBlockMatcher;
import ivorius.reccomplex.utils.IntegerRanges;
import ivorius.reccomplex.utils.LineSelection;
import ivorius.reccomplex.utils.RCStreams;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lukas on 18.09.16.
 */
public class FactorMatch extends GenericPlacer.Factor
{
    public BlockMatcher sourceMatcher;
    public PositionedBlockMatcher destMatcher;

    public float requiredConformity;

    public FactorMatch()
    {
        this(1, "", "", .5f);
    }

    public FactorMatch(float priority, String sourceExpression, String destExpression, float requiredConformity)
    {
        super(priority);
        this.sourceMatcher = new BlockMatcher(RecurrentComplex.specialRegistry, sourceExpression);
        this.destMatcher = new PositionedBlockMatcher(RecurrentComplex.specialRegistry, destExpression);

        this.requiredConformity = requiredConformity;
    }

    protected float weight(WorldCache cache, Set<BlockPos> sources, int y, float needed)
    {
        int[] chances = new int[]{(int) (sources.size() * needed)};
        int[] matched = new int[]{0};

        RCStreams.visit(sources.stream(), pos ->
        {
            if (destMatcher.test(PositionedBlockMatcher.Argument.at(cache, pos.up(y))))
            {
                matched[0]++;
                return true;
            }
            else
                return --chances[0] > 0; // Already lost
        });

        return chances[0] > 0 ? (float) matched[0] / sources.size() : 0;
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceFactorMatch(this, delegate, navigator);
    }

    @Override
    public List<Pair<LineSelection, Float>> consider(WorldCache cache, LineSelection considerable, @Nullable IvBlockCollection blockCollection, StructurePlaceContext context)
    {
        if (blockCollection == null)
            throw new IllegalArgumentException("Missing a block collection!");

        List<Pair<LineSelection, Float>> consideration = new ArrayList<>();

        int[] size = context.boundingBoxSize();
        BlockPos lowerCoord = context.lowerCoord();
        Set<BlockPos> sources = blockCollection.area().stream()
                .filter(p -> sourceMatcher.test(blockCollection.getBlockState(p)))
                .map(p -> context.transform.apply(p, size).add(lowerCoord.getX(), 0, lowerCoord.getZ()))
                .collect(Collectors.toSet());

        for (IntegerRange range : (Iterable<IntegerRange>) considerable.streamSections(null, true)::iterator)
        {
            Float curConformity = null;
            int lastY = range.getMax();
            int end = range.getMin();

            for (int y = lastY; y >= end; y--)
            {
                float conformity = weight(cache, sources, y, requiredConformity);

                if (curConformity == null)
                {
                    curConformity = conformity;
                    lastY = y;
                }
                else if (!DoubleMath.fuzzyEquals(conformity, curConformity, 0.01))
                {
                    consideration.add(Pair.of(LineSelection.fromRange(IntegerRanges.from(lastY, y), true), weight(curConformity)));
                    curConformity = conformity;
                }
            }

            if (curConformity != null)
                consideration.add(Pair.of(LineSelection.fromRange(IntegerRanges.from(lastY, end), true), weight(curConformity)));
        }

        return consideration;
    }

    public static class Serializer implements JsonSerializer<FactorMatch>, JsonDeserializer<FactorMatch>
    {
        @Override
        public FactorMatch deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(json, "factorMatch");

            float priority = JsonUtils.getFloat(jsonObject, "priority", 1);

            String sourceExpression = JsonUtils.getString(jsonObject, "sourceExpression", "");
            String destExpression = JsonUtils.getString(jsonObject, "destExpression", "");

            float requiredConformity = JsonUtils.getFloat(jsonObject, "requiredConformity", 0);

            return new FactorMatch(priority, sourceExpression, destExpression, requiredConformity);
        }

        @Override
        public JsonElement serialize(FactorMatch src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("priority", src.priority);

            jsonObject.addProperty("sourceExpression", src.sourceMatcher.getExpression());
            jsonObject.addProperty("destExpression", src.destMatcher.getExpression());

            jsonObject.addProperty("requiredConformity", src.requiredConformity);

            return jsonObject;
        }
    }
}
