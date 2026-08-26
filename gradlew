#!/bin/sh
set -eu
GRADLE_VERSION=8.1.1
GRADLE_SHA256=e111cb9948407e26351227dabce49822fb88c37ee72f1d1582a69c68af2e702f
DIST_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
BOOT_ROOT="${HOME}/.gradle/matteroverdrive-bootstrap"
DIST_DIR="${BOOT_ROOT}/gradle-${GRADLE_VERSION}"
ZIP_FILE="${BOOT_ROOT}/gradle-${GRADLE_VERSION}-bin.zip"
GRADLE_BIN="${DIST_DIR}/bin/gradle"

if [ ! -x "$GRADLE_BIN" ]; then
  mkdir -p "$BOOT_ROOT"
  echo "[MatterOverdrive] Gradle ${GRADLE_VERSION} is not cached."
  echo "[MatterOverdrive] Downloading ${DIST_URL}"
  if command -v curl >/dev/null 2>&1; then
    curl -fL "$DIST_URL" -o "$ZIP_FILE"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP_FILE" "$DIST_URL"
  else
    echo "ERROR: curl or wget is required for the first Gradle bootstrap." >&2
    exit 2
  fi
  if command -v sha256sum >/dev/null 2>&1; then
    ACTUAL_SHA=$(sha256sum "$ZIP_FILE" | awk '{print $1}')
    [ "$ACTUAL_SHA" = "$GRADLE_SHA256" ] || { echo "ERROR: Gradle checksum mismatch." >&2; exit 3; }
  fi
  command -v unzip >/dev/null 2>&1 || { echo "ERROR: unzip is required." >&2; exit 4; }
  unzip -q -o "$ZIP_FILE" -d "$BOOT_ROOT"
  rm -f "$ZIP_FILE"
fi
exec "$GRADLE_BIN" "$@"
