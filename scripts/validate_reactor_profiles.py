"""Validate the player-facing fusion reactor operating profile slice."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def require(path: str, *needles: str) -> None:
    text = (ROOT / path).read_text(encoding="utf-8")
    missing = [needle for needle in needles if needle not in text]
    if missing:
        raise SystemExit(f"{path}: missing {missing}")

require(
    "src/main/java/matteroverdrive/blockentity/FusionReactorControllerBlockEntity.java",
    "enum OperatingMode",
    'OVERDRIVE("Overdrive"',
    'CONSERVATION("Conservation"',
    "OperatingMode",
    "MAX_HEAT",
    "MAX_STABILITY",
    "updateContainmentState",
    '"Heat"',
    '"Stability"',
    '"Thermal runaway"',
    '"Containment failure"',
    "announceAlarm",
    "containment heat above 80%",
    "thermal runaway — automatic SCRAM",
    "sendParticles(ParticleTypes.FLAME",
    "NOTE_BLOCK_BASS",
    "heat = Math.min(MAX_HEAT",
    "heat = Math.max(0, heat - 8)",
    "stability = Math.max(0, Math.min(MAX_STABILITY",
    "stability = Math.min(MAX_STABILITY",
    "reactorEnabled = false",
    "OperatingMode",
)
require(
    "src/main/java/matteroverdrive/menu/FusionReactorMenu.java",
    "DATA_COUNT = 37",
    "public int heat()",
    "public int stability()",
    "operatingModeLabel",
    "id == 4",
)
require(
    "src/main/java/matteroverdrive/client/screen/FusionReactorScreen.java",
    "PROFILE",
    "click(4)",
    "menu.heat()",
    "menu.stability()",
    "THERMAL RUNAWAY",
    "CONTAINMENT FAILURE",
)
require(
    "src/main/java/matteroverdrive/quest/ReactorCommands.java",
    'Commands.literal("reactor")',
    'Commands.literal("status")',
    "offset(-32, -16, -32)",
    "getHeat()",
    "getStability()",
    'Commands.literal("scram")',
    'Commands.literal("reset")',
    "emergencyScram()",
    "resetContainment()",
)
require(
    "src/main/java/matteroverdrive/entity/DroneEntity.java",
    "ROLE_REACTOR_MAINTENANCE",
    "maintainStabilizer()",
    "FusionReactorControllerBlockEntity",
    "applyMaintenancePulse",
)
require(
    "src/main/java/matteroverdrive/blockentity/FusionReactorControllerBlockEntity.java",
    "applyMaintenancePulse(int availableEnergy)",
    '"Thermal runaway".equals(fault)',
    "heat = Math.max(0, heat - 40)",
    "stability = Math.min(MAX_STABILITY, stability + 30)",
    'fault = "Manual SCRAM"',
    "heat >= 500 || stability <= 0",
)
print("reactor operating profile validation: PASS")
