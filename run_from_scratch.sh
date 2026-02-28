#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${ROOT_DIR}"

# Runtime defaults. Override by exporting variables before running this script.
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-feedback_ai}"
DB_USERNAME="${DB_USERNAME:-feedback_user}"
DB_PASSWORD="${DB_PASSWORD:-FeedbackUser#2026Aa!}"
MYSQL_ADMIN_USER="${MYSQL_ADMIN_USER:-root}"
MYSQL_ADMIN_PASSWORD="${MYSQL_ADMIN_PASSWORD:-}"
MYSQL_CONNECTION_MODE="${MYSQL_CONNECTION_MODE:-auto}" # auto|tcp|socket
MYSQL_USE_SUDO="${MYSQL_USE_SUDO:-false}"
APP_ADMIN_USERNAME="${APP_ADMIN_USERNAME:-admin}"
APP_ADMIN_PASSWORD="${APP_ADMIN_PASSWORD:-ChangeMeNow123!}"
MODEL_ARTIFACTS_DIR="${MODEL_ARTIFACTS_DIR:-${ROOT_DIR}/artifacts}"
CSV_MAX_BYTES="${CSV_MAX_BYTES:-5242880}"
BACKEND_PORT="${BACKEND_PORT:-8080}"
FRONTEND_PORT="${FRONTEND_PORT:-5173}"
VITE_API_BASE="${VITE_API_BASE:-http://localhost:${BACKEND_PORT}}"
RUN_BOOTSTRAP_TRAINING="${RUN_BOOTSTRAP_TRAINING:-false}"
SKIP_DB_BOOTSTRAP="${SKIP_DB_BOOTSTRAP:-false}"
SPRING_BOOT_PLUGIN_VERSION="${SPRING_BOOT_PLUGIN_VERSION:-3.3.5}"
MAVEN_REPO_LOCAL="${MAVEN_REPO_LOCAL:-${ROOT_DIR}/.m2/repository}"
DEFAULT_JWT_SECRET="LlyffxRWhfAq9HYtj+DXX9yyEelkvm6V6H89KK/2Hus="

# Keep a stable default secret across restarts to avoid invalidating existing sessions.
JWT_SECRET="${JWT_SECRET:-${DEFAULT_JWT_SECRET}}"

BACKEND_PID=""
FRONTEND_PID=""
MYSQL_MODE_RESOLVED=""
JAVA_DETECTED_RELEASE=""
MAVEN_JAVA_ARGS=()
MAVEN_COMMON_ARGS=()
RUN_DIR="${ROOT_DIR}/.run"
BACKEND_LOG="${RUN_DIR}/backend.log"
FRONTEND_LOG="${RUN_DIR}/frontend.log"

info() {
  printf '[INFO] %s\n' "$*"
}

error() {
  printf '[ERROR] %s\n' "$*" >&2
}

prompt_mysql_credentials() {
  # Ask only in interactive terminals and only when DB bootstrap is enabled.
  if [[ "${SKIP_DB_BOOTSTRAP}" == "true" ]] || [[ ! -t 0 ]]; then
    return 0
  fi

  local input_user
  local input_pass

  read -r -p "MySQL admin username [${MYSQL_ADMIN_USER}]: " input_user
  if [[ -n "${input_user}" ]]; then
    MYSQL_ADMIN_USER="${input_user}"
  fi

  if [[ -z "${MYSQL_ADMIN_PASSWORD}" ]]; then
    read -rs -p "MySQL password for ${MYSQL_ADMIN_USER} (leave empty to try socket/sudo): " input_pass
    printf '\n'
    MYSQL_ADMIN_PASSWORD="${input_pass}"
  fi

  read -r -p "Application DB username [${DB_USERNAME}]: " input_user
  if [[ -n "${input_user}" ]]; then
    DB_USERNAME="${input_user}"
  fi

  read -rs -p "Application DB password for ${DB_USERNAME} (leave empty to keep current/default): " input_pass
  printf '\n'
  if [[ -n "${input_pass}" ]]; then
    DB_PASSWORD="${input_pass}"
  fi
}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    error "Missing required command: $1"
    exit 1
  fi
}

