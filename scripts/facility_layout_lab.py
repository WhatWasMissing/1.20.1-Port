#!/usr/bin/env python3
"""Matter Overdrive facility QA v2.

Offline architectural QA for the six native Forge 1.20.1 technology facilities.
The lab mirrors layout placement, then checks player-scale reachability across room
floors, corridors, one/two block level transitions, ladders/stairs, service access,
and restoration-gated thresholds. It also emits plan + elevation SVGs and a JSON
report. No Minecraft/Forge imports are required.

The mirror is intentionally guarded against Java drift: when run from the repo,
source markers for the critical traversal pieces must still exist in the real
StructurePiece assembly code.
"""
from __future__ import annotations

import argparse
import html
import json
from collections import deque
from dataclasses import asdict, dataclass
from pathlib import Path
from typing import Iterable

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_OUT = ROOT / "build/reports/facility_layout_lab"

@dataclass(frozen=True)
class Piece:
    name: str
    x: int
    z: int
    y: int
    hx: int
    hz: int
    role: str = "room"

    @property
    def x0(self) -> int: return self.x - self.hx
    @property
    def x1(self) -> int: return self.x + self.hx
    @property
    def z0(self) -> int: return self.z - self.hz
    @property
    def z1(self) -> int: return self.z + self.hz


def p(name: str, x: int, z: int, y: int, hx: int, hz: int, role: str = "room") -> Piece:
    return Piece(name, x, z, y, hx, hz, role)


def corridor_x(x: int, z: int, y: int = 0, name: str = "corridor_x") -> Piece:
    return p(name, x, z, y, 5, 2, "corridor")


def corridor_z(x: int, z: int, y: int = 0, name: str = "corridor_z") -> Piece:
    return p(name, x, z, y, 2, 5, "corridor")


def plant(layout: int) -> list[Piece]:
    q = [p("manufacturing_core",0,0,0,7,7)]
    if layout == 0:
        q += [p("fabrication",-18,0,0,7,6), p("assembly",18,0,0,7,6), p("shipping",0,19,0,6,7),
              p("entrance",0,-18,0,4,6,"entrance"), corridor_x(-10,0), corridor_x(10,0),
              corridor_z(0,11), corridor_z(0,-10), p("shipping_gate",0,12,0,2,0,"gate")]
    elif layout == 1:
        q += [p("fabrication",-18,0,0,7,6), p("assembly",-18,18,0,7,6), p("shipping",0,18,0,6,7),
              p("entrance",18,0,0,4,6,"entrance"), corridor_x(-10,0), corridor_z(-18,9),
              corridor_x(-9,18), corridor_x(10,0), p("shipping_gate_west",-6,18,0,0,2,"gate"),
              p("east_entrance_fix",14,0,0,1,1,"fix")]
    else:
        q += [p("fabrication",-19,13,0,7,6), p("assembly",19,13,0,7,6), p("shipping",0,24,0,6,7),
              p("entrance",0,-18,0,4,6,"entrance"), corridor_x(-10,9), corridor_x(10,9),
              corridor_z(0,16), corridor_z(0,-10), corridor_x(-12,0,name="qa_core_link_west"),
              corridor_z(-18,6,name="qa_west_turn"), corridor_x(12,0,name="qa_core_link_east"),
              corridor_z(18,6,name="qa_east_turn"), corridor_z(0,11,name="qa_shipping_link"),
              p("shipping_gate",0,17,0,2,0,"gate")]
    q += [p("service_gantry",0,10,5,8,1,"service"), p("service_spine",0,10,5,8,1,"service"),
          p("service_access",-8,7,0,3,3,"vertical")]
    return q


def refinery(layout: int) -> list[Piece]:
    side = 1 if layout == 1 else -1
    q = [p("refinery_core",0,0,0,8,8), p("excavation",19*side,0,-2,7,7), p("storage",-19*side,0,0,7,7),
         p("processing",0,19,0,7,7), p("entrance",0,-18,0,4,6,"entrance"),
         corridor_x(10*side,0), corridor_x(-10*side,0), corridor_z(0,10), corridor_z(0,-10),
         p("excavation_shaft",26*side,0,-7,3,3,"vertical"), p("excavation_ladder",26*side,2,-7,1,1,"vertical"),
         p("lowered_step",12*side,0,0,2,2,"vertical"), p("service_gantry",0,10,5,1,8,"service"),
         p("service_spine",0,10,5,1,8,"service"), p("service_access",-3,18,0,3,3,"vertical"),
         p("processing_gate",0,12,0,2,0,"gate")]
    if layout == 2: q.append(p("roof_plant",0,-3,7,4,3,"service"))
    return q


