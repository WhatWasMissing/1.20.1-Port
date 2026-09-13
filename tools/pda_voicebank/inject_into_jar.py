#!/usr/bin/env python3
"""Inject locally processed PDA WAV files into a built Matter Overdrive JAR.

Usage:
    python tools/pda_voicebank/inject_into_jar.py path/to/matteroverdrive-0.7.jar

The source bank defaults to src/main/resources/assets/matteroverdrive/pda_voice.
A new sibling JAR named <stem>-pda-voice.jar is written; the original JAR is untouched.
"""
from __future__ import annotations

from pathlib import Path
import shutil
import sys
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parents[2]
VOICE_DIR = ROOT / "src/main/resources/assets/matteroverdrive/pda_voice"
PREFIX = "assets/matteroverdrive/pda_voice/"


def main() -> int:
    if len(sys.argv) != 2:
        print("Usage: inject_into_jar.py <matteroverdrive-jar>", file=sys.stderr)
        return 2

    source_jar = Path(sys.argv[1]).expanduser().resolve()
    if not source_jar.is_file() or source_jar.suffix.lower() != ".jar":
        print(f"ERROR: JAR not found: {source_jar}", file=sys.stderr)
        return 2
    if not VOICE_DIR.is_dir():
        print(f"ERROR: voice directory not found: {VOICE_DIR}", file=sys.stderr)
        return 2

    wavs = sorted(VOICE_DIR.glob("*.wav"))
    if not wavs:
        print("ERROR: no processed PDA WAV files found. Run PROCESS_PDA_VOICE_BANK.bat first.", file=sys.stderr)
        return 2

    output = source_jar.with_name(source_jar.stem + "-pda-voice.jar")
    with tempfile.TemporaryDirectory(prefix="mo-pda-jar-") as tmp:
        staged = Path(tmp) / output.name
        with zipfile.ZipFile(source_jar, "r") as src, zipfile.ZipFile(staged, "w") as dst:
            for info in src.infolist():
                # Replace only locally generated PDA WAV entries. Keep manifest and all other assets intact.
                if info.filename.startswith(PREFIX) and info.filename.lower().endswith(".wav"):
                    continue
                dst.writestr(info, src.read(info.filename))
            for wav in wavs:
                dst.write(wav, PREFIX + wav.name, compress_type=zipfile.ZIP_DEFLATED)
        shutil.copy2(staged, output)

    print(f"Injected {len(wavs)} PDA voice clip(s).")
    print(f"Original: {source_jar}")
    print(f"Voiced:   {output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
