#!/usr/bin/env python3
"""Static structure gate for Matter Overdrive.

Checks the active new-world generators against the vanilla-style contract:
terrain -> entrance -> readable route -> guarded objective -> optional side rooms -> exit.
Also rejects live Matter Overdrive machine placement in generated exploration sites.
"""
from pathlib import Path
import sys

ROOT=Path(__file__).resolve().parent
WG=ROOT/"src/main/java/matteroverdrive/worldgen"
REG=ROOT/"src/main/java/matteroverdrive/registry/ModStructures.java"
EVENT=ROOT/"src/main/java/matteroverdrive/event/StructureExplorationSanitizer.java"
LOOT=ROOT/"src/main/resources/data/matteroverdrive/loot_tables/chests/facilities/story_cache.json"

def read(p):
    if not p.exists(): raise FileNotFoundError(p)
    return p.read_text(encoding="utf-8")

def main():
    errors=[]
    legacy=read(WG/"LegacyVanillaStructurePiece.java")
    modern=read(WG/"ModernExplorationStructurePiece.java")
    frontier=read(WG/"FrontierExplorationStructurePiece.java")
    legacy_start=read(WG/"LegacyNativeStructure.java")
    modern_start=read(WG/"TechnologyFacilityStructure.java")
    frontier_start=read(WG/"FrontierSiteStructure.java")
    registry=read(REG)

    required={
        "legacy":(legacy,("crashedShip","cargoShip","underwaterBase","madScientistLab","androidSafehouse","excavation","story_cache","salvage","android_spawner","corridorX","corridorZ")),
        "modern":(modern,("plant(","refinery(","relay(","bunker(","fusion(","blackSite(","story_cache","android_spawner","corridorX","corridorZ","cache(","stairNorthSouth")),
        "frontier":(frontier,("vault(","foundry(","quarantine(","recovery(","story_cache","android_spawner","corridorX","corridorZ","cache(","stair(")),
    }
    for name,(src,tokens) in required.items():
        for token in tokens:
            if token not in src: errors.append(f"{name}: missing topology/policy token {token}")

    for src,token,msg in (
        (legacy_start,"new LegacyVanillaStructurePiece(kind, origin)","Legacy starts are not wired to the vanilla exploration generator."),
        (modern_start,"new ModernExplorationStructurePiece(kind,origin,layout)","Modern facilities are not wired to ModernExplorationStructurePiece."),
        (frontier_start,"new FrontierExplorationStructurePiece(kind,origin,layout)","Frontier sites are not wired to FrontierExplorationStructurePiece."),
    ):
        if token not in src: errors.append(msg)

    for token in ("LEGACY_NATIVE_PIECE","TECHNOLOGY_FACILITY_PIECE","FRONTIER_SITE_PIECE",
                  "LEGACY_VANILLA_PIECE","MODERN_EXPLORATION_PIECE","MODERN_TRAVERSAL_REPAIR_PIECE","FRONTIER_EXPLORATION_PIECE"):
        if token not in registry: errors.append(f"registry missing {token}")

    modern_repair=WG/"ModernTraversalRepairPiece.java"
    if not modern_repair.exists(): errors.append("ModernTraversalRepairPiece.java missing")
    else:
        repair=read(modern_repair)
        for token in ("refineryTransition","blackSiteTransition","c.isInside"):
            if token not in repair: errors.append(f"modern traversal repair missing {token}")
        if "new ModernTraversalRepairPiece(kind,origin)" not in modern_start:
            errors.append("ModernTraversalRepairPiece is not assembled after the modern facility shell")

    active=("LegacyVanillaStructurePiece.java","LegacyTraversalRepairPiece.java","ModernExplorationStructurePiece.java","ModernTraversalRepairPiece.java","FrontierExplorationStructurePiece.java")
    for filename in active:
        src=read(WG/filename)
        if "clip.isInside" not in src and "c.isInside" not in src:
            errors.append(f"{filename}: no chunk clipping guard")
        for bad in ("getChunk(","setChunkForced","addRegionTicket","TicketType"):
            if bad in src: errors.append(f"{filename}: forbidden force-loading token {bad}")

    live_ids=("matter_analyzer","decomposer","replicator","inscriber","network_router","network_switch",
              "fusion_reactor_controller","fusion_reactor_io","drone_fabricator","android_station",
              "matter_storage_matrix","matter_excavator","grid_capacitor","quantum_power_relay",
              "gravitational_stabilizer","anomaly_containment_unit","facility_network_controller",
              "charging_station","android_induction_relay","weapon_station","matter_network_terminal")
    for name,src in (("legacy",legacy),("modern",modern),("frontier",frontier)):
        for machine in live_ids:
            if f'"{machine}"' in src: errors.append(f"{name}: active generator directly places functional machine {machine}")

    if "surfaceY + 24" in legacy_start: errors.append("Cargo Ship elevated boarding regression returned")
    if "surfaceY-22" in frontier_start.replace(" ",""): errors.append("Deep Matter Vault buried-entry regression returned")
    if "TechnologyFacilityStructurePiece.assemble" in modern_start: errors.append("Modern start fell back to old machine-room generator")
    if "FrontierSitePiece.assemble" in frontier_start: errors.append("Frontier start fell back to old machine-room generator")

    legacy_repair=WG/"LegacyTraversalRepairPiece.java"
    if not legacy_repair.exists() or "LegacyTraversalRepairPiece" not in legacy_start:
        errors.append("Mad Scientist traversal repair piece is not wired")

    if not EVENT.exists(): errors.append("StructureExplorationSanitizer missing (old-world safety net)")
    else:
        sanitizer=read(EVENT)
        for token in ("getStructureWithPieceAt","story_cache","ensureGuard","FUNCTIONAL_BLOCKS"):
            if token not in sanitizer: errors.append(f"sanitizer missing {token}")
    if not LOOT.exists(): errors.append("story_cache loot table missing")

    print("Matter Overdrive structure topology validator")
    print("Checked 16 structure families across 3 active exploration generators")
    for e in errors: print("FAIL:",e)
    if errors:
        print(f"RESULT: FAIL ({len(errors)} blocking issue(s))")
        return 1
    print("RESULT: PASS")
    return 0

if __name__=="__main__": sys.exit(main())