def relay(layout: int) -> list[Piece]:
    d = -1 if layout == 1 else 1
    q = [p("quantum_core",0,0,0,7,7), p("relay_wing",-16*d,10,0,6,6), p("power_wing",16*d,10,0,6,6),
         p("control_wing",0,-16,0,6,6), p("entrance",0,-28,0,4,6,"entrance"),
         corridor_x(-9*d,6), corridor_x(9*d,6), corridor_z(0,-9), corridor_z(0,-22),
         p("control_gate",0,-10,0,2,0,"gate"), p("wing_gate",9*d,6,0,0,2,"gate"),
         p("restoration_terminal",0,3,1,0,0,"objective"), p("service_spine",0,8,7,8,1,"service"),
         p("service_access",-8,5,0,3,3,"vertical"), p("relay_mast_a",-18*d,11,5,2,2,"service"),
         p("relay_mast_b",18*d,11,5,2,2,"service")]
    if layout == 2: q.append(p("observation_bridge",0,8,7,7,2,"service"))
    return q


def bunker(layout: int) -> list[Piece]:
    q = [p("bunker_command",0,0,0,8,8), p("entrance",0,-24,7,4,6,"entrance"),
         p("security_checkpoint",0,-14,0,4,4), corridor_z(0,-8), corridor_x(-10,2), corridor_x(10,2),
         corridor_z(0,11), p("entry_ladder",0,-19,0,1,1,"vertical"), p("surface_stair",0,-30,7,2,3,"vertical")]
    if layout == 0:
        q += [p("drone_bay",-18,3,0,7,7), p("android_bay",18,3,0,7,7), p("armory",0,19,0,7,7),
              p("armory_gate",0,12,0,2,0,"gate")]
    elif layout == 1:
        q += [p("drone_bay",-18,0,0,7,7), p("android_bay",-18,18,0,7,7), p("armory",18,0,0,7,7),
              p("armory_gate",10,0,0,0,2,"gate"), corridor_z(-18,9,name="bunker_android_link")]
    else:
        q += [p("drone_bay",18,0,0,7,7), p("android_bay",18,18,0,7,7), p("armory",-18,0,0,7,7),
              p("armory_gate",-10,0,0,0,2,"gate"), corridor_z(18,9,name="bunker_android_link")]
    return q


def fusion(layout: int) -> list[Piece]:
    turn = -1 if layout == 1 else 1
    bridge_z = -4 if layout == 2 else 4
    return [p("fusion_core",0,0,0,10,10), p("stabilizer_wing",-22*turn,0,0,8,7),
            p("reactor_control",22*turn,0,0,8,7), p("service_wing",0,22,-2,7,8),
            p("entrance",0,-22,0,4,6,"entrance"), corridor_x(-13*turn,0), corridor_x(13*turn,0),
            corridor_z(0,13), corridor_z(0,-13), p("observation_bridge",0,bridge_z,6,7,2,"service"),
            p("service_spine",0,0,6,8,1,"service"), p("service_bridge_link",0,(-1 if layout==2 else 1),6,1,2,"service"),
            p("service_access",8,-3,0,3,3,"vertical"), p("lowered_step",0,14,0,2,2,"vertical"),
            p("surface_stair",0,-28,0,2,3,"vertical"), p("restoration_terminal",0,-6,1,0,0,"objective"),
            p("secure_gate",13*turn,0,0,0,2,"gate"), p("roof_plant",0,0,10,4,3,"service")]


def black_site(layout: int) -> list[Piece]:
    d = -1 if layout == 1 else 1
    q = [p("black_core",0,0,0,8,8), p("black_security",0,-16,0,7,7), p("entrance_shaft",0,-27,12,3,3,"entrance"),
         p("security_checkpoint",0,-10,0,4,4), p("black_lab",-18*d,0,0,7,7), p("containment",18*d,0,0,7,7),
         p("vault",0,19,-6,7,7), corridor_x(-10*d,0), corridor_x(10*d,0), corridor_z(0,10), corridor_z(0,-9),
         p("vault_gate",0,11,0,2,0,"gate"), p("entry_ladder",0,-25,0,1,1,"vertical"),
         p("surface_hatch",0,-27,12,2,2,"vertical"), p("vault_stair",0,13,0,2,6,"vertical")]
    if layout == 2:
        q += [p("lower_lab",-18,19,-6,7,7), corridor_x(-9,19,-6,"lower_lab_link")]
    return q

