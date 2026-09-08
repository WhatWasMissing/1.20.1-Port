---
navigation:
  title: Star Map
  parent: index.md
  position: 10
  icon: matteroverdrive:star_map
---
# Star Map

The **Star Map** is Matter Overdrive's strategic layer. It is not just a decorative block in the 0.6 line: the restored system tracks navigation, travel, fleets, colonies, encounters and economy state persistently in the world.

## Navigation hierarchy

The map is arranged as:

**Galaxy -> Quadrant -> Star -> Planet**

Move down the hierarchy to inspect a destination and back out when you want to select another region. A planet is therefore not identified only by one flat index; its star/quadrant context matters to travel and persisted state.

## Travel

Star Map travel is server-authoritative. The client requests a destination and the world stores the resulting journey state. Same-star travel and longer interstellar travel use different scales instead of every destination completing in the same amount of time.

Before dispatching a journey, verify that the selected quadrant/star/planet really is the destination you intended. Closing the GUI does not mean a persistent journey was cancelled.

## Encounters and fleet combat

Travel can create strategic encounters. Fleet combat is resolved through the Star Map state rather than spawning an ordinary Minecraft melee beside the block.

If an attack option is unavailable, check that you actually have a fleet/ship state capable of performing the action and that the currently viewed destination is valid.

## Scouts

Scout ships are the exploration tool. Use them to reveal or interact with strategic destinations before committing more expensive colony/fleet actions. Scout state persists with the world, so dispatching a scout is a strategic action rather than a temporary GUI animation.

## Colonizers and colonies

Colonizer ships establish persistent colony state at eligible destinations. A colony tracks its own economy and build queue. Once established, revisit the same strategic destination to inspect or manage that colony rather than expecting its state to appear at every planet.

## Economy

Colonies have economy actions and build-queue state. Resources and queued work are world data: leaving the screen or restarting the game should not reset legitimate progress.

When testing economy behavior, record the destination plus the before/after resource values. This makes it possible to distinguish a persistence bug from viewing a different planet.

## Ship dispatch

Ship dispatch requests include the selected ship type and destination coordinates in the strategic hierarchy. If a ship appears to go to the wrong place, report the quadrant, star and planet selected at the time of dispatch.

## Persistence expectations

The following are intended to survive save/reload:

- active journeys;
- discovered/explored strategic state where implemented;
- fleet/ship state;
- colonies;
- colony economy;
- build queues.

If one of these resets, reproduce it with a clean sequence such as **open map -> perform one action -> save and quit -> reload -> inspect the same destination**.

## Relationship to the overworld

The Star Map is a strategic simulation layer attached to your Minecraft world. It does not replace the Transporter and it does not mean every strategic planet is a directly walkable Minecraft dimension. Use [Transporter and Security](transporter_security.md) for local/entity transport systems.
