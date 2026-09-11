package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.entity.DefectorAndroidEntity;
import matteroverdrive.entity.FacilityResearcherEntity;
import matteroverdrive.registry.ModEntities;
import matteroverdrive.world.StructureLoreCatalog;
import matteroverdrive.world.StructurePopulationSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Finite, structure-aware population for the exploration campaign.
 *
 * No chunks are loaded by this system. It only examines a small radius around a
 * player already standing inside a loaded structure piece, and each generated
 * structure start receives its encounter package at most once.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StructurePopulationEvents {
    private static final Map<UUID, Long> LAST_SCANNED_CELL = new HashMap<>();
    private static final int SEARCH_RADIUS = 9;
    private static final int MAX_POSITION_CHECKS = 420;

    private StructurePopulationEvents() {}

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;

        BlockPos playerPos = player.blockPosition();
        // Eight-block cells catch players walking into a structure within one chunk while
        // still avoiding a structure-manager scan every second when they stand still.
        long cell = BlockPos.asLong(playerPos.getX() >> 3, playerPos.getY() >> 3, playerPos.getZ() >> 3);
        Long previous = LAST_SCANNED_CELL.put(player.getUUID(), cell);
        if (previous != null && previous == cell) return;
        populateCurrentStructure(player);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_SCANNED_CELL.remove(event.getEntity().getUUID());
    }

    private static void populateCurrentStructure(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos playerPos = player.blockPosition();
        for (StructureLoreCatalog.LoreRecord record : StructureLoreCatalog.records()) {
            StructureStart start = level.structureManager().getStructureWithPieceAt(playerPos, record.structureKey());
            if (start == null || !start.isValid()) continue;

            String populationKey = populationKey(level, record.id(), start.getBoundingBox());
            StructurePopulationSavedData data = StructurePopulationSavedData.get(level);
            if (data.contains(populationKey)) return;

            List<BlockPos> occupied = new ArrayList<>();
            int spawned = spawnPackage(level, playerPos, record.id(), start, occupied);
            if (spawned > 0) data.mark(populationKey);
            return;
        }
    }

    private static int spawnPackage(ServerLevel level, BlockPos origin, String site,
                                    StructureStart start, List<BlockPos> occupied) {
        return switch (site) {
            case "crashed_ship" -> researcher(level, origin, start, occupied, FacilityResearcherEntity.Role.RECOVERY_SPECIALIST)
                    + hostile(level, origin, start, occupied, ModEntities.ROGUE_ANDROID.get());
            case "cargo_ship" -> researcher(level, origin, start, occupied, FacilityResearcherEntity.Role.SALVAGER)
                    + hostile(level, origin, start, occupied, ModEntities.RANGED_ROGUE_ANDROID.get());
            case "underwater_base" -> researcher(level, origin, start, occupied, FacilityResearcherEntity.Role.RECOVERY_SPECIALIST)
                    + hostile(level, origin, start, occupied, ModEntities.RESONANT_ANDROID.get());
            case "mad_scientist_house" -> researcher(level, origin, start, occupied, FacilityResearcherEntity.Role.FIELD_RESEARCHER)
                    + hostile(level, origin, start, occupied, ModEntities.MUTANT_SCIENTIST.get());
            case "android_house" -> defector(level, origin, start, occupied, DefectorAndroidEntity.Role.MORROW_SCOUT)
                    + defector(level, origin, start, occupied, DefectorAndroidEntity.Role.CHORUS_COURIER);
            case "sand_pit" -> researcher(level, origin, start, occupied, FacilityResearcherEntity.Role.SALVAGER)
                    + hostile(level, origin, start, occupied, ModEntities.ROGUE_ANDROID.get());
            case "synthetic_manufacturing_plant" -> researcher(level, origin, start, occupied, FacilityResearcherEntity.Role.FIELD_RESEARCHER)
                    + hostile(level, origin, start, occupied, ModEntities.ORPHEUS_SECURITY.get())
                    + hostile(level, origin, start, occupied, ModEntities.ORPHEUS_SECURITY.get());
            case "matter_refinery" -> researcher(level, origin, start, occupied, FacilityResearcherEntity.Role.FIELD_RESEARCHER)
                    + hostile(level, origin, start, occupied, ModEntities.RESONANT_ANDROID.get());
            case "quantum_relay_station" -> researcher(level, origin, start, occupied, FacilityResearcherEntity.Role.RECOVERY_SPECIALIST)
                    + hostile(level, origin, start, occupied, ModEntities.ORPHEUS_SECURITY.get())
                    + hostile(level, origin, start, occupied, ModEntities.RANGED_ROGUE_ANDROID.get());
            case "android_command_bunker" -> hostile(level, origin, start, occupied, ModEntities.ORPHEUS_SECURITY.get())
                    + hostile(level, origin, start, occupied, ModEntities.ORPHEUS_SECURITY.get())
                    + defector(level, origin, start, occupied, DefectorAndroidEntity.Role.MORROW_SCOUT);
            case "fusion_research_complex" -> researcher(level, origin, start, occupied, FacilityResearcherEntity.Role.ICARUS_ENGINEER)
                    + hostile(level, origin, start, occupied, ModEntities.RESONANT_ANDROID.get())
                    + hostile(level, origin, start, occupied, ModEntities.RESONANT_ANDROID.get());
            case "black_site" -> hostile(level, origin, start, occupied, ModEntities.ORPHEUS_SECURITY.get())
                    + hostile(level, origin, start, occupied, ModEntities.ORPHEUS_SECURITY.get())
                    + hostile(level, origin, start, occupied, ModEntities.ORPHEUS_SECURITY.get())
                    + hostile(level, origin, start, occupied, ModEntities.RESONANT_ANDROID.get());
            case "deep_matter_vault" -> researcher(level, origin, start, occupied, FacilityResearcherEntity.Role.ARCHIVIST)
                    + hostile(level, origin, start, occupied, ModEntities.RESONANT_ANDROID.get())
                    + hostile(level, origin, start, occupied, ModEntities.RESONANT_ANDROID.get());
            case "autonomous_drone_foundry" -> defector(level, origin, start, occupied, DefectorAndroidEntity.Role.HEPHAESTUS_LIAISON)
                    + defector(level, origin, start, occupied, DefectorAndroidEntity.Role.CHORUS_COURIER)
                    + hostile(level, origin, start, occupied, ModEntities.ORPHEUS_SECURITY.get());
            case "anomaly_quarantine_site" -> researcher(level, origin, start, occupied, FacilityResearcherEntity.Role.JANUS_MEDIC)
                    + hostile(level, origin, start, occupied, ModEntities.RESONANT_ANDROID.get())
                    + hostile(level, origin, start, occupied, ModEntities.RESONANT_ANDROID.get());
            case "orbital_recovery_array" -> researcher(level, origin, start, occupied, FacilityResearcherEntity.Role.RECOVERY_SPECIALIST)
                    + hostile(level, origin, start, occupied, ModEntities.ORPHEUS_SECURITY.get())
                    + hostile(level, origin, start, occupied, ModEntities.RESONANT_ANDROID.get());
            default -> 0;
        };
    }

    private static int researcher(ServerLevel level, BlockPos origin, StructureStart start,
                                  List<BlockPos> occupied, FacilityResearcherEntity.Role role) {
        BlockPos pos = findSafePosition(level, origin, start, occupied);
        if (pos == null) return 0;
        FacilityResearcherEntity entity = ModEntities.FACILITY_RESEARCHER.get().create(level);
        if (entity == null) return 0;
        prepare(entity, level, pos);
        entity.setRole(role);
        entity.restrictTo(pos, 10);
        level.addFreshEntity(entity);
        occupied.add(pos);
        return 1;
    }

    private static int defector(ServerLevel level, BlockPos origin, StructureStart start,
                                List<BlockPos> occupied, DefectorAndroidEntity.Role role) {
        BlockPos pos = findSafePosition(level, origin, start, occupied);
        if (pos == null) return 0;
        DefectorAndroidEntity entity = ModEntities.DEFECTOR_ANDROID.get().create(level);
        if (entity == null) return 0;
        prepare(entity, level, pos);
        entity.setRole(role);
        entity.restrictTo(pos, 10);
        level.addFreshEntity(entity);
        occupied.add(pos);
        return 1;
    }

    private static int hostile(ServerLevel level, BlockPos origin, StructureStart start,
                               List<BlockPos> occupied, EntityType<? extends PathfinderMob> type) {
        BlockPos pos = findSafePosition(level, origin, start, occupied);
        if (pos == null) return 0;
        PathfinderMob entity = type.create(level);
        if (entity == null) return 0;
        prepare(entity, level, pos);
        entity.setPersistenceRequired();
        entity.restrictTo(pos, 14);
        level.addFreshEntity(entity);
        occupied.add(pos);
        return 1;
    }

    private static void prepare(Mob entity, ServerLevel level, BlockPos pos) {
        entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
        entity.setPersistenceRequired();
    }

    private static BlockPos findSafePosition(ServerLevel level, BlockPos origin,
                                              StructureStart start, List<BlockPos> occupied) {
        int checks = 0;
        for (int radius = 2; radius <= SEARCH_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    for (int dy = -2; dy <= 2; dy++) {
                        if (++checks > MAX_POSITION_CHECKS) return null;
                        BlockPos pos = origin.offset(dx, dy, dz);
                        if (!level.isLoaded(pos) || !level.isLoaded(pos.above()) || !level.isLoaded(pos.below())) continue;
                        if (!insideAnyPiece(start, pos)) continue;
                        if (occupied.stream().anyMatch(existing -> existing.distSqr(pos) < 9.0D)) continue;
                        BlockState floor = level.getBlockState(pos.below());
                        if (floor.isAir() || floor.getCollisionShape(level, pos.below()).isEmpty()) continue;
                        if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) continue;
                        if (!level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) continue;
                        return pos.immutable();
                    }
                }
            }
        }
        return null;
    }

    private static boolean insideAnyPiece(StructureStart start, BlockPos pos) {
        for (StructurePiece piece : start.getPieces()) {
            if (piece.getBoundingBox().isInside(pos)) return true;
        }
        return false;
    }

    private static String populationKey(ServerLevel level, String site, BoundingBox box) {
        return level.dimension().location() + "|" + site + "|"
                + box.minX() + "," + box.minY() + "," + box.minZ() + "|"
                + box.maxX() + "," + box.maxY() + "," + box.maxZ();
    }
}