BUILDERS = {
    "plant": plant,
    "refinery": refinery,
    "relay": relay,
    "bunker": bunker,
    "fusion": fusion,
    "black_site": black_site,
}

REQUIRED = {
    "plant": {"manufacturing_core", "fabrication", "assembly", "shipping"},
    "refinery": {"refinery_core", "excavation", "storage", "processing", "excavation_shaft"},
    "relay": {"quantum_core", "relay_wing", "power_wing", "control_wing"},
    "bunker": {"bunker_command", "security_checkpoint", "drone_bay", "android_bay", "armory"},
    "fusion": {"fusion_core", "stabilizer_wing", "reactor_control", "service_wing"},
    "black_site": {"black_core", "black_security", "black_lab", "containment", "vault", "security_checkpoint"},
}

ROLE_STYLE = {
    "room": ("#d9e3ec", "#667788"), "entrance": ("#e8dfc8", "#8a7650"),
    "corridor": ("#cfd8df", "#71808a"), "gate": ("#f0c8c8", "#9a4444"),
    "service": ("#e0d1ea", "#7f5c8d"), "vertical": ("#cae8cf", "#4d8158"),
    "objective": ("#c7e8e8", "#397b7b"), "fix": ("#f2ddaa", "#8c6b21"),
}

# Critical strings make accidental divergence between this mirror and the Java assembler visible.
SOURCE_MARKERS = {
    "src/main/java/matteroverdrive/worldgen/TechnologyFacilityStructurePiece.java": [
        "c.offset(-19, 0, 13)", "c.offset(19, 0, 13)", "c.offset(0, -6, 19)",
        "Room.EXCAVATION_SHAFT", "Room.OBSERVATION_BRIDGE",
    ],
    "src/main/java/matteroverdrive/worldgen/FacilityInfrastructurePiece.java": [
        "Kind.SERVICE_ACCESS", "Kind.LOWERED_STEP_EAST", "Kind.LOWERED_STEP_WEST",
        "Kind.LADDER_DOWN_7", "Kind.LADDER_DOWN_12", "Kind.BLACK_SURFACE_HATCH",
        "Kind.BLACK_VAULT_STAIR", "Kind.BLACK_LOWER_CORRIDOR_X",
        "Kind.PLANT_EAST_ENTRANCE_FIX", "Kind.SERVICE_BRIDGE_LINK_NORTH",
    ],
    "src/main/java/matteroverdrive/worldgen/FacilityTerrainPiece.java": [
        "Kind.ENTRY_APRON_NORTH", "Kind.SALVAGE_FOUNDATION", "Kind.BLACK_HATCH_CROWN",
        "Kind.BUNKER_ANDROID_LINK_Z", "c.offset(-18, 0, 9)", "c.offset(18, 0, 9)",
    ],
}


def vertical_levels(piece: Piece) -> set[int]:
    name = piece.name
    if name == "service_access":
        top = 7 if piece.x == -8 and piece.z == 5 else 6 if piece.x == 8 and piece.z == -3 else 5
        return set(range(piece.y, piece.y + top + 1))
    if name == "entry_ladder":
        return set(range(piece.y, 13 if piece.y == 0 and piece.z <= -25 else 8))
    if name == "excavation_ladder": return set(range(piece.y, piece.y + 13))
    if name == "lowered_step": return {piece.y, piece.y - 1, piece.y - 2}
    if name == "vault_stair": return set(range(piece.y - 6, piece.y + 1))
    if name == "surface_hatch": return set(range(piece.y, piece.y + 5))
    if name == "surface_stair": return {piece.y, piece.y + 1, piece.y + 2}
    return {piece.y}


def axis_gap(a0: int, a1: int, b0: int, b1: int) -> int:
    if a1 < b0: return b0 - a1 - 1
    if b1 < a0: return a0 - b1 - 1
    return 0


def horizontally_close(a: Piece, b: Piece, allowance: int = 1) -> bool:
    return axis_gap(a.x0, a.x1, b.x0, b.x1) <= allowance and axis_gap(a.z0, a.z1, b.z0, b.z1) <= allowance


