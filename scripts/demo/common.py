"""Shared helpers of the demo scripts: a small HTTP client and the state file.

Only the Python standard library is used, so there is nothing to install.
"""
import json
import os
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime, timezone

STATE_PATH = os.environ.get(
    "DEMO_STATE", os.path.join(os.path.dirname(os.path.abspath(__file__)), "demo-state.json"))


class ApiError(Exception):
    """The server answered with an error status. 'code' is the machine-readable code of the answer."""

    def __init__(self, status, body, method, path):
        self.status = status
        self.body = body if isinstance(body, dict) else {}
        self.code = self.body.get("code")
        detail = self.body.get("detail") or ""
        super().__init__(f"{method} {path} -> {status} {self.code or ''} {detail}".strip())


def call(base_url, method, path, body=None, token=None, device_key=None, timeout=30):
    """Calls /api/v1<path> and returns (status, parsed JSON or None). Raises ApiError on 4xx/5xx."""
    url = base_url.rstrip("/") + "/api/v1" + path
    data = None if body is None else json.dumps(body).encode("utf-8")
    request = urllib.request.Request(url, data=data, method=method)
    request.add_header("Accept", "application/json")
    request.add_header("Accept-Language", "es")
    if data is not None:
        request.add_header("Content-Type", "application/json")
    if token:
        request.add_header("Authorization", "Bearer " + token)
    if device_key:
        request.add_header("X-Device-Key", device_key)
    try:
        with urllib.request.urlopen(request, timeout=timeout) as response:
            raw = response.read()
            return response.status, (json.loads(raw) if raw else None)
    except urllib.error.HTTPError as error:
        raw = error.read()
        try:
            parsed = json.loads(raw) if raw else None
        except ValueError:
            parsed = {"detail": raw.decode("utf-8", "replace")[:200]}
        raise ApiError(error.code, parsed, method, path) from None
    except urllib.error.URLError as error:
        sys.exit(f"No se pudo conectar con {base_url}: {error.reason}")


def login(base_url, email, password):
    return call(base_url, "POST", "/auth/login", {"email": email, "password": password})[1]["accessToken"]


def post_reading(base_url, sensor, value, measured_at, source_key):
    """Sends one reading with the sensor's own key. Waits and retries if the server says 'too fast'."""
    body = {"sensorId": sensor["id"], "sourceKey": source_key, "value": value,
            "unit": sensor["unit"], "measuredAt": iso(measured_at)}
    for attempt in range(3):
        try:
            return call(base_url, "POST", "/sensors/readings", body, device_key=sensor["key"])[1]
        except ApiError as error:
            if error.status == 429 and attempt < 2:
                time.sleep(1.5)
                continue
            raise


def iso(moment):
    """ISO-8601 in UTC with a trailing Z, the format the API expects."""
    return moment.astimezone(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


def load_state():
    if not os.path.exists(STATE_PATH):
        return None
    with open(STATE_PATH, encoding="utf-8") as handle:
        return json.load(handle)


def save_state(state):
    """The file holds passwords and device keys, so it is readable only by its owner."""
    temporary = STATE_PATH + ".tmp"
    with open(temporary, "w", encoding="utf-8") as handle:
        json.dump(state, handle, indent=2, ensure_ascii=False)
    os.chmod(temporary, 0o600)
    os.replace(temporary, STATE_PATH)


def require_state():
    state = load_state()
    if not state:
        sys.exit("No hay datos de demostración. Primero ejecuta:  python3 seed_demo.py")
    return state