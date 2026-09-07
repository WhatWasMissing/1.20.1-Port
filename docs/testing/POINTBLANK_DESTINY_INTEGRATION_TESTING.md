# Point Blank Destiny Integration Testing

## Purpose

Matter Overdrive does not redistribute the Point Blank Destiny content pack. The integration is registry-based and remains inert when Point Blank or the Destiny pack is absent.

Target content pack supplied for testing: `pointblank-destiny-pack` / Destiny pack 0.5.1.

## Weapons detected

- Khvostov 7G-02
- Marshal-A1
- Trax Callum 1
- Proxima Centauri II
- Hawkmoon
- Eyasluna
- The Last Word
- Ace Of Spades
- Thorn
- SUROS Regime
- Monte Carlo
- MIDA Multi-Tool
- CHAOS DOGMA~
- Sleeper Simulant

## Install for testing

1. Install a Forge 1.20.1 build of Vic's Point Blank that supports the supplied pack.
2. Create `.minecraft/pointblank` if it does not already exist.
3. Place `destiny-ext 0.5.1.zip` in that folder.
4. Start Minecraft with Matter Overdrive.
5. Do not unpack or copy the Destiny assets into the Matter Overdrive JAR.

## Verify

1. With Point Blank and the Destiny extension absent, Matter Overdrive starts normally and its creative tab is unchanged except for native MO content.
2. With the extension installed, all 14 detected Destiny weapons also appear at the end of the Matter Overdrive creative tab.
3. Hover a detected weapon. It should show a `Matter Overdrive // Destiny Integration` tooltip.
4. Fire a detected weapon as a non-Android and record damage.
5. Convert to Android and equip damage-changing Aspects/Fragments such as Vanguard Protocol or Surge. Confirm their generic player-sourced damage modifiers also affect the Point Blank weapon.
6. Confirm defensive Android Aspects/Fragments still work while a Point Blank weapon is held.
7. Confirm the Point Blank pack's own animations, reloads, sounds, reticles and effects remain controlled by Point Blank and are not replaced by Matter Overdrive.
8. Confirm uninstalling the content pack does not leave missing-item errors or prevent Matter Overdrive from loading.

## Matter values

The compatibility table reserves Matter values for these weapons so the Matter Overdrive matter network can recognize them in a later decomposer/replicator integration pass. This first bridge does not copy or rewrite Point Blank's gun definitions.

## Asset policy

The supplied Destiny content pack is an external companion pack. Its meshes, textures, animations and audio remain outside the Matter Overdrive JAR. Matter Overdrive only references the registered item IDs at runtime.
