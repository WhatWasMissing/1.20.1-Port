# Matter Overdrive 1.20.1 Desktop-Mode Handoff

Date: 2026-09-10
Repository: https://github.com/WhatWasMissing/1.20.1-Port
Target branch: main
Latest main commit: b1a80629545d9f76143687e3dca1676b0d67e8f5

## Objective

Continue development of the Matter Overdrive Forge 1.20.1 port using the local Windows checkout. The immediate objective is to finish the native Destiny weapon pass, build it locally, and test the result in Minecraft. After that, continue broader parity, structure, reactor, Android progression, and weapon-quality work from the current repository state.

Always inspect the local branch, status, and remote commit before editing. Do not assume an older branch or conversation state is current.

## Current repository state

The native Destiny weapon work was developed on feature/weapon-input-destiny-render and then moved into main by a clean fast-forward. Main had no divergent commits and was 20 commits behind the feature branch.

Relevant commits:

- 624fc0816de9332deb5d5e07a93326b291f4ce96 — native Destiny renderer, input, profiles, models, staged geometry bundle, reload-energy path, importer, and sound resources.
- e84390d4145a3ad1213f8dc293a5303e5fbda2d6 — synchronized TO_TEST.md with bundled to_test.txt.
- b1a80629545d9f76143687e3dca1676b0d67e8f5 — synchronized WORKING_FEATURES.md with bundled current_features.txt.

The user’s local checkout was previously at 624fc08 and then pulled e84390d. It must pull b1a8062 before rebuilding.

## Immediate build status

The Destiny importer and renderer consistency check already passed locally:

~~~text
Imported 14/14 native Destiny textures
Imported 56/56 native Destiny sounds
WEAPON RENDERER CONSISTENCY PASSED
~~~

The build previously failed only because bundled documentation mirrors were stale. The Gradle task checks these three pairs:

1. docs/testing/TO_TEST.md ↔ src/main/resources/assets/matteroverdrive/docs/to_test.txt
2. docs/reference/WORKING_FEATURES.md ↔ src/main/resources/assets/matteroverdrive/docs/current_features.txt
3. docs/reference/SYSTEM_GUIDE.md ↔ src/main/resources/assets/matteroverdrive/docs/system_guide.txt

The first pair was fixed in e84390d. The second was fixed in b1a8062. The third was already synchronized. Do not bypass verifyBundledDocumentation.

## Immediate Windows commands

The user’s local checkout is:

~~~text
C:/Users/novel/Desktop/MatterOverdrive-1.20.1 test/MatterOverdrive-1.20.1-port-M2-Network-alpha4.1/MatterOverdrive-1.20.1-port
~~~

From Command Prompt:

~~~cmd
cd "C:/Users/novel/Desktop/MatterOverdrive-1.20.1 test/MatterOverdrive-1.20.1-port-M2-Network-alpha4.1/MatterOverdrive-1.20.1-port"
git status
git fetch origin
git switch main
git pull --ff-only origin main
git rev-parse --short HEAD
~~~

Expected commit:

~~~text
b1a8062
~~~

The local Destiny binary assets were imported and are untracked working-tree files. Pulling the documentation-only commit should preserve them. Do not use git reset --hard or delete the resource folders.

Re-run the build:

~~~cmd
gradlew.bat build
~~~

If it succeeds:

