#!/usr/bin/env python3
"""Apply Matter Overdrive PDA synthetic-VA treatment to raw neural voice clips.

Input files are named by PDA line ID and placed in tools/pda_voicebank/raw/.
Supported source formats: wav, mp3, ogg, flac, m4a.
Output is 48 kHz mono PCM WAV under src/main/resources/assets/matteroverdrive/pda_voice/.
The output name includes the inferred presentation profile, e.g. signal_echo.corrupted.wav.
Requires ffmpeg on PATH and no Python packages.
"""

from __future__ import annotations

from pathlib import Path
import shutil
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[2]
RAW = Path(__file__).resolve().parent / "raw"
OUT = ROOT / "src/main/resources/assets/matteroverdrive/pda_voice"
SUPPORTED = {".wav", ".mp3", ".ogg", ".flac", ".m4a"}


def profile_for(line_id: str) -> str:
    value = line_id.lower()
    if value == "closed_loop" or value == "signal_echo" or "echo9" in value or "lagrange" in value:
        return "corrupted"
    if "orpheus" in value or "black_site" in value:
        return "orpheus"
    if "anomaly" in value or "resonance" in value or "m0" in value:
        return "anomaly"
    if any(part in value for part in ("synthetic", "morrow", "chorus", "hephaestus")):
        return "synthetic"
    if any(part in value for part in ("warning", "pressure", "structural", "icarus")):
        return "hazard"
    if value.startswith("lore_") or value.startswith("site_") or "archive" in value \
            or value in {"record_recovered", "reconstruction_complete"}:
        return "archive"
    return "standard"


PARAMS = {
    "standard":  dict(rate1=48480, rate2=47520, delay1=18, delay2=31, ghost1=0.115, ghost2=0.075, echo=0.10, threshold=-18, ratio=2.15, lowpass=7800, tremolo=""),
    "hazard":    dict(rate1=48576, rate2=47424, delay1=15, delay2=26, ghost1=0.135, ghost2=0.090, echo=0.12, threshold=-20, ratio=2.45, lowpass=7400, tremolo=""),
    "anomaly":   dict(rate1=48864, rate2=47136, delay1=13, delay2=29, ghost1=0.165, ghost2=0.115, echo=0.17, threshold=-19, ratio=2.35, lowpass=7200, tremolo=""),
    "archive":   dict(rate1=48384, rate2=47616, delay1=20, delay2=34, ghost1=0.090, ghost2=0.055, echo=0.07, threshold=-17, ratio=2.00, lowpass=8000, tremolo=""),
    "orpheus":   dict(rate1=48624, rate2=47376, delay1=16, delay2=28, ghost1=0.145, ghost2=0.095, echo=0.13, threshold=-20, ratio=2.55, lowpass=6600, tremolo=""),
    "synthetic": dict(rate1=48720, rate2=47280, delay1=14, delay2=27, ghost1=0.155, ghost2=0.105, echo=0.11, threshold=-19, ratio=2.30, lowpass=7600, tremolo=""),
    "corrupted": dict(rate1=48960, rate2=47040, delay1=12, delay2=33, ghost1=0.175, ghost2=0.120, echo=0.19, threshold=-20, ratio=2.50, lowpass=6900, tremolo=",tremolo=f=9:d=0.10"),
}


def make_filter(profile: str) -> str:
    p = PARAMS[profile]
    return f"""
[0:a]aformat=sample_rates=48000:channel_layouts=mono,highpass=f=105,lowpass=f={p['lowpass']},
acompressor=threshold={p['threshold']}dB:ratio={p['ratio']}:attack=6:release=90,asplit=3[dry][g1][g2];
[dry]volume=1.0[d];
[g1]asetrate={p['rate1']},aresample=48000,adelay={p['delay1']}|{p['delay1']},highpass=f=240,lowpass=f=6400,volume={p['ghost1']}[g1p];
[g2]asetrate={p['rate2']},aresample=48000,adelay={p['delay2']}|{p['delay2']},highpass=f=300,lowpass=f=5900,
volume={p['ghost2']},aecho=0.8:0.55:21:{p['echo']}{p['tremolo']}[g2p];
[d][g1p][g2p]amix=inputs=3:normalize=0,alimiter=limit=0.92[voice];
[1:a]volume=0.075,afade=t=out:st=0.025:d=0.03[c1];
[2:a]adelay=68|68,volume=0.052,afade=t=out:st=0.04:d=0.04[c2];
[c1][c2]amix=inputs=2:normalize=0[chime];
[chime]apad=pad_dur=0.18[chp];
[voice]adelay=180|180[vdel];
[chp][vdel]amix=inputs=2:normalize=0,alimiter=limit=0.95[out]
""".replace("\n", "")


def main() -> int:
    ffmpeg = shutil.which("ffmpeg")
    if not ffmpeg:
        print("ERROR: ffmpeg is required on PATH", file=sys.stderr)
        return 2
    RAW.mkdir(parents=True, exist_ok=True)
    OUT.mkdir(parents=True, exist_ok=True)
    sources = sorted(p for p in RAW.iterdir() if p.is_file() and p.suffix.lower() in SUPPORTED)
    if not sources:
        print(f"No raw clips found in {RAW}")
        print("Name each source after its PDA line ID, e.g. icarus_warning.mp3")
        return 1

    failed = []
    for source in sources:
        profile = profile_for(source.stem)
        target = OUT / f"{source.stem}.{profile}.wav"
        command = [
            ffmpeg, "-y", "-hide_banner", "-loglevel", "error",
            "-i", str(source),
            "-f", "lavfi", "-i", "sine=frequency=790:duration=0.060:sample_rate=48000",
            "-f", "lavfi", "-i", "sine=frequency=1230:duration=0.085:sample_rate=48000",
            "-filter_complex", make_filter(profile),
            "-map", "[out]", "-ar", "48000", "-ac", "1", "-c:a", "pcm_s16le",
            str(target),
        ]
        result = subprocess.run(command, check=False)
        if result.returncode != 0:
            failed.append(source.name)
            print(f"FAILED  {source.name} [{profile}]")
        else:
            print(f"BUILT   {target.relative_to(ROOT)} [{profile}]")

    if failed:
        print("Voice bank processing failed for:", ", ".join(failed), file=sys.stderr)
        return 1
    print(f"Processed {len(sources)} PDA voice clip(s) with contextual profiles.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
