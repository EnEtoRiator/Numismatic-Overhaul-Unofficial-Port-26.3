package com.glisco.numismaticoverhaul.loot;

import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;
import java.util.function.Consumer;

public class MoneyBagLootEntry extends LootPoolEntryContainer {

    public static final MapCodec<MoneyBagLootEntry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("min", 0).forGetter(entry -> entry.min),
            Codec.INT.fieldOf("max").forGetter(entry -> entry.max)
    ).and(commonFields(instance)).apply(instance, MoneyBagLootEntry::new));

    private final int min;
    private final int max;

    private MoneyBagLootEntry(
            int min,
            int max,
            Optional<Holder<LootItemCondition>> condition,
            Optional<Holder<LootItemFunction>> modifier
    ) {
        super(condition, modifier);
        this.min = min;
        this.max = max;
    }

    @Override
    protected boolean expandRaw(LootContext context, Consumer<LootPoolEntry> output) {
        output.accept(new LootPoolEntry() {
            @Override
            public int getWeight(float luck) {
                return 1;
            }

            @Override
            public void createItemStack(Consumer<ItemStack> stackConsumer, LootContext lootContext) {
                int value = Mth.nextInt(lootContext.getRandom(), MoneyBagLootEntry.this.min, MoneyBagLootEntry.this.max);
                if (value > 0) {
                    stackConsumer.accept(MoneyBagItem.fromRawValue(value));
                }
            }
        });
        return true;
    }

    @Override
    public MapCodec<? extends LootPoolEntryContainer> codec() {
        return CODEC;
    }

    public static LootPoolEntryContainer.Builder<?> builder(int min, int max) {
        return new Builder(min, max);
    }

    public static class Builder extends LootPoolEntryContainer.Builder<Builder> {
        private final int min;
        private final int max;

        public Builder(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public LootPoolEntryContainer build() {
            return new MoneyBagLootEntry(this.min, this.max, this.getCondition(), this.getModifier());
        }
    }
}