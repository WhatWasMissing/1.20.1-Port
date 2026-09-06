package matteroverdrive.worldgen;

import com.mojang.serialization.Codec;
import matteroverdrive.entity.DroneEntity;
import matteroverdrive.entity.RangedRogueAndroidEntity;
import matteroverdrive.entity.RogueAndroidEntity;
import matteroverdrive.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Population correction layer for the recovered 1.12.2 structure generators.
 * Geometry remains handled by ModernizedStructureFeature, while this class removes
 * occupants that were added by the early 1.20.1 reconstruction but are absent from
 * the legacy generation hooks and restores the Android House's exact population rule.
 */
public final class SourcePopulationStructureFeature extends Feature<NoneFeatureConfiguration> {
    private final LegacyParityStructureFeature.Kind kind;
    private final ModernizedStructureFeature delegate;

    public SourcePopulationStructureFeature(Codec<NoneFeatureConfiguration> codec,
                                            LegacyParityStructureFeature.Kind kind) {
        super(codec);
        this.kind = kind;
        this.delegate = new ModernizedStructureFeature(codec, kind);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        ServerLevel server = context.level().getLevel();
        AABB bounds = populationBounds(context.origin());
        Set<UUID> before = new HashSet<>();
        for (Entity entity : server.getEntities(null, bounds)) before.add(entity.getUUID());

        if (!delegate.place(context)) return false;

        var created = server.getEntities(null, bounds, entity -> !before.contains(entity.getUUID()));
        switch (kind) {
            case CRASHED_SHIP, CARGO_SHIP, UNDERWATER_BASE, SAND_PIT -> {
                // The authoritative 1.12.2 onGeneration hooks do not add combat mobs here.
                for (Entity entity : created) {
                    if (entity instanceof RogueAndroidEntity || entity instanceof DroneEntity) entity.discard();
                }
            }
            case ANDROID_HOUSE -> restoreAndroidHouse(context.level(), context.origin(), context.random(), created);
            case MAD_SCIENTIST_HOUSE -> {
                // The scientist house is a separate legacy village-piece path; retain its recovered population.
            }
        }
        return true;
    }

    private static void restoreAndroidHouse(WorldGenLevel level, BlockPos origin, RandomSource random,
                                            java.util.List<Entity> created) {
        int ordinary = 0;
        for (Entity entity : created) {
            if (entity instanceof DroneEntity) {
                // No Drone is created by MOAndroidHouseBuilding.onGeneration in 1.12.2.
                entity.discard();
            } else if (entity instanceof RogueAndroidEntity) {
                ordinary++;
            }
        }

        // The reconstruction produces 3-4 defenders. Promoting one third of houses to
        // five defenders converts that distribution to the legacy-uniform 3/4/5 split.
        if (random.nextInt(3) == 0) {
            int extras = Math.max(0, 5 - ordinary);
            BlockPos base = origin.below(2);
            for (int i = 0; i < extras; i++) {
                spawnOrdinaryAndroid(level, base.offset(-3, 1 + ordinary + i, 0), random);
            }
        }

        // Legacy template relative (12,4,10) maps to (+2,+4,0) around the centred 21x21 port layout.
        spawnLegendaryAndroid(level, origin.below(2).offset(2, 4, 0), random);
    }

    private static void spawnOrdinaryAndroid(WorldGenLevel level, BlockPos pos, RandomSource random) {
        // Legacy MOAndroidHouseBuilding uses 60% ranged / 40% melee.
        EntityType<? extends Mob> type = random.nextFloat() < 0.60F
                ? ModEntities.RANGED_ROGUE_ANDROID.get()
                : ModEntities.ROGUE_ANDROID.get();
        spawnMob(level, pos, type, random, false);
    }

    private static void spawnLegendaryAndroid(WorldGenLevel level, BlockPos pos, RandomSource random) {
        Mob mob = ModEntities.RANGED_ROGUE_ANDROID.get().create(level.getLevel());
        if (!(mob instanceof RangedRogueAndroidEntity android)) return;
        if (!prepare(level, pos, random, android)) return;

        CompoundTag rank = new CompoundTag();
        android.addAdditionalSaveData(rank);
        rank.putInt("AndroidLevel", 3);
        rank.putBoolean("Legendary", true);
        android.readAdditionalSaveData(rank);
        android.setPersistenceRequired();
        level.addFreshEntity(android);
    }

    private static void spawnMob(WorldGenLevel level, BlockPos pos, EntityType<? extends Mob> type,
                                 RandomSource random, boolean persistent) {
        Mob mob = type.create(level.getLevel());
        if (mob == null || !prepare(level, pos, random, mob)) return;
        if (persistent) mob.setPersistenceRequired();
        level.addFreshEntity(mob);
    }

    private static boolean prepare(WorldGenLevel level, BlockPos pos, RandomSource random, Mob mob) {
        if (!level.getWorldBorder().isWithinBounds(pos)) {
            mob.discard();
            return false;
        }
        mob.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
        if (!level.noCollision(mob)) {
            mob.discard();
            return false;
        }
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
        return true;
    }

    private AABB populationBounds(BlockPos origin) {
        return switch (kind) {
            case CRASHED_SHIP -> new AABB(origin.offset(-7, -4, -19), origin.offset(7, 12, 19));
            case CARGO_SHIP -> new AABB(origin.offset(-32, -4, -14), origin.offset(32, 18, 14));
            case UNDERWATER_BASE -> new AABB(origin.offset(-24, -6, -24), origin.offset(24, 16, 24));
            case ANDROID_HOUSE -> new AABB(origin.offset(-13, -5, -13), origin.offset(13, 12, 13));
            case SAND_PIT -> new AABB(origin.offset(-15, -14, -15), origin.offset(15, 8, 15));
            case MAD_SCIENTIST_HOUSE -> new AABB(origin.offset(-7, -3, -7), origin.offset(7, 10, 7));
        };
    }
}
