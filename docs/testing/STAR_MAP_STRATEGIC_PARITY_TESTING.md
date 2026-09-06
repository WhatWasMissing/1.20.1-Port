# Star Map Strategic Parity Testing

Authoritative reference: Matter Overdrive 1.7.10 0.4.2.

## Save compatibility
- [ ] A world created before this pass keeps the same q/s/p ownership, buildings, fleets and queued construction for the original first 4 quadrants / first 6 stars.
- [ ] Existing fifth compatibility planets, where present in old 1.20 catalog stars, remain reachable even though new appended legacy stars use the source-effective 1-3 planet count.
- [ ] Breaking/replacing a Star Map still does not erase server-global planet state.

## Expanded galaxy
- [ ] Galaxy view exposes 27 quadrants and remains responsive.
- [ ] Original Aquila/Cygnus/Orion/Perseus quadrants remain at their existing indices.
- [ ] Original first six stars in those quadrants retain stable star and planet indices.
- [ ] Appended quadrants/stars can be reached with wheel zoom + drag and can be selected reliably.
- [ ] Total catalog construction does not produce noticeable world-load hitching or excessive memory use.
- [ ] New stars expose 1-3 planets at default legacy-effective generation behavior.
- [ ] Newly generated planets use only Normal, Gas Giant or Dwarf classes.

## Planet generator identity
- [ ] Normal planets report 6/6 base capacity before Base/Residential/Hangar bonuses.
- [ ] Gas Giants report 2/8.
- [ ] Dwarfs report 4/4.
- [ ] New deterministic planet sizes stay within source ranges: Normal 0.7-1.3, Gas Giant 2.0-3.0, Dwarf 0.2-0.6.
- [ ] Existing ordinal orbit remains stable for 10-per-AU same-star travel.

## Economy / construction
- [ ] Homeworld remains 8 base building / 10 base fleet spaces, receives Base + starting Scout and retains the intentional 1.20 Ship Factory compatibility bridge.
- [ ] Four independent construction slots persist through save/reload and travel.
- [ ] Capacity reservation rejects overbooking from parallel build slots.
- [ ] Hangar adds 2 fleet spaces.
- [ ] Residential adds 4 building spaces and 10,000 population while applying recovered energy/matter/happiness effects.
- [ ] Matter Extractor and Power Generator production figures remain source-backed.
- [ ] Scout/Colonizer/Factory/Hangar/Extractor/Generator/Residential build durations remain unchanged from the recovered values.

## Ship travel
- [ ] Scout can transfer between friendly colonies with available fleet capacity and produces no invented scouting reward.
- [ ] Colonizer arriving at an unowned planet establishes ownership + Base and is consumed.
- [ ] Colonizer arriving at a friendly colony stations there when capacity exists; otherwise return behavior remains safe.
- [ ] Independent ship travel persists separately from command-fleet travel.
- [ ] 10-per-AU same-system and 8-per-LY interstellar timing behavior remains intact.

## Presentation
- [ ] 27 quadrant dots remain distinguishable at common GUI scales.
- [ ] Quadrants containing roughly legacy-scale star density remain navigable with zoom/pan.
- [ ] Star/planet selection does not become ambiguous enough to prevent normal use; record screenshots at GUI scales 2, 3 and Auto for later refinement.
- [ ] Existing economy and travel controls remain within the Star Map panel and do not overlap inventory slots.
