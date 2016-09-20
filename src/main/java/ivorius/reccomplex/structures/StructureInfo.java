/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures;

import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
import ivorius.reccomplex.utils.NBTStorable;
import net.minecraft.nbt.NBTBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 24.05.14.
 */
public interface StructureInfo<S extends NBTStorable>
{
    boolean generate(@Nonnull StructureSpawnContext context, @Nonnull S instanceData);

    @Nonnull
    S prepareInstanceData(@Nonnull StructurePrepareContext context);

    @Nonnull
    S loadInstanceData(@Nonnull StructureLoadContext context, @Nonnull NBTBase nbt);

    @Nonnull
    <I extends StructureGenerationInfo> List<I> generationInfos(@Nonnull Class<? extends I> clazz);

    StructureGenerationInfo generationInfo(@Nonnull String id);

    @Nonnull
    int[] structureBoundingBox();

    boolean isRotatable();

    boolean isMirrorable();

    @Nullable
    GenericStructureInfo copyAsGenericStructureInfo();

    boolean areDependenciesResolved();

    @Nullable
    IvBlockCollection blockCollection();
}