detect_java_release() {
  local version_text
  local version_num

  version_text="$(javac -version 2>&1 || true)"
  version_num="$(printf '%s' "${version_text}" | awk '{print $2}' | cut -d'.' -f1)"

  if [[ -z "${version_num}" || ! "${version_num}" =~ ^[0-9]+$ ]]; then
    error "Unable to detect javac release from: ${version_text}"
    exit 1
  fi

  JAVA_DETECTED_RELEASE="${version_num}"
}

configure_java_for_maven() {
  detect_java_release
  if (( JAVA_DETECTED_RELEASE < 17 )); then
    error "Detected JDK ${JAVA_DETECTED_RELEASE}. This project requires JDK 17+."
    error "Install JDK 21 (recommended) or at least JDK 17."
    exit 1
  fi

  if (( JAVA_DETECTED_RELEASE >= 21 )); then
    info "Detected JDK ${JAVA_DETECTED_RELEASE}. Using project default Java 21 build settings."
    MAVEN_JAVA_ARGS=(-Djava.version=21 -Dmaven.compiler.release=21)
    return 0
  fi

  info "Detected JDK ${JAVA_DETECTED_RELEASE}. Falling back Maven compile target to ${JAVA_DETECTED_RELEASE}."
  MAVEN_JAVA_ARGS=(-Djava.version="${JAVA_DETECTED_RELEASE}" -Dmaven.compiler.release="${JAVA_DETECTED_RELEASE}")
}

sql_escape() {
  printf '%s' "$1" | sed "s/'/''/g"
}

mysql_exec_tcp() {
  local sql="$1"
  if [[ -n "${MYSQL_ADMIN_PASSWORD}" ]]; then
    mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${MYSQL_ADMIN_USER}" "-p${MYSQL_ADMIN_PASSWORD}" -e "${sql}"
  else
    mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${MYSQL_ADMIN_USER}" -e "${sql}"
  fi
}

mysql_exec_socket() {
  local sql="$1"
  if [[ -n "${MYSQL_ADMIN_PASSWORD}" ]]; then
    mysql -u "${MYSQL_ADMIN_USER}" "-p${MYSQL_ADMIN_PASSWORD}" -e "${sql}"
  else
    mysql -u "${MYSQL_ADMIN_USER}" -e "${sql}"
  fi
}

mysql_exec_sudo() {
  local sql="$1"
  sudo mysql -e "${sql}"
}

mysql_exec_app() {
  local sql="$1"
  if [[ -n "${DB_PASSWORD}" ]]; then
    mysql --protocol=TCP -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USERNAME}" "-p${DB_PASSWORD}" -e "${sql}"
  else
    mysql --protocol=TCP -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USERNAME}" -e "${sql}"
  fi
}

can_use_app_database() {
  mysql_exec_app "USE \`${DB_NAME}\`; SELECT 1;" >/dev/null 2>&1
}

generate_strong_password() {
  local random_part
  random_part="$(openssl rand -base64 32 | tr -dc 'A-Za-z0-9' | head -c 16)"
  printf '%s' "${random_part}Aa1!"
}

resolve_mysql_mode() {
  if [[ "${MYSQL_USE_SUDO}" == "true" ]]; then
    if mysql_exec_sudo "SELECT 1;" >/dev/null 2>&1; then
      MYSQL_MODE_RESOLVED="sudo"
      return 0
    fi
    return 1
  fi

  if [[ "${MYSQL_CONNECTION_MODE}" == "tcp" ]]; then
    if mysql_exec_tcp "SELECT 1;" >/dev/null 2>&1; then
      MYSQL_MODE_RESOLVED="tcp"
      return 0
    fi
    return 1
  fi

  if [[ "${MYSQL_CONNECTION_MODE}" == "socket" ]]; then
    if mysql_exec_socket "SELECT 1;" >/dev/null 2>&1; then
      MYSQL_MODE_RESOLVED="socket"
      return 0
    fi
    return 1
  fi

  # auto mode: try socket first for Ubuntu auth_socket root, then TCP.
  if mysql_exec_socket "SELECT 1;" >/dev/null 2>&1; then
    MYSQL_MODE_RESOLVED="socket"
    return 0
  fi
  if mysql_exec_tcp "SELECT 1;" >/dev/null 2>&1; then
    MYSQL_MODE_RESOLVED="tcp"
    return 0
  fi
  # Final auto fallback for Ubuntu setups where MySQL root is managed via sudo.
  if [[ "${MYSQL_ADMIN_USER}" == "root" ]] && command -v sudo >/dev/null 2>&1; then
    if mysql_exec_sudo "SELECT 1;" >/dev/null 2>&1; then
      MYSQL_MODE_RESOLVED="sudo"
      return 0
    fi
  fi
  return 1
}

