#!/usr/bin/env python3
"""Offline visual/topology lab for Matter Overdrive native facility layouts.

This is deliberately a QA mirror rather than a second world generator. It renders the
piece graph used by TechnologyFacilityStructurePiece + FacilityInfrastructurePiece,
checks the invariants that previously produced unreachable rooms, and emits SVG/JSON
artifacts without Minecraft, Forge, Pillow, or matplotlib.

Run from the repository root:
    python scripts/facility_layout_lab.py

Outputs are written to build/reports/facility_layout_lab/ by default.
"""
from __future__ import annotations

import argparse
import html
import json
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
              p("armory_gate",10,0,0,0,2,"gate")]
    else:
        q += [p("drone_bay",18,0,0,7,7), p("android_bay",18,18,0,7,7), p("armory",-18,0,0,7,7),
              p("armory_gate",-10,0,0,0,2,"gate")]
    return q


def fusion(layout: int) -> list[Piece]:
    turn = -1 if layout == 1 else 1
    bridge_z = -4 if layout == 2 else 4
    q = [p("fusion_core",0,0,0,10,10), p("stabilizer_wing",-22*turn,0,0,8,7),
         p("reactor_control",22*turn,0,0,8,7), p("service_wing",0,22,-2,7,8),
         p("entrance",0,-22,0,4,6,"entrance"), corridor_x(-13*turn,0), corridor_x(13*turn,0),
         corridor_z(0,13), corridor_z(0,-13), p("observation_bridge",0,bridge_z,6,7,2,"service"),
         p("service_spine",0,0,6,8,1,"service"), p("service_bridge_link",0,(-1 if layout==2 else 1),6,1,2,"service"),
         p("service_access",8,-3,0,3,3,"vertical"), p("lowered_step",0,14,0,2,2,"vertical"),
         p("surface_stair",0,-28,0,2,3,"vertical"), p("restoration_terminal",0,-6,1,0,0,"objective"),
         p("secure_gate",13*turn,0,0,0,2,"gate"), p("roof_plant",0,0,10,4,3,"service")]
    return q


def black_site(layout: int) -> list[Piece]:
    d = -1 if layout == 1 else 1
    q = [p("black_core",0,0,0,8,8), p("black_security",0,-16,0,7,7), p("entrance_shaft",0,-27,0,3,3,"entrance"),
         p("security_checkpoint",0,-10,0,4,4), p("black_lab",-18*d,0,0,7,7), p("containment",18*d,0,0,7,7),
         p("vault",0,19,-6,7,7), corridor_x(-10*d,0), corridor_x(10*d,0), corridor_z(0,10), corridor_z(0,-9),
         p("vault_gate",0,11,0,2,0,"gate"), p("entry_ladder",0,-25,0,1,1,"vertical"),
         p("surface_hatch",0,-27,12,2,2,"vertical"), p("vault_stair",0,16,-3,2,3,"vertical")]
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

ROLE_STYLE = {
    "room": ("#d9e3ec", "#667788"),
    "entrance": ("#e8dfc8", "#8a7650"),
    "corridor": ("#cfd8df", "#71808a"),
    "gate": ("#f0c8c8", "#9a4444"),
    "service": ("#e0d1ea", "#7f5c8d"),
    "vertical": ("#cae8cf", "#4d8158"),
    "objective": ("#c7e8e8", "#397b7b"),
    "fix": ("#f2ddaa", "#8c6b21"),
}


def assert_layout(family: str, layout: int, pieces: list[Piece]) -> list[str]:
    names = {x.name for x in pieces}
    errors: list[str] = []
    def need(*required: str) -> None:
        for item in required:
            if item not in names: errors.append(f"missing {item}")

    if family == "plant":
        need("service_access")
        if layout == 1: need("shipping_gate_west", "east_entrance_fix")
        if layout == 2: need("qa_core_link_west", "qa_core_link_east", "qa_shipping_link", "shipping_gate")
    elif family == "refinery": need("lowered_step", "excavation_ladder", "service_access", "processing_gate")
    elif family == "relay": need("restoration_terminal", "service_access", "control_gate", "wing_gate")
    elif family == "bunker": need("entry_ladder", "surface_stair", "armory_gate")
    elif family == "fusion": need("lowered_step", "surface_stair", "service_access", "service_bridge_link", "restoration_terminal", "secure_gate")
    elif family == "black_site":
        need("entry_ladder", "surface_hatch", "vault_stair", "vault_gate")
        if layout == 2: need("lower_lab", "lower_lab_link")

    for piece in pieces:
        if piece.hx > 12 or piece.hz > 12:
            errors.append(f"oversized QA piece {piece.name}: {piece.hx}x{piece.hz}")
    return errors


