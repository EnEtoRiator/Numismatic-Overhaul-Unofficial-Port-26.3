package com.glisco.numismaticoverhaul.config;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class NumismaticConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "numismatic_overhaul.json"
    );

    public boolean enableVillagerTrading = true;
    public long emeraldExchangeRate = 0;
    public boolean generateCurrencyInChests = true;
    public MoneyMessageLocation moneyMessageLocation = MoneyMessageLocation.ACTIONBAR;
    public boolean scaleOnHealth = false;
    public float healthScaleReduction = 1.0f;

    public PurseOffsets purseOffsets = new PurseOffsets();
    public LootOptions lootOptions = new LootOptions();
    public Map<String, Integer> mobsToBaseValues = new LinkedHashMap<>(
            Map.of("#numismatic_overhaul:the_bourgeoisie", 75)
    );

    public static class PurseOffsets {
        public int survivalX = 0;
        public int survivalY = 0;
        public int creativeX = 0;
        public int creativeY = 0;
        public int merchantX = 0;
        public int merchantY = 0;
        public int pawnShopX = 0;
        public int pawnShopY = 0;
    }

    public static class LootOptions {
        public int desertMinLoot = 300;
        public int desertMaxLoot = 1200;
        public int dungeonMinLoot = 500;
        public int dungeonMaxLoot = 2000;
        public int structureMinLoot = 1500;
        public int structureMaxLoot = 4000;
        public int strongholdLibraryMinLoot = 2000;
        public int strongholdLibraryMaxLoot = 6000;
    }

    public enum MoneyMessageLocation {
        ACTIONBAR,
        CHAT,
        DISABLED
    }

    private static NumismaticConfig INSTANCE;

    public static NumismaticConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static NumismaticConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                NumismaticConfig loaded = GSON.fromJson(reader, NumismaticConfig.class);
                if (loaded != null) {
                    INSTANCE = loaded;
                    return loaded;
                }
            } catch (Exception e) {
                NumismaticOverhaul.LOGGER.error("Не удалось прочитать config/numismatic_overhaul.json, используются значения по умолчанию.", e);
            }
        }
        NumismaticConfig defaults = new NumismaticConfig();
        defaults.save();
        INSTANCE = defaults;
        return defaults;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            NumismaticOverhaul.LOGGER.error("Ошибка сохранения config/numismatic_overhaul.json", e);
        }
    }
}