~~~cmd
dir build/libs/*.jar
~~~

If it fails, preserve the complete first error block, including task name and file/line. Do not hide failures with -x or skipped verification tasks.

## Native Destiny pass already implemented

The native pass currently covers these 14 Matter Overdrive items:

- destiny_aceofspades
- destiny_chaosdogma
- destiny_eyasluna
- destiny_hawkmoon
- destiny_khvostov7g02
- destiny_marshala1
- destiny_midamultitool
- destiny_montecarlo
- destiny_proximacentauriii
- destiny_sleepersimulant
- destiny_surosregime
- destiny_thelastword
- destiny_thorn
- destiny_traxcallum1

Implemented pieces include:

- Native item/profile registration and Matter Overdrive creative-tab entries.
- builtin/entity item-model entry points for all 14 items.
- Native first-person and item-context renderer dispatch.
- Staged geometry/animation data loaded from:
  - src/main/resources/assets/matteroverdrive/native_destiny/weapons_00.b64
  - src/main/resources/assets/matteroverdrive/native_destiny/weapons_01.b64
- Weapon input packet path and client trigger handling.
- Draw, ADS, recoil, fire, and reload animation inputs.
- Magazine/energy state and server-side firing path.
- Shift-right-click reload.
- Energy Pack and charged battery transfer during reload.
- 56 native Destiny sound definitions and deterministic asset import support.
- Updated testing documentation and static consistency validation.

The validation script is scripts/validate_weapon_renderer_consistency.py. Run it with:

~~~cmd
py scripts/validate_weapon_renderer_consistency.py
~~~

## Destiny asset source and importer

The connected Dropbox source contains two packs:

- destiny-ext 0.5.1
- Destiny_GunPack_v1.5.2

The first native pass uses destiny-ext 0.5.1. It contains the exact 14 native weapon PNGs, 56 matching OGG files, geometry JSON, animation JSON, language data, and sounds.json.

The importer is scripts/import_destiny_assets.py. It copies:

- 14 PNGs to src/main/resources/assets/matteroverdrive/textures/native_destiny
- 56 OGGs to src/main/resources/assets/matteroverdrive/sounds

The source must be extracted, not left as a ZIP:

~~~cmd
python scripts/import_destiny_assets.py --source "C:/MatterOverdriveAssets/destiny-ext 0.5.1" --strict
~~~

Expected result:

~~~text
Imported 14/14 native Destiny textures
Imported 56/56 native Destiny sounds
~~~

Do not re-run the importer unless the imported files are missing. Do not commit the binaries until the build and basic game test pass. Once testing succeeds, inspect:

~~~cmd
git status --short
~~~

If the user wants the binary resources stored in Git:

~~~cmd
git add src/main/resources/assets/matteroverdrive/textures/native_destiny
git add src/main/resources/assets/matteroverdrive/sounds
git commit -m "Add native Destiny textures and sounds"
git push origin main
~~~

Check the staged file list first. Do not add build output, saves, logs, or IDE files.

## What is not complete

Destiny_GunPack_v1.5.2 is not yet ported into Matter Overdrive. It is a substantially larger second pass containing approximately:

- 956 total files
- 247 textures
- 177 OGG files
- 78 Bedrock geometry models
- 30 animation files
- additional display, data, recipe, tag, and script files

Its geometry uses the Bedrock minecraft:geometry format with per-face UV objects, unlike the normalized native bundle consumed by NativeDestinyVisualLibrary. The correct next step is an adapter/normalization pass followed by item registration in coherent weapon groups.

The two Vex Mythoclast sound IDs in ModDestinySounds are stable placeholders and do not have source OGGs in the 56-file native import set. Treat them as a future asset task, not as proof that the full GunPack is complete.

## Required game testing after build

Install the generated main mod JAR from build/libs into the Forge 1.20.1 mods directory. Use a new test world first.

Verify:

1. All 14 weapons appear in the Matter Overdrive creative tab.
2. Names and inventory, ground, and third-person models render without missing-model errors.
3. First-person models are visible and oriented correctly.
4. Draw, ADS, recoil, fire, and reload animations activate.
5. Weapons cannot fire without the required energy.
6. Firing consumes the weapon’s intended energy.
7. Shift-right-click starts reload.
8. Energy Packs and charged batteries transfer energy correctly.
9. Reload completes and firing becomes available again.
10. Server-side damage, range, spread, and automatic-fire cadence work.
11. Save, quit, relaunch, and confirm magazine/energy state is preserved.
12. Check the client log for missing Destiny textures, sounds, or renderer exceptions.

The external Point Blank pack remains separate. The native Matter Overdrive pass should work without Point Blank installed. If testing the external compatibility bridge, keep the Point Blank pack in its expected folder and do not place it in the Forge mods directory.

## Desktop-mode tool advantages

Desktop mode should use the actual local checkout. It is useful here because it can:

- Inspect the complete local source tree and untracked imported PNG/OGG files.
- Run git status, branch comparisons, diffs, and local merges.
- Run the Gradle wrapper and capture compiler/runtime errors.
- Run Python validation and importer scripts directly.
- Inspect ZIP/JAR contents and resource paths.
- Compare repository documents with bundled in-game resources.
- Inspect PNG dimensions, transparency, model JSON, animation JSON, and sound filenames.
- Build the mod locally without relying on exhausted GitHub Actions.
- Examine latest.log, crash reports, and generated JAR contents.
- Make coordinated local edits with immediate syntax/build feedback.

Desktop mode still needs user-provided evidence for things it cannot observe directly, such as whether an animation looks correct on-screen, whether a weapon is held too low in third person, or whether a world-generation issue reproduces during play. Screenshots, logs, and short test results remain useful.

## Suggested Desktop-mode opening prompt

~~~text
Continue Matter Overdrive Forge 1.20.1 development from the local checkout at C:/Users/novel/Desktop/MatterOverdrive-1.20.1 test/MatterOverdrive-1.20.1-port-M2-Network-alpha4.1/MatterOverdrive-1.20.1-port.

Read docs/handoffs/MATTER_OVERDRIVE_DESKTOP_HANDOFF_2026-09-10.md first. Work from main, fetch the latest remote state, and inspect git status before editing. The expected current remote commit is b1a80629545d9f76143687e3dca1676b0d67e8f5.

The native Destiny importer already reported 14/14 textures and 56/56 sounds, and the renderer consistency script passed. Previous builds failed because bundled documentation mirrors were stale; those were fixed in e84390d and b1a8062. Run gradlew.bat build now, fix only the first real failure if one remains, and preserve the imported untracked assets. Do not skip verification tasks. After a successful build, inspect the generated JAR and prepare the native Destiny weapon test checklist. Do not claim Destiny_GunPack_v1.5.2 is ported; it is a separate larger geometry-adapter pass.
~~~
