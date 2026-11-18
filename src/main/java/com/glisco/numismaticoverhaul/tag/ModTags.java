package com.glisco.numismaticoverhaul.tag;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

public final class ModTags {

    public static final TagKey<Block> PIGGY_BANKS = TagKey.create(
            Registries.BLOCK, NumismaticOverhaul.id("piggy_banks")
    );

    public static final TagKey<Block> VERY_HEAVY_BLOCKS = TagKey.create(
            Registries.BLOCK, NumismaticOverhaul.id("very_heavy_blocks")
    );

    public static final TagKey<EntityType<?>> THE_BOURGEOISIE = TagKey.create(
            Registries.ENTITY_TYPE, NumismaticOverhaul.id("the_bourgeoisie")
    );
}