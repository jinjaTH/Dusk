package com.example.unlock;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ReachAttributeHandler {

    private static final ResourceLocation REACH_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(UnlockMod.MOD_ID, "reach_extension");

    private static final double REACH_BONUS = 5.5; // brings survival 4.5 → 10

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            applyReach(handler.player);
        });
    }

    public static void applyReach(ServerPlayer player) {
        if (!UnlockConfig.get().enableReach) return;
        applyToAttribute(player, Attributes.BLOCK_INTERACTION_RANGE);
        applyToAttribute(player, Attributes.ENTITY_INTERACTION_RANGE);
    }

    public static void removeReach(ServerPlayer player) {
        removeFromAttribute(player, Attributes.BLOCK_INTERACTION_RANGE);
        removeFromAttribute(player, Attributes.ENTITY_INTERACTION_RANGE);
    }

    private static void applyToAttribute(Player player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        if (instance.getModifier(REACH_MODIFIER_ID) != null) return; // already applied
        instance.addPermanentModifier(new AttributeModifier(
            REACH_MODIFIER_ID,
            REACH_BONUS,
            AttributeModifier.Operation.ADD_VALUE
        ));
    }

    private static void removeFromAttribute(Player player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(REACH_MODIFIER_ID);
    }
}