def connected(a: Piece, b: Piece) -> bool:
    if not horizontally_close(a, b, 1): return False
    la, lb = vertical_levels(a), vertical_levels(b)
    if la & lb: return True
    # A one-block ordinary step is walkable. Larger changes require an explicit vertical piece.
    if min(abs(x-y) for x in la for y in lb) <= 1:
        return True
    return False


def graph_for(pieces: list[Piece], gates_open: bool) -> dict[int, set[int]]:
    graph = {i:set() for i in range(len(pieces))}
    for i, a in enumerate(pieces):
        if a.role == "gate" and not gates_open: continue
        for j in range(i+1, len(pieces)):
            b = pieces[j]
            if b.role == "gate" and not gates_open: continue
            if connected(a,b):
                graph[i].add(j); graph[j].add(i)
    return graph


def reachable(pieces: list[Piece], gates_open: bool) -> set[int]:
    graph = graph_for(pieces, gates_open)
    starts = [i for i,piece in enumerate(pieces) if piece.role == "entrance"]
    seen = set(starts); queue = deque(starts)
    while queue:
        i = queue.popleft()
        for j in graph[i]:
            if j not in seen:
                seen.add(j); queue.append(j)
    return seen


def assert_layout(family: str, layout: int, pieces: list[Piece]) -> tuple[list[str], dict]:
    errors: list[str] = []
    names = {x.name for x in pieces}
    post = reachable(pieces, True)
    pre = reachable(pieces, False)
    post_names = {pieces[i].name for i in post}
    pre_names = {pieces[i].name for i in pre}

    required = set(REQUIRED[family])
    if family == "black_site" and layout == 2: required.add("lower_lab")
    missing = sorted(required - post_names)
    if missing: errors.append("unreachable after restoration: " + ", ".join(missing))

    if family in {"relay", "fusion"} and "restoration_terminal" not in pre_names:
        errors.append("restoration terminal is not reachable before secure gates open")

    if not any(x.role == "entrance" for x in pieces): errors.append("no entrance piece")
    for piece in pieces:
        if piece.hx > 12 or piece.hz > 12:
            errors.append(f"oversized QA piece {piece.name}: half-extents {piece.hx}x{piece.hz}")

    # Specific regressions that are easy to hide in a generic graph.
    if family == "plant" and layout == 1 and "east_entrance_fix" not in names: errors.append("missing east entrance opening")
    if family == "plant" and layout == 2:
        for required_name in ("qa_core_link_west","qa_core_link_east","qa_shipping_link"):
            if required_name not in names: errors.append(f"missing {required_name}")
    if family == "bunker" and "entry_ladder" not in names: errors.append("missing bunker shaft ladder")
    if family == "black_site" and not {"entry_ladder","surface_hatch","vault_stair"}.issubset(names):
        errors.append("black site missing one or more vertical access pieces")

    return errors, {
        "pre_restore_reachable": sorted(pre_names),
        "post_restore_reachable": sorted(post_names),
        "required": sorted(required),
        "post_restore_coverage": round(len(required & post_names) / max(1, len(required)), 3),
    }


def validate_source_markers(root: Path) -> list[str]:
    failures: list[str] = []
    for rel, markers in SOURCE_MARKERS.items():
        path = root / rel
        if not path.exists():
            failures.append(f"source drift check missing file: {rel}")
            continue
        text = path.read_text(encoding="utf-8")
        for marker in markers:
            if marker not in text: failures.append(f"source drift: {rel} missing marker {marker}")
    return failures


def render_plan(title: str, pieces: list[Piece], reachable_ids: set[int], out: Path) -> None:
    margin=5; x0=min(x.x0 for x in pieces)-margin; x1=max(x.x1 for x in pieces)+margin
    z0=min(x.z0 for x in pieces)-margin; z1=max(x.z1 for x in pieces)+margin; scale=10
    width=(x1-x0+1)*scale; height=(z1-z0+1)*scale+36
    sx=lambda x:(x-x0)*scale; sy=lambda z:(z-z0)*scale+32
    body=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}">',
          '<rect width="100%" height="100%" fill="#fafafa"/>',
          f'<text x="8" y="20" font-family="monospace" font-size="14">{html.escape(title)}</text>']
    for i,piece in enumerate(pieces):
        fill,stroke=ROLE_STYLE.get(piece.role,("#ddd","#555")); opacity=.72 if i in reachable_ids else .20
        rx,ry=sx(piece.x0),sy(piece.z0); rw=max(scale,(piece.x1-piece.x0+1)*scale); rh=max(scale,(piece.z1-piece.z0+1)*scale)
        body.append(f'<rect x="{rx}" y="{ry}" width="{rw}" height="{rh}" fill="{fill}" fill-opacity="{opacity}" stroke="{stroke}" stroke-width="1.5"/>')
        body.append(f'<text x="{rx+3}" y="{ry+12}" font-family="monospace" font-size="8">{html.escape(piece.name)} y{piece.y:+d}</text>')
    body.append('</svg>'); out.write_text("\n".join(body),encoding="utf-8")


