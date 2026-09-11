#!/usr/bin/env python3
"""Apply the approved Matter Overdrive PDA synthetic-VA treatment to raw neural voice clips.

Input files are named by PdaVoiceLineCatalog ID and placed in tools/pda_voicebank/raw/.
Supported source formats: wav, mp3, ogg, flac, m4a.
Output is 48 kHz mono PCM WAV under src/main/resources/assets/matteroverdrive/pda_voice/.
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

FILTER = r"""
[0:a]aformat=sample_rates=48000:channel_layouts=mono,highpass=f=105,lowpass=f=7800,
acompressor=threshold=-18dB:ratio=2.15:attack=6:release=90,asplit=3[dry][g1][g2];
[dry]volume=1.0[d];
[g1]asetrate=48480,aresample=48000,adelay=18|18,highpass=f=240,lowpass=f=6400,volume=0.115[g1p];
[g2]asetrate=47520,aresample=48000,adelay=31|31,highpass=f=300,lowpass=f=5900,
volume=0.075,aecho=0.8:0.55:21:0.10[g2p];
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
        print("Name each source after its PdaVoiceLineCatalog ID, e.g. icarus_warning.mp3")
        return 1

    failed = []
    for source in sources:
        target = OUT / f"{source.stem}.wav"
        command = [
            ffmpeg, "-y", "-hide_banner", "-loglevel", "error",
            "-i", str(source),
            "-f", "lavfi", "-i", "sine=frequency=790:duration=0.060:sample_rate=48000",
            "-f", "lavfi", "-i", "sine=frequency=1230:duration=0.085:sample_rate=48000",
            "-filter_complex", FILTER,
            "-map", "[out]", "-ar", "48000", "-ac", "1", "-c:a", "pcm_s16le",
            str(target),
        ]
        result = subprocess.run(command, check=False)
        if result.returncode != 0:
            failed.append(source.name)
            print(f"FAILED  {source.name}")
        else:
            print(f"BUILT   {target.relative_to(ROOT)}")

    if failed:
        print("Voice bank processing failed for:", ", ".join(failed), file=sys.stderr)
        return 1
    print(f"Processed {len(sources)} PDA voice clip(s).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
