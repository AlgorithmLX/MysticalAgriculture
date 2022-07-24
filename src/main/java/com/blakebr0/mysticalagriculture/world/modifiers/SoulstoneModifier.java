package com.blakebr0.mysticalagriculture.world.modifiers;

import com.blakebr0.mysticalagriculture.config.ModConfigs;
import com.blakebr0.mysticalagriculture.init.ModBiomeModifiers;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.registries.ForgeRegistries;

public record SoulstoneModifier(HolderSet<Biome> biomes, Holder<PlacedFeature> feature) implements BiomeModifier {
    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD && ModConfigs.GENERATE_SOULSTONE.get() && this.biomes.contains(biome)) {
            var id = ForgeRegistries.BIOMES.getKey(biome.value());
            var whitelist = ModConfigs.SOULSTONE_BIOME_WHITELIST.get();

            if (id == null || whitelist.isEmpty() || !whitelist.contains(id.toString()))
                return;

            builder.getGenerationSettings().addFeature(GenerationStep.Decoration.RAW_GENERATION, this.feature);
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return ModBiomeModifiers.SOULSTONE.get();
    }
}