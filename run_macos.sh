#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${ROOT_DIR}"

if [[ "$(uname -s)" != "Darwin" ]]; then
  printf '[WARN] run_macos.sh is optimized for macOS. Current platform: %s\n' "$(uname -s)" >&2
fi

# macOS defaults (Homebrew MySQL commonly supports socket auth).
export MYSQL_CONNECTION_MODE="${MYSQL_CONNECTION_MODE:-socket}"
export MYSQL_USE_SUDO="${MYSQL_USE_SUDO:-false}"
export DB_HOST="${DB_HOST:-127.0.0.1}"
export DB_PORT="${DB_PORT:-3306}"

exec bash "${ROOT_DIR}/run_from_scratch.sh" "$@"