def render_elevation(title: str, pieces: list[Piece], out: Path) -> None:
    levels=sorted({level for piece in pieces for level in vertical_levels(piece)})
    width=900; row=36; height=54+len(levels)*row
    body=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}">','<rect width="100%" height="100%" fill="#fafafa"/>',
          f'<text x="8" y="20" font-family="monospace" font-size="14">{html.escape(title)} elevation/access bands</text>']
    for idx,y in enumerate(reversed(levels)):
        yy=42+idx*row; body.append(f'<line x1="42" y1="{yy}" x2="890" y2="{yy}" stroke="#ddd"/>')
        body.append(f'<text x="5" y="{yy+4}" font-family="monospace" font-size="10">y{y:+d}</text>')
        names=[piece.name for piece in pieces if y in vertical_levels(piece)]
        body.append(f'<text x="48" y="{yy+4}" font-family="monospace" font-size="9">{html.escape(" | ".join(names))}</text>')
    body.append('</svg>'); out.write_text("\n".join(body),encoding="utf-8")


def main() -> int:
    ap=argparse.ArgumentParser(); ap.add_argument("--out",type=Path,default=DEFAULT_OUT); ap.add_argument("--check-only",action="store_true")
    ap.add_argument("--skip-source-drift",action="store_true",help="skip Java mirror marker checks (useful for isolated script tests)")
    ns=ap.parse_args(); failures=[]; layouts=[]
    if not ns.skip_source_drift: failures.extend(validate_source_markers(ROOT))
    if not ns.check_only: ns.out.mkdir(parents=True,exist_ok=True)

    for family,builder in BUILDERS.items():
        for layout in range(3):
            pieces=builder(layout); errors,access=assert_layout(family,layout,pieces)
            failures.extend(f"{family} layout {layout}: {e}" for e in errors)
            post=reachable(pieces,True)
            layouts.append({"family":family,"layout":layout,"pieces":[asdict(x) for x in pieces],"access":access,"errors":errors})
            if not ns.check_only:
                render_plan(f"{family} layout {layout} - post-restoration reachability",pieces,post,ns.out/f"{family}_layout_{layout}.svg")
                render_elevation(f"{family} layout {layout}",pieces,ns.out/f"{family}_layout_{layout}_elevation.svg")

    if not ns.check_only:
        payload={"version":2,"layouts":layouts,"failures":failures}
        (ns.out/"facility_layout_lab.json").write_text(json.dumps(payload,indent=2),encoding="utf-8")
        index=["<!doctype html><meta charset='utf-8'><title>M2 Facility Layout Lab v2</title>",
               "<style>body{font-family:sans-serif;background:#eee}main{display:grid;grid-template-columns:repeat(2,minmax(380px,1fr));gap:12px}iframe{width:100%;height:440px;border:1px solid #aaa;background:white}</style><h1>Matter Overdrive Facility Layout Lab v2</h1><p>Opaque pieces are reachable from the entrance after restoration. Each plan has an elevation/access-band view.</p><main>"]
        for family in BUILDERS:
            for layout in range(3):
                index += [f"<iframe title='{family} {layout}' src='{family}_layout_{layout}.svg'></iframe>",
                          f"<iframe title='{family} {layout} elevation' src='{family}_layout_{layout}_elevation.svg'></iframe>"]
        index.append("</main>"); (ns.out/"index.html").write_text("\n".join(index),encoding="utf-8")

    if failures:
        print("FACILITY LAYOUT LAB V2 FAILED")
        for failure in failures: print(" -",failure)
        return 1
    print("FACILITY LAYOUT LAB V2 PASSED: 18 layouts; player-scale reachability verified")
    return 0

if __name__ == "__main__": raise SystemExit(main())