mysql_exec() {
  local sql="$1"
  if [[ "${MYSQL_MODE_RESOLVED}" == "sudo" ]]; then
    mysql_exec_sudo "${sql}"
  elif [[ "${MYSQL_MODE_RESOLVED}" == "socket" ]]; then
    mysql_exec_socket "${sql}"
  elif [[ "${MYSQL_MODE_RESOLVED}" == "tcp" ]]; then
    mysql_exec_tcp "${sql}"
  else
    error "Internal error: MySQL mode not resolved."
    exit 1
  fi
}

cleanup() {
  local exit_code=$?
  if [[ -n "${FRONTEND_PID}" ]] && kill -0 "${FRONTEND_PID}" >/dev/null 2>&1; then
    info "Stopping frontend (pid=${FRONTEND_PID})"
    kill "${FRONTEND_PID}" >/dev/null 2>&1 || true
  fi
  if [[ -n "${BACKEND_PID}" ]] && kill -0 "${BACKEND_PID}" >/dev/null 2>&1; then
    info "Stopping backend (pid=${BACKEND_PID})"
    kill "${BACKEND_PID}" >/dev/null 2>&1 || true
  fi
  wait >/dev/null 2>&1 || true
  exit "${exit_code}"
}
trap cleanup EXIT INT TERM

wait_for_backend() {
  local health_url="http://localhost:${BACKEND_PORT}/actuator/health"
  local tries=75
  local i
  for ((i=1; i<=tries; i++)); do
    if curl -fsS "${health_url}" >/dev/null 2>&1; then
      info "Backend is healthy at ${health_url}"
      return 0
    fi
    if [[ -n "${BACKEND_PID}" ]] && ! kill -0 "${BACKEND_PID}" >/dev/null 2>&1; then
      error "Backend process exited before health check passed."
      if [[ -f "${BACKEND_LOG}" ]]; then
        error "Recent backend log output:"
        tail -n 120 "${BACKEND_LOG}" >&2 || true
        error "Detected root causes (if any):"
        grep -E "Caused by:|ERROR|Exception|Unsupported Database" "${BACKEND_LOG}" | tail -n 20 >&2 || true
        error "Full backend log: ${BACKEND_LOG}"
      fi
      return 1
    fi
    sleep 2
  done
  error "Backend did not become healthy in time: ${health_url}"
  if [[ -f "${BACKEND_LOG}" ]]; then
    error "Recent backend log output:"
    tail -n 120 "${BACKEND_LOG}" >&2 || true
    error "Full backend log: ${BACKEND_LOG}"
  fi
  return 1
}

