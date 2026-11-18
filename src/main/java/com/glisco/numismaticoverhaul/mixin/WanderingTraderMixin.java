package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.config.NumismaticConfig;
import com.glisco.numismaticoverhaul.villagers.VillagerTradesLoader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderMixin extends AbstractVillager {

    protected WanderingTraderMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
    private void numismatic$onUpdateTrades(ServerLevel level, CallbackInfo ci) {
        if (!NumismaticConfig.get().enableVillagerTrading) return;
        if (!VillagerTradesLoader.hasTrades(VillagerTradesLoader.WANDERING_TRADER_ID)) return;

        List<VillagerTradesLoader.TradeFactory> common = VillagerTradesLoader.getTrades(VillagerTradesLoader.WANDERING_TRADER_ID, 1);
        List<VillagerTradesLoader.TradeFactory> rare = VillagerTradesLoader.getTrades(VillagerTradesLoader.WANDERING_TRADER_ID, 2);

        VillagerTradesLoader.populateOffers(level, this.getOffers(), common, this, this.random, 5);
        VillagerTradesLoader.populateOffers(level, this.getOffers(), rare, this, this.random, 1);
        ci.cancel();
    }

    @Inject(method = "updateTrades", at = @At("RETURN"))
    private void numismatic$convertFallbackTrades(ServerLevel level, CallbackInfo ci) {
        if (!NumismaticConfig.get().enableVillagerTrading) return;
        VillagerTradesLoader.convertEmeraldOffers(this.getOffers());
    }
}