#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${ROOT_DIR}"

OS_NAME="$(uname -s)"
IS_WSL="false"
if grep -qi "microsoft" /proc/version 2>/dev/null; then
  IS_WSL="true"
fi

case "${OS_NAME}" in
  MINGW*|MSYS*|CYGWIN*)
    ;;
  Linux)
    if [[ "${IS_WSL}" != "true" ]]; then
      printf '[WARN] run_windows.sh is optimized for Git Bash/MSYS/Cygwin or WSL.\n' >&2
    fi
    ;;
  *)
    printf '[WARN] run_windows.sh is optimized for Windows shells. Current platform: %s\n' "${OS_NAME}" >&2
    ;;
esac

# Windows defaults (TCP is most reliable for MySQL on Windows).
export MYSQL_CONNECTION_MODE="${MYSQL_CONNECTION_MODE:-tcp}"
export MYSQL_USE_SUDO="${MYSQL_USE_SUDO:-false}"
export DB_HOST="${DB_HOST:-127.0.0.1}"
export DB_PORT="${DB_PORT:-3306}"

exec bash "${ROOT_DIR}/run_from_scratch.sh" "$@"
