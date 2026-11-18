package com.glisco.numismaticoverhaul.attachment;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public final class ModAttachments {

    public static final AttachmentType<Long> PURSE_BALANCE = AttachmentRegistry.create(
            NumismaticOverhaul.id("currency"),
            builder -> builder
                    .initializer(() -> 0L)
                    .persistent(Codec.LONG)
                    .copyOnDeath()
    );

    public static void initialize() {
        NumismaticOverhaul.LOGGER.info("[ModAttachments] Аттачмент кошелька зарегистрирован.");
    }
}