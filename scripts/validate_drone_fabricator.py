"""Regression contract for the FE-powered Drone Fabricator vertical slice."""
from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
required = {
    "src/main/java/matteroverdrive/block/DroneFabricatorBlock.java": ["DroneFabricatorBlockEntity", "NetworkHooks.openScreen", "FACING"],
    "src/main/java/matteroverdrive/blockentity/DroneFabricatorBlockEntity.java": [
        "FABRICATION_FE_PER_TICK", "FABRICATION_TICKS", "hasIngredients()", "canOutput()", "consumeIngredients()",
        "MachineRedstoneMode.allowsWork", "pullAdjacentEnergy", "pullAdjacentIngredients", "ForgeCapabilities.ITEM_HANDLER", "MachineUpgradeInventory", "fabricationTicks()", "fabricationEnergyPerTick()", "DroneRole", "saveAdditional", "getCapability",
        "plasma_core", "isolinear_circuit_mk2", "tritanium_plate", "hasResearchClearance", "pullNetworkIngredients", "ItemNetworkUtil.pullMatchingItem", "DATA_COUNT = 14", "cycleNetworkChannel", "NetworkChannel"
    ],
    "src/main/java/matteroverdrive/item/DroneDeploymentCoreItem.java": ["requiredResearch", "hasResearchClearance", "clearanceMessage", "ResearchProgression.Stage"],
    "src/main/java/matteroverdrive/entity/DroneEntity.java": ["DroneDeploymentCoreItem.hasResearchClearance"],
    "src/main/java/matteroverdrive/MatterOverdrive.java": ["blockEntities=19, menus=16", "droneFabricator=enabled"],
    "src/main/java/matteroverdrive/menu/DroneFabricatorMenu.java": ["cycleSelectedRole", "cycleRedstoneMode", "quickMoveStack", "recipeReady", "MachineUpgradeItem", "fabricationTicks"],
    "src/main/java/matteroverdrive/client/screen/DroneFabricatorScreen.java": ["DRONE ASSEMBLY GANTRY", "ASSEMBLING", "AUTO-FEED", "ROLE"],
    "src/main/java/matteroverdrive/registry/ModBlocks.java": ["drone_fabricator", "DroneFabricatorBlock"],
    "src/main/java/matteroverdrive/registry/ModBlockEntities.java": ["DRONE_FABRICATOR"],
    "src/main/java/matteroverdrive/registry/ModMenus.java": ["DRONE_FABRICATOR"],
    "src/main/java/matteroverdrive/client/ClientModEvents.java": ["DroneFabricatorScreen"],
    "src/main/resources/assets/matteroverdrive/blockstates/drone_fabricator.json": ["matteroverdrive:block/drone_fabricator"],
    "src/main/resources/assets/matteroverdrive/models/block/drone_fabricator.json": ["Fabricator Gantry", "Assembly Core"],
    "src/main/resources/data/matteroverdrive/recipes/drone_fabricator.json": ["matteroverdrive:drone_fabricator"],
    "src/main/resources/data/matteroverdrive/loot_tables/blocks/drone_fabricator.json": ["matteroverdrive:drone_fabricator"],
}
errors = []
for relative, needles in required.items():
    path = ROOT / relative
    if not path.is_file():
        errors.append(f"missing {relative}")
        continue
    source = path.read_text(encoding="utf-8")
    for needle in needles:
        if needle not in source:
            errors.append(f"{relative}: missing {needle!r}")

for relative in [
    "src/main/resources/assets/matteroverdrive/blockstates/drone_fabricator.json",
    "src/main/resources/assets/matteroverdrive/models/block/drone_fabricator.json",
    "src/main/resources/data/matteroverdrive/recipes/drone_fabricator.json",
    "src/main/resources/data/matteroverdrive/loot_tables/blocks/drone_fabricator.json",
]:
    try:
        json.loads((ROOT / relative).read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        errors.append(f"invalid JSON {relative}: {exc}")

machine = (ROOT / "src/main/java/matteroverdrive/blockentity/DroneFabricatorBlockEntity.java").read_text(encoding="utf-8")
if "inventory.extractItem" not in machine or "if (++progress < fabricationTicks()) return;" not in machine:
    errors.append("fabricator must only consume inputs at successful completion")
if "energy.consumeEnergy(energyCost" not in machine or "fabricationEnergyPerTick()" not in machine:
    errors.append("fabricator must consume real FE while assembling")

if errors:
    print("DRONE FABRICATOR VALIDATION FAILED")
    print("\n".join(errors))
    sys.exit(1)
print("Drone Fabricator validation passed: powered fabrication, persistence, role output, GUI, and resources present")