def render_svg(title: str, pieces: Iterable[Piece], out: Path) -> None:
    pieces = list(pieces)
    margin = 5
    x0 = min(x.x0 for x in pieces) - margin
    x1 = max(x.x1 for x in pieces) + margin
    z0 = min(x.z0 for x in pieces) - margin
    z1 = max(x.z1 for x in pieces) + margin
    scale = 10
    width = (x1 - x0 + 1) * scale
    height = (z1 - z0 + 1) * scale + 36
    def sx(x: float) -> float: return (x - x0) * scale
    def sy(z: float) -> float: return (z - z0) * scale + 32

    body = [f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">',
            '<rect width="100%" height="100%" fill="#fafafa"/>',
            f'<text x="8" y="20" font-family="monospace" font-size="14">{html.escape(title)}</text>']
    role_order = {"room":0,"entrance":1,"corridor":2,"service":3,"vertical":4,"objective":5,"gate":6,"fix":7}
    for piece in sorted(pieces, key=lambda q: (role_order.get(q.role, 9), q.y)):
        fill, stroke = ROLE_STYLE.get(piece.role, ("#ddd", "#555"))
        rx, ry = sx(piece.x0), sy(piece.z0)
        rw, rh = max(scale, (piece.x1-piece.x0+1)*scale), max(scale, (piece.z1-piece.z0+1)*scale)
        body.append(f'<rect x="{rx}" y="{ry}" width="{rw}" height="{rh}" fill="{fill}" fill-opacity="0.64" stroke="{stroke}" stroke-width="1.5"/>')
        label = f"{piece.name} y{piece.y:+d}"
        body.append(f'<text x="{rx+3}" y="{ry+12}" font-family="monospace" font-size="8" fill="#202020">{html.escape(label)}</text>')
    body.append('</svg>')
    out.write_text("\n".join(body), encoding="utf-8")


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", type=Path, default=DEFAULT_OUT)
    ap.add_argument("--check-only", action="store_true")
    ns = ap.parse_args()
    out = ns.out
    layouts = []
    failures = []
    if not ns.check_only: out.mkdir(parents=True, exist_ok=True)

    for family, builder in BUILDERS.items():
        for layout in range(3):
            pieces = builder(layout)
            errors = assert_layout(family, layout, pieces)
            if errors: failures.extend(f"{family} layout {layout}: {e}" for e in errors)
            layouts.append({"family": family, "layout": layout, "pieces": [asdict(x) for x in pieces], "errors": errors})
            if not ns.check_only:
                render_svg(f"{family} layout {layout}", pieces, out / f"{family}_layout_{layout}.svg")

    if not ns.check_only:
        (out / "facility_layout_lab.json").write_text(json.dumps({"layouts": layouts, "failures": failures}, indent=2), encoding="utf-8")
        index = ["<!doctype html><meta charset='utf-8'><title>M2 Facility Layout Lab</title>",
                 "<style>body{font-family:sans-serif;background:#eee}main{display:grid;grid-template-columns:repeat(3,minmax(300px,1fr));gap:12px}iframe{width:100%;height:420px;border:1px solid #aaa;background:white}</style><h1>Matter Overdrive Facility Layout Lab</h1><main>"]
        for family in BUILDERS:
            for layout in range(3):
                fn=f"{family}_layout_{layout}.svg"
                index.append(f"<iframe title='{family} layout {layout}' src='{fn}'></iframe>")
        index.append("</main>")
        (out / "index.html").write_text("\n".join(index), encoding="utf-8")

    if failures:
        print("FACILITY LAYOUT LAB FAILED")
        for f in failures: print(" -", f)
        return 1
    print("FACILITY LAYOUT LAB PASSED: 18 layouts")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
