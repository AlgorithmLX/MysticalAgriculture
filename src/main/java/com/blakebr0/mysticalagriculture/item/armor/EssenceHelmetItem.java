package com.blakebr0.mysticalagriculture.item.armor;

import com.blakebr0.cucumber.item.BaseArmorItem;
import com.blakebr0.mysticalagriculture.api.tinkering.AugmentType;
import com.blakebr0.mysticalagriculture.api.tinkering.ITinkerable;
import com.blakebr0.mysticalagriculture.api.util.AugmentUtils;
import com.blakebr0.mysticalagriculture.config.ModConfigs;
import com.blakebr0.mysticalagriculture.init.ModItems;
import com.blakebr0.mysticalagriculture.lib.ModTooltips;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class EssenceHelmetItem extends BaseArmorItem implements ITinkerable {
    private static final UUID[] ARMOR_MODIFIERS = new UUID[] { UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150") };
    private static final EnumSet<AugmentType> TYPES = EnumSet.of(AugmentType.ARMOR, AugmentType.HELMET);
    private final int tinkerableTier;
    private final int slots;

    public EssenceHelmetItem(ArmorMaterial material, int tinkerableTier, int slots) {
        super(material, Type.HELMET);
        this.tinkerableTier = tinkerableTier;
        this.slots = slots;
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        AugmentUtils.getAugments(stack).forEach(a -> a.onArmorTick(stack, level, player));
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        if (stack.hurt(amount, entity.getRandom(), entity instanceof ServerPlayer ? (ServerPlayer) entity : null)) {
            onBroken.accept(entity);

            AugmentUtils.getAugments(stack).forEach(augment -> {
                Block.popResource(entity.level(), entity.getOnPos(), new ItemStack(augment.getItem()));
            });

            stack.shrink(1);

            if (entity instanceof Player player) {
                player.awardStat(Stats.ITEM_BROKEN.get(this));
            }

            stack.setDamageValue(0);
        }

        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(ModTooltips.getTooltipForTier(this.tinkerableTier));

        if (ModConfigs.AWAKENED_SUPREMIUM_SET_BONUS.get() && stack.is(ModItems.AWAKENED_SUPREMIUM_CHESTPLATE.get())) {
            tooltip.add(ModTooltips.SET_BONUS.args(ModTooltips.AWAKENED_SUPREMIUM_SET_BONUS.build()).build());
        }

        AugmentUtils.getAugments(stack).forEach(a -> {
            tooltip.add(a.getDisplayName().withStyle(ChatFormatting.GRAY));
        });
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();

        if (slot == this.type.getSlot()) {
            var material = this.getMaterial();

            modifiers.put(Attributes.ARMOR, new AttributeModifier(ARMOR_MODIFIERS[slot.getIndex()], "Armor modifier", material.getDefenseForType(this.type), AttributeModifier.Operation.ADDITION));
            modifiers.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(ARMOR_MODIFIERS[slot.getIndex()], "Armor toughness", material.getToughness(), AttributeModifier.Operation.ADDITION));

            if (material.getKnockbackResistance() > 0) {
                modifiers.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(ARMOR_MODIFIERS[slot.getIndex()], "Armor knockback resistance", material.getKnockbackResistance(), AttributeModifier.Operation.ADDITION));
            }

            AugmentUtils.getAugments(stack).forEach(a -> {
                a.addArmorAttributeModifiers(modifiers, slot, stack);
            });
        }

        return modifiers;
    }

    @Override
    public int getAugmentSlots() {
        return this.slots;
    }

    @Override
    public EnumSet<AugmentType> getAugmentTypes() {
        return TYPES;
    }

    @Override
    public int getTinkerableTier() {
        return this.tinkerableTier;
    }
}
