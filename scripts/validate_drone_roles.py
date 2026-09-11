"""Static contract checks for the Drone Roles Alpha vertical slice."""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]

required = {
    "src/main/java/matteroverdrive/entity/DroneEntity.java": [
        "ROLE_COMBAT", "ROLE_REPAIR", "ROLE_LOGISTICS", "ROLE_SURVEY", "ROLE_REACTOR_MAINTENANCE",
        "DroneEnergy", "performRoleWork", "roleName()", "receiveDroneEnergy",
        "MODE_PATROL", "PatrolAnchor", "patrolFlight", "PATROL_RADIUS_SQR", "recallTo",
        "MODE_LOGISTICS", "LogisticsTarget", "performLogisticsDelivery", "logisticsFlight", "ForgeCapabilities.ITEM_HANDLER",
        "maintainStabilizer", "GravitationalStabilizerBlockEntity", "BlockPos.betweenClosed",
    ],
    "src/main/java/matteroverdrive/item/DroneDeploymentCoreItem.java": [
        "DroneRole", "DroneLogisticsTarget", "logisticsTarget", "setOwnerUuid", "setDroneType", "setPersistenceRequired",
    ],
    "src/main/java/matteroverdrive/network/DroneStatusPacket.java": [
        "byte role", "int energy", "int maxEnergy", "BlockPos patrolAnchor", "BlockPos logisticsTarget",
    ],
    "src/main/java/matteroverdrive/network/DroneCommandPacket.java": [
        "MAX_MANAGED_DRONES", "clampCommandMode", ".limit(MAX_MANAGED_DRONES)", "RECALL_ALL", "recallAll",
    ],
    "src/main/java/matteroverdrive/client/screen/DroneManagementScreen.java": [
        "roleName(info.role())", "info.energy()", "info.maxEnergy()", "PATROL ALL", "LOGISTICS ALL", "RECALL ALL", "info.patrolAnchor()", "info.logisticsTarget()",
    ],
    "src/main/resources/assets/matteroverdrive/models/item/drone_deployment_core.json": [
        "matteroverdrive:item/plasma_core",
    ],
    "src/main/resources/data/matteroverdrive/recipes/drone_deployment_core.json": [
        "matteroverdrive:drone_deployment_core",
    ],
    "src/main/java/matteroverdrive/blockentity/ChargingStationBlockEntity.java": [
        "import matteroverdrive.entity.DroneEntity;", "chargeDrones()",
        "getOwnerUuid()", "receiveDroneEnergy", "DRONE_SCAN_INTERVAL_TICKS",
        "MAX_CACHED_DRONES", "refreshDroneCache", "Math.floorMod", "maintainDrones",
        "MAX_DRONES_REPAIRED_PER_CYCLE", "DRONE_REPAIR_FE_PER_HEALTH", "dronesRepaired",
    ],
}

errors = []
for relative, needles in required.items():
    path = ROOT / relative
    if not path.is_file():
        errors.append(f"missing {relative}")
        continue
    text = path.read_text(encoding="utf-8")
    for needle in needles:
        if needle not in text:
            errors.append(f"{relative}: missing contract marker {needle!r}")

entity = (ROOT / "src/main/java/matteroverdrive/entity/DroneEntity.java").read_text(encoding="utf-8")
if "inflate(6D)" not in entity or "inflate(18D)" not in entity:
    errors.append("Drone role scans must remain explicitly bounded")
if "putInt(\"DroneEnergy\"" not in entity or "getInt(\"DroneEnergy\"" not in entity:
    errors.append("Drone energy persistence read/write contract is incomplete")
if "hasChunkAt(logisticsTarget)" not in entity:
    errors.append("Logistics routing must not force-load target chunks")

if errors:
    print("DRONE ROLE VALIDATION FAILED")
    print("\n".join(errors))
    sys.exit(1)

print("Drone role contract validation passed (5 roles, patrol/logistics persistence, bounded commands/work, status sync, cached charging)")
