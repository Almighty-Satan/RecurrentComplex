/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.selector;

import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.reccomplex.world.gen.feature.structure.StructureInfo;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.StructureGenerationInfo;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by lukas on 24.09.16.
 */
public class MixingStructureSelector<T extends StructureGenerationInfo & EnvironmentalSelection<C>, C extends MixingStructureSelector.Category> extends StructureSelector<T, C>
{
    public MixingStructureSelector(Collection<StructureInfo> structures, WorldProvider provider, Biome biome, Class<T> typeClass)
    {
        super(structures, provider, biome, typeClass);
    }

    public float generationChance(C category, WorldProvider worldProvider, Biome biome, Float distanceToSpawn)
    {
        if (category != null)
            return category.structureSpawnChance(biome, worldProvider, weightedStructureInfos.get(category).size(), distanceToSpawn);

        return 0.0f;
    }

    public List<Pair<StructureInfo, T>> generatedStructures(Random random, Biome biome, WorldProvider provider, Float distanceToSpawn)
    {
        return weightedStructureInfos.keySet().stream()
                .filter(category -> random.nextFloat() < generationChance(category, provider, biome, distanceToSpawn))
                .map(category -> WeightedSelector.select(random, weightedStructureInfos.get(category)))
                .collect(Collectors.toList());
    }

    @Nullable
    public Pair<StructureInfo, T> selectOne(Random random, WorldProvider provider, Biome biome, @Nullable C c, Float distanceToSpawn)
    {
        if (c != null)
            return super.selectOne(random, c);

        List<WeightedSelector.SimpleItem<C>> list = weightedStructureInfos.keySet().stream()
                .map(category -> new WeightedSelector.SimpleItem<>(generationChance(category, provider, biome, distanceToSpawn), category))
                .collect(Collectors.toList());

        return selectOne(random, WeightedSelector.select(random, list));
    }

    interface Category
    {
        float structureSpawnChance(Biome biome, WorldProvider worldProvider, int registeredStructures, Float distanceToSpawn);
    }
}
