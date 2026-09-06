# Star Map Strategic Parity Pass

Authoritative reference: Matter Overdrive 1.7.10 0.4.2.

## Strategic class audit

The authoritative strategic item set is already represented by the port. There are no additional legacy ship/building classes to invent:

- Scout Ship
- Colonizer Ship
- Base
- Ship Factory
- Ship Hangar
- Matter Extractor
- Power Generator
- Residential Building

The existing four-slot planet construction model, planet-local fleets, capacity reservation and independent ship travel remain the correct modern foundation.

## Recovered galaxy generator identity

Legacy `GalaxyGenerator` defaults:

- quadrant axis count: 3, yielding 27 quadrants (`3 ^ 3`)
- minimum stars: 2,048
- maximum stars: 2,304 (legacy random selection treats the upper bound as exclusive)
- configured planets per star: 1-4; the legacy `nextInt(max-min)` implementation effectively generates 1-3 at defaults
- star-name prefix chance: 1.0
- star-name suffix chance: 0.8

The port now expands its deterministic catalog to 27 quadrants / 2,176 stars while preserving the original 1.20 compatibility coordinates at q0-q3/s0-s5. Existing planet keys remain valid; expansion is append-only around that compatibility region.

## Recovered star classes

The seven legacy star generators use relative weights:
`0.00003, 0.13, 0.6, 3.0, 7.6, 12.1, 76.45`.

Recovered temperature ranges progress from approximately 30,000-60,000 K down to 2,400-3,700 K. New appended stars use those weighted classes and recovered radius bands. Compatibility stars preserve their stable positions/names while receiving deterministic legacy-class metadata.

## Recovered planet classes

Legacy planet generation has only three classes:

- Normal: 6 base building spaces / 6 base fleet spaces, size 0.7-1.3
- Gas Giant: 2 / 8, size 2.0-3.0
- Dwarf: 4 / 4, size 0.2-0.6

Generator weighting depends on the legacy 0-1 orbit value: Normal is favored around 0.4-0.6, Gas Giant beyond 0.6, and Dwarf below 0.4 or beyond 0.6. The previous port-only `Oceanic` class is no longer generated; old index positions are preserved and map into the legacy class model.

The ordinal `orbit` used by the modern 10-per-AU travel compatibility calculation is retained. A separate deterministic `legacyOrbit` property preserves the recovered 0-1 generator parameter without breaking travel or save keys.

## Homeworld / economy

Authoritative homeworld initialization is owner + homeworld flag + 8 base building spaces + 10 base fleet spaces + Base + one Scout. The port deliberately retains its existing automatic Ship Factory bridge for compatibility with worlds/progression created before exact strategic parity; this is documented as a modernization, not claimed as an original legacy starting building.

Recovered building effects/build times already matched the current port and remain unchanged:

- Base: +2 building spaces; legacy build length 10,000 ticks when built normally.
- Scout: 3,600 ticks.
- Colonizer: 5,000 ticks.
- Ship Factory: current compatibility production gate retained.
- Hangar: 4,800 ticks, +2 fleet spaces.
- Matter Extractor: 14,400 ticks, +10 matter / -6 energy.
- Power Generator: 14,400 ticks, +8 energy / -2 matter.
- Residential: 6,000 ticks, +10,000 population / -4 energy / -2 matter / +4 building spaces; happiness contribution remains derived from current energy/matter balance using recovered +0.5/-0.4 and +0.5/-0.6 factors.

## Travel

The source-backed ship set is Scout + Colonizer. Legacy Scout travel callback is intentionally empty; the port does not invent a scouting reward. Colonizer establishes ownership and a Base at an unowned destination. Current independent per-ship travel and server-global planet ownership preserve those semantics in a multiplayer-safe form.

The current command-fleet random encounters are additive 1.20 gameplay; they are not presented as recovered 1.7 `TravelEvent` subclasses. The authoritative `TravelEvent` is the generic from/to/time/ship journey record.
