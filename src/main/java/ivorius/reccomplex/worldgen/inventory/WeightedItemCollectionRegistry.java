/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import ivorius.reccomplex.files.SimpleLeveledRegistry;

/**
 * Created by lukas on 25.05.14.
 */
public class WeightedItemCollectionRegistry extends SimpleLeveledRegistry<WeightedItemCollection>
{
    public static WeightedItemCollectionRegistry INSTANCE = new WeightedItemCollectionRegistry();

    public WeightedItemCollectionRegistry()
    {
        super("weighted item collection");
    }
}
