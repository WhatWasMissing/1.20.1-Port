# Android power/progression state hardening

This targeted pass makes chassis-adjusted Android energy capacity authoritative across charging, loadout thresholds, network sync and progression displays.

## Fixed

- Capacitor Core's +50,000 FE reserve is honored by crouch-held battery charging and loadout bonus charging.
- 50% and 75% loadout/perk thresholds use `AndroidData.getEnergyCapacity(player)` rather than the 100,000 FE base constant.
- Removing or swapping a capacity-granting core persists an immediate clamp to the reduced maximum, preventing hidden excess FE from reappearing later.
- Effective energy capacity is synchronized in `AndroidStatePacket`; the network protocol was bumped for the packet shape change.
- Android HUD and Skill Tree display the synchronized effective maximum.
- HUD XP now follows the server's nonlinear `experienceForLevel` thresholds.
- HUD available Ascension Points now use `skillPointsForLevel` (one point every two levels, max five).
- Added `scripts/validate_android_consistency.py` and wired it into `VERIFY_M2_BUILD.bat`.

## Runtime focus

Test 100k -> 150k charging with Capacitor Core, 50%/75% threshold boundaries, capacity shrink/reinstall behavior, HUD/Skill Tree/Station agreement, and relog/death persistence.

Forge/Minecraft runtime validation is still required locally.
