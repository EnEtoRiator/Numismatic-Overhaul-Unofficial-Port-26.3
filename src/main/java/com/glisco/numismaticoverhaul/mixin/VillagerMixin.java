package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.config.NumismaticConfig;
import com.glisco.numismaticoverhaul.villagers.VillagerTradesLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager {

    @Shadow
    public abstract VillagerData getVillagerData();

    protected VillagerMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
    private void numismatic$onUpdateTrades(ServerLevel level, CallbackInfo ci) {
        if (!NumismaticConfig.get().enableVillagerTrading) return;

        VillagerData data = this.getVillagerData();
        ResourceKey<VillagerProfession> key = data.profession().unwrapKey().orElse(null);
        if (key == null) return;

        Identifier profId = key.identifier();
        if (VillagerTradesLoader.hasTrades(profId)) {
            List<VillagerTradesLoader.TradeFactory> factories = VillagerTradesLoader.getTrades(profId, data.level());
            if (!factories.isEmpty()) {
                VillagerTradesLoader.populateOffers(level, this.getOffers(), factories, this, this.random, 2);
            }
            ci.cancel();
        }
    }

    @Inject(method = "updateTrades", at = @At("RETURN"))
    private void numismatic$convertFallbackTrades(ServerLevel level, CallbackInfo ci) {
        if (!NumismaticConfig.get().enableVillagerTrading) return;
        VillagerTradesLoader.convertEmeraldOffers(this.getOffers());
    }
}