bootstrap_database() {
  if [[ "${SKIP_DB_BOOTSTRAP}" == "true" ]]; then
    info "Skipping DB bootstrap because SKIP_DB_BOOTSTRAP=true"
    return 0
  fi

  info "Checking MySQL connectivity (${MYSQL_ADMIN_USER}@${DB_HOST}:${DB_PORT})"
  if ! resolve_mysql_mode; then
    info "Admin MySQL auth unavailable. Trying application DB credentials (${DB_USERNAME})..."
    if can_use_app_database; then
      info "Application DB credentials are valid; skipping bootstrap."
      return 0
    fi
    error "Unable to connect to MySQL as ${MYSQL_ADMIN_USER}."
    error "Try one of:"
    error "1) Set MYSQL_ADMIN_PASSWORD=<password>"
    error "2) Use socket auth explicitly: MYSQL_CONNECTION_MODE=socket"
    error "3) Use sudo-backed bootstrap: MYSQL_USE_SUDO=true"
    error "4) Ensure ${DB_NAME} + ${DB_USERNAME} exist, then run with SKIP_DB_BOOTSTRAP=true"
    exit 1
  fi
  info "MySQL connection mode: ${MYSQL_MODE_RESOLVED}"

  local db_name_escaped
  local db_user_escaped
  local db_pass_escaped
  local bootstrap_sql
  local bootstrap_output
  db_name_escaped="$(sql_escape "${DB_NAME}")"
  db_user_escaped="$(sql_escape "${DB_USERNAME}")"
  db_pass_escaped="$(sql_escape "${DB_PASSWORD}")"

  info "Bootstrapping database and application user"
  bootstrap_sql="
    CREATE DATABASE IF NOT EXISTS \`${db_name_escaped}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    CREATE USER IF NOT EXISTS '${db_user_escaped}'@'localhost' IDENTIFIED BY '${db_pass_escaped}';
    CREATE USER IF NOT EXISTS '${db_user_escaped}'@'%' IDENTIFIED BY '${db_pass_escaped}';
    ALTER USER '${db_user_escaped}'@'localhost' IDENTIFIED BY '${db_pass_escaped}';
    ALTER USER '${db_user_escaped}'@'%' IDENTIFIED BY '${db_pass_escaped}';
    GRANT ALL PRIVILEGES ON \`${db_name_escaped}\`.* TO '${db_user_escaped}'@'localhost';
    GRANT ALL PRIVILEGES ON \`${db_name_escaped}\`.* TO '${db_user_escaped}'@'%';
    FLUSH PRIVILEGES;
  "
  if bootstrap_output="$(mysql_exec "${bootstrap_sql}" 2>&1)"; then
    return 0
  fi

  if printf '%s' "${bootstrap_output}" | grep -q "ERROR 1819"; then
    error "MySQL password policy rejected the application DB password for ${DB_USERNAME}."
    if [[ -t 0 ]]; then
      local replacement_pass
      read -rs -p "Enter a stronger application DB password (leave empty to auto-generate): " replacement_pass
      printf '\n'
      if [[ -z "${replacement_pass}" ]]; then
        replacement_pass="$(generate_strong_password)"
        info "Generated strong application DB password."
      fi
      DB_PASSWORD="${replacement_pass}"
      db_pass_escaped="$(sql_escape "${DB_PASSWORD}")"
      bootstrap_sql="
        CREATE DATABASE IF NOT EXISTS \`${db_name_escaped}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
        CREATE USER IF NOT EXISTS '${db_user_escaped}'@'localhost' IDENTIFIED BY '${db_pass_escaped}';
        CREATE USER IF NOT EXISTS '${db_user_escaped}'@'%' IDENTIFIED BY '${db_pass_escaped}';
        ALTER USER '${db_user_escaped}'@'localhost' IDENTIFIED BY '${db_pass_escaped}';
        ALTER USER '${db_user_escaped}'@'%' IDENTIFIED BY '${db_pass_escaped}';
        GRANT ALL PRIVILEGES ON \`${db_name_escaped}\`.* TO '${db_user_escaped}'@'localhost';
        GRANT ALL PRIVILEGES ON \`${db_name_escaped}\`.* TO '${db_user_escaped}'@'%';
        FLUSH PRIVILEGES;
      "
      mysql_exec "${bootstrap_sql}"
      info "Database bootstrap completed with updated application DB password."
      return 0
    fi
    error "Non-interactive mode cannot prompt for stronger password."
  fi

  error "Database bootstrap failed."
  printf '%s\n' "${bootstrap_output}" >&2
  exit 1
}

optional_ml_training_smoke() {
  if [[ "${RUN_BOOTSTRAP_TRAINING}" != "true" ]]; then
    return 0
  fi

  info "Running optional ML integration smoke call (/api/v1/models/train)"

  local auth_response
  auth_response="$(curl -fsS -X POST "http://localhost:${BACKEND_PORT}/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"${APP_ADMIN_USERNAME}\",\"password\":\"${APP_ADMIN_PASSWORD}\"}")"

  local token
  token="$(printf '%s' "${auth_response}" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')"
  if [[ -z "${token}" ]]; then
    error "Could not extract JWT from login response"
    return 1
  fi

  curl -fsS -X POST "http://localhost:${BACKEND_PORT}/api/v1/models/train" \
    -H "Authorization: Bearer ${token}" \
    -H "Content-Type: application/json" \
    -d '{"datasetPolicy":"AUTO_LABEL_ONLY","epochs":3,"batchSize":128,"learningRate":0.001,"rebalance":true}' >/dev/null

  info "ML training smoke call submitted successfully"
}

main() {
  require_cmd java
  require_cmd javac
  require_cmd mvn
  require_cmd node
  require_cmd npm
  require_cmd mysql
  require_cmd curl
  require_cmd openssl
  if [[ "${MYSQL_USE_SUDO}" == "true" ]]; then
    require_cmd sudo
  fi

  info "Project root: ${ROOT_DIR}"
  info "Backend URL: http://localhost:${BACKEND_PORT}"
  info "Frontend URL: http://localhost:${FRONTEND_PORT}"
  info "Model artifacts dir: ${MODEL_ARTIFACTS_DIR}"
  info "DB bootstrap: ${SKIP_DB_BOOTSTRAP}"

  configure_java_for_maven
  MAVEN_COMMON_ARGS=(-Dmaven.repo.local="${MAVEN_REPO_LOCAL}")
  info "Maven local repo: ${MAVEN_REPO_LOCAL}"

  prompt_mysql_credentials

  mkdir -p "${MODEL_ARTIFACTS_DIR}"
  mkdir -p "${MAVEN_REPO_LOCAL}"
  mkdir -p "${RUN_DIR}"
  : > "${BACKEND_LOG}"
  : > "${FRONTEND_LOG}"
  bootstrap_database

  export DB_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
  export DB_USERNAME
  export DB_PASSWORD
  export JWT_SECRET
  export APP_ADMIN_USERNAME
  export APP_ADMIN_PASSWORD
  export MODEL_ARTIFACTS_DIR
  export CSV_MAX_BYTES
  export VITE_API_BASE
  export MAVEN_OPTS="${MAVEN_OPTS:-}"

  if [[ "${MAVEN_REPO_LOCAL}" == "${ROOT_DIR}/"* ]]; then
    info "Clearing cached project artifacts from local Maven repo"
    rm -rf "${MAVEN_REPO_LOCAL}/com/datavalley"
  else
    info "Skipping Maven cache purge for external repo path: ${MAVEN_REPO_LOCAL}"
  fi

  info "Building and installing backend + ml-core (skip tests)"
  mvn -q "${MAVEN_COMMON_ARGS[@]}" "${MAVEN_JAVA_ARGS[@]}" -Dmaven.test.skip=true clean install

  info "Installing frontend dependencies"
  (cd frontend && npm install)

  info "Verifying application DB connectivity over TCP (${DB_USERNAME}@${DB_HOST}:${DB_PORT}/${DB_NAME})"
  if ! can_use_app_database; then
    error "Application DB user cannot connect over TCP with current settings."
    error "Check DB_HOST/DB_PORT and ensure MySQL accepts TCP connections."
    exit 1
  fi

  info "Starting backend service"
  (
    mvn -q -e "${MAVEN_COMMON_ARGS[@]}" "${MAVEN_JAVA_ARGS[@]}" -f "${ROOT_DIR}/backend/pom.xml" \
      "org.springframework.boot:spring-boot-maven-plugin:${SPRING_BOOT_PLUGIN_VERSION}:run"
  ) > "${BACKEND_LOG}" 2>&1 &
  BACKEND_PID=$!
  info "Backend log: ${BACKEND_LOG}"

  wait_for_backend

  optional_ml_training_smoke

  info "Starting frontend service"
  (
    cd "${ROOT_DIR}/frontend"
    npm run dev -- --host 0.0.0.0 --port "${FRONTEND_PORT}"
  ) > "${FRONTEND_LOG}" 2>&1 &
  FRONTEND_PID=$!
  info "Frontend log: ${FRONTEND_LOG}"

  info "Application is running."
  info "Login with ${APP_ADMIN_USERNAME} / ${APP_ADMIN_PASSWORD}"
  info "Press Ctrl+C to stop backend and frontend."

  wait "${BACKEND_PID}" "${FRONTEND_PID}"
}

main "$@"
