# Android power-state hardening test plan

The primary test matrix is `TO_TEST.md`; this page isolates the new Android capacity/progression regression checks for fast retesting.

- Base Android reserve: **100,000 FE**.
- Capacitor Core reserve: **150,000 FE**.
- At 150k, 50% gates are **75,000 FE** and 75% gates are **112,500 FE**.
- Crouch-held Battery/HC Battery charging must continue above 100k with Capacitor Core installed.
- Fragment of Induction and Recursive Core bonus charging must respect the same effective maximum.
- Removing/swapping Capacitor Core above 100k must immediately and permanently clamp stored FE to 100k.
- Reinstalling the core must not restore discarded hidden FE.
- HUD, Skill Tree and Android Station must show the same capacity.
- HUD XP must match nonlinear server thresholds and Ascension Points must use one point per two levels.
- Relog/death must preserve core, chassis, skill and loadout state.

Static gate:

```text
python scripts/validate_android_consistency.py
```
