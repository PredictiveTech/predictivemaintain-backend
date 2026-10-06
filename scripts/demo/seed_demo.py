#!/usr/bin/env python3
"""Creates a demo company with users, assets, sensors, 24 hours of readings and production data.

It is safe to run again: what already exists is skipped, and what was interrupted is resumed.
Everything that cannot be recovered later (passwords, sensor keys) is saved in demo-state.json,
which is NOT committed to Git.

    python3 seed_demo.py --url https://TU-URL.up.railway.app
"""
import argparse
import math
import os
import random
import secrets
import sys
import time
import uuid
from datetime import datetime, timedelta, timezone

from common import ApiError, call, iso, load_state, login, post_reading, save_state

# metric -> (unit, lower bound, upper bound, severity of the alert when it is crossed)
METRICS = {
    "VIBRATION": ("mm/s", 0.0, 7.0, "CRITICAL"),
    "TEMPERATURE": ("°C", 20.0, 85.0, "WARNING"),
    "PRESSURE": ("bar", 2.0, 8.0, "WARNING"),
    "CURRENT": ("A", 5.0, 40.0, "WARNING"),
    "NOISE": ("dB", 55.0, 90.0, "WARNING"),
}

# 'degrading' is a sensor whose readings rise during the last 24 hours: it gives the remaining
# useful life (RUL) analysis something to say.
ASSETS = [
    dict(code="PUMP-001", name="Bomba centrífuga principal", assetType="PUMP", criticality="CRITICAL",
         line="Línea A", location="Planta Ate - Nave 1", latitude=-12.0264, longitude=-76.9190,
         metrics=["VIBRATION", "TEMPERATURE", "PRESSURE"], degrading="VIBRATION"),
    dict(code="MOTOR-014", name="Motor de faja transportadora", assetType="MOTOR", criticality="MEDIUM",
         line="Línea A", location="Planta Ate - Nave 1", latitude=-12.0266, longitude=-76.9188,
         metrics=["VIBRATION", "CURRENT"], degrading=None),
    dict(code="COMP-003", name="Compresor de aire 3", assetType="COMPRESSOR", criticality="HIGH",
         line="Línea B", location="Planta Ate - Nave 2", latitude=-12.0270, longitude=-76.9181,
         metrics=["TEMPERATURE", "PRESSURE", "NOISE"], degrading="TEMPERATURE"),
    dict(code="EXTR-002", name="Extrusora 2", assetType="EXTRUDER", criticality="HIGH",
         line="Línea B", location="Planta Ate - Nave 2", latitude=-12.0272, longitude=-76.9179,
         metrics=["TEMPERATURE", "CURRENT"], degrading=None),
    dict(code="FAJA-021", name="Faja empacadora", assetType="CONVEYOR", criticality="LOW",
         line="Línea C", location="Planta Ate - Nave 3", latitude=-12.0275, longitude=-76.9175,
         metrics=["VIBRATION", "NOISE"], degrading=None),
]

USERS = [("tecnico1", "Técnico Uno", "TECHNICIAN"),
         ("tecnico2", "Técnico Dos", "TECHNICIAN"),
         ("operador", "Operador Demo", "OPERATOR")]

# assets that also get production and stop data, with the ideal seconds needed per unit
PRODUCTION = {"PUMP-001": 10, "COMP-003": 8, "EXTR-002": 12}

HEALTHY, DEGRADED = 0.45, 0.88   # where in the allowed range the readings sit (0 = lower bound, 1 = upper bound)


def say(message):
    print(message, flush=True)


def parse_args():
    parser = argparse.ArgumentParser(description="Crea los datos de demostración de PredictiveMaintain.")
    parser.add_argument("--url", help="URL del backend (por defecto API_URL o http://localhost:8080)")
    parser.add_argument("--domain", default="example.com", help="dominio de los correos de demostración")
    parser.add_argument("--company", default="PredictiveTech Demo", help="nombre de la empresa")
    parser.add_argument("--password", default=os.environ.get("DEMO_PASSWORD"),
                        help="contraseña de las cuentas (por defecto se genera una y se guarda)")
    parser.add_argument("--skip-history", action="store_true", help="no crear lecturas ni datos de producción")
    parser.add_argument("--history-hours", type=int, default=24, help="horas de lecturas históricas")
    parser.add_argument("--pause", type=float, default=1.2,
                        help="segundos entre rondas de lecturas (el servidor exige al menos 1 por sensor)")
    parser.add_argument("--force", action="store_true", help="usar los datos guardados aunque sean de otro servidor")
    return parser.parse_args()


def resolve_state(args):
    base = (args.url or os.environ.get("API_URL") or "").strip()
    state = load_state()
    if state:
        if base and state["base_url"].rstrip("/") != base.rstrip("/") and not args.force:
            sys.exit(f"demo-state.json pertenece a {state['base_url']}, no a {base}.\n"
                     "Sus claves de sensores no sirven en otro servidor. Bórralo para empezar de nuevo "
                     "o usa --force si sabes lo que haces.")
        state["base_url"] = base or state["base_url"]
        return state
    password = args.password or secrets.token_urlsafe(9)
    if len(password) < 8:
        sys.exit("La contraseña debe tener al menos 8 caracteres.")
    return {"base_url": base or "http://localhost:8080", "domain": args.domain, "password": password,
            "company": args.company, "registration_id": str(uuid.uuid4()),
            "manager_email": f"jefe.demo@{args.domain}", "users": {}, "assets": {},
            "operation_data_done": False}


def ensure_company(state):
    base = state["base_url"]
    try:
        call(base, "POST", "/auth/register", {"companyName": state["company"], "email": state["manager_email"],
                                              "password": state["password"],
                                              "registrationId": state["registration_id"]})
        say(f"  empresa creada: {state['company']}")
    except ApiError as error:
        if error.status != 409:
            raise
        say("  la empresa ya existía")
    try:
        return login(base, state["manager_email"], state["password"])
    except ApiError as error:
        if error.status == 401:
            sys.exit(f"{state['manager_email']} ya existe, pero la contraseña guardada no coincide.\n"
                     "Usa otro dominio (--domain) para crear una empresa nueva, o recupera la contraseña.")
        raise


def ensure_users(state, token):
    base = state["base_url"]
    state["users"][state["manager_email"]] = "MAINTENANCE_MANAGER"
    for name, display, role in USERS:
        email = f"{name}.demo@{state['domain']}"
        try:
            call(base, "POST", "/users", {"email": email, "displayName": display,
                                          "initialPassword": state["password"], "roles": [role]}, token=token)
            say(f"  usuario creado: {email} ({role})")
        except ApiError as error:
            if error.status != 409:
                raise
        state["users"][email] = role
    save_state(state)


def ensure_assets_and_sensors(state, token):
    base = state["base_url"]
    existing = {item["code"]: item["id"]
                for item in call(base, "GET", "/assets?size=100", token=token)[1]["items"]}
    for plan in ASSETS:
        entry = state["assets"].setdefault(plan["code"], {"id": existing.get(plan["code"]), "sensors": {}})
        if not entry["id"]:
            created = call(base, "POST", "/assets", {
                "code": plan["code"], "name": plan["name"], "location": plan["location"],
                "assetType": plan["assetType"], "criticality": plan["criticality"],
                "productionLine": plan["line"], "latitude": plan["latitude"], "longitude": plan["longitude"],
            }, token=token)[1]
            entry["id"] = created["id"]
            say(f"  activo creado: {plan['code']}")
            save_state(state)
        present = {sensor["metric"] for sensor in call(base, "GET", f"/assets/{entry['id']}/sensors", token=token)[1]}
        for metric in plan["metrics"]:
            if metric in entry["sensors"]:
                continue
            if metric in present:
                say(f"  AVISO: {plan['code']} ya tiene un sensor {metric} cuya clave no está en demo-state.json "
                    "(la clave solo se muestra al crearlo). Ese sensor no enviará datos.")
                continue
            unit, lower, upper, severity = METRICS[metric]
            sensor = call(base, "POST", f"/assets/{entry['id']}/sensors", {"metric": metric, "unit": unit}, token=token)[1]
            # The key is shown only now: it is saved before anything else can fail
            entry["sensors"][metric] = {"id": sensor["id"], "key": sensor["deviceKey"], "unit": unit,
                                        "lower": lower, "upper": upper,
                                        "base": DEGRADED if plan["degrading"] == metric else HEALTHY,
                                        "history": False}
            save_state(state)
            call(base, "PUT", f"/sensors/{sensor['id']}/threshold",
                 {"lowerBound": lower, "upperBound": upper, "severity": severity}, token=token)
            say(f"  sensor creado: {plan['code']} {metric} ({lower} a {upper} {unit})")


def history_value(sensor, step, steps, rng):
    """A believable reading: stable with noise, or slowly rising for the degrading sensors."""
    span = sensor["upper"] - sensor["lower"]
    if sensor["base"] == DEGRADED:
        fraction = HEALTHY + (DEGRADED - HEALTHY) * step / max(steps - 1, 1)
    else:
        fraction = HEALTHY + 0.04 * math.sin(step / 3.0)
    fraction += rng.gauss(0, 0.015)
    fraction = min(max(fraction, 0.05), 0.92)      # never outside the range: the history must not raise alerts
    return round(sensor["lower"] + fraction * span, 3)


def backfill_history(state, hours, pause):
    base = state["base_url"]
    pending = [(code, metric, sensor) for code, asset in state["assets"].items()
               for metric, sensor in asset["sensors"].items() if not sensor["history"]]
    if not pending:
        return
    say(f"  enviando {hours} horas de lecturas para {len(pending)} sensores (unos {int(hours * pause)} segundos)...")
    now = datetime.now(timezone.utc).replace(minute=0, second=0, microsecond=0)
    rngs = {(code, metric): random.Random(f"{code}-{metric}") for code, metric, _ in pending}
    alerts = 0
    for step in range(hours):
        started = time.monotonic()
        measured_at = now - timedelta(hours=hours - 1 - step)
        for code, metric, sensor in pending:
            value = history_value(sensor, step, hours, rngs[(code, metric)])
            # The key is the hour, so sending the same history twice never stores it twice
            answer = post_reading(base, sensor, value, measured_at, f"seed-{measured_at:%Y%m%d%H}")
            alerts += 1 if answer.get("alertRaised") else 0
        elapsed = time.monotonic() - started
        if elapsed < pause:
            time.sleep(pause - elapsed)      # the server accepts one reading per second per sensor
    for _, _, sensor in pending:
        sensor["history"] = True
    save_state(state)
    say(f"  lecturas enviadas. Alertas creadas por el historial: {alerts} (debe ser 0)")


def production_data(state, token):
    """Seven working days (08:00-16:00 in Lima) with a few stops, so the reports have something to show."""
    if state["operation_data_done"]:
        return
    base = state["base_url"]
    say("  registrando 7 días de producción y paradas...")
    today = datetime.now(timezone.utc).replace(hour=0, minute=0, second=0, microsecond=0)
    for code, cycle in PRODUCTION.items():
        asset_id = state["assets"][code]["id"]
        rng = random.Random(f"production-{code}")
        for days_ago in range(7, 0, -1):
            starts = today - timedelta(days=days_ago) + timedelta(hours=13)       # 13:00 UTC = 08:00 Lima
            ends = starts + timedelta(hours=8)
            stopped = 0
            for first_minute in (rng.randint(60, 120), rng.randint(260, 320)):     # two stops that never overlap
                if rng.random() < 0.65:
                    length = rng.randint(15, 60)
                    begin = starts + timedelta(minutes=first_minute)
                    call(base, "POST", f"/assets/{asset_id}/downtime",
                         {"startedAt": iso(begin), "endedAt": iso(begin + timedelta(minutes=length))}, token=token)
                    stopped += length * 60
            planned = 8 * 3600
            operating = planned - stopped
            total_units = int(operating / (cycle * 1.12))      # about 89 % of the ideal speed
            call(base, "POST", f"/assets/{asset_id}/production-windows", {
                "startsAt": iso(starts), "endsAt": iso(ends), "plannedSeconds": planned,
                "operatingSeconds": operating, "totalUnits": total_units,
                "goodUnits": int(total_units * 0.95), "idealCycleSeconds": cycle}, token=token)
    state["operation_data_done"] = True
    save_state(state)


def main():
    args = parse_args()
    state = resolve_state(args)
    say(f"Servidor: {state['base_url']}")
    save_state(state)
    token = ensure_company(state)
    ensure_users(state, token)
    ensure_assets_and_sensors(state, token)
    if not args.skip_history:
        backfill_history(state, args.history_hours, args.pause)
        production_data(state, token)

    say("\nListo. Cuentas de demostración (todas con la misma contraseña):")
    for email, role in state["users"].items():
        say(f"  {role:<20} {email}")
    say(f"  contraseña: {state['password']}")
    say("\nAhora puedes iniciar la simulación:  python3 simulator.py run")
    say("Para provocar una alerta en vivo:     python3 simulator.py spike PUMP-001")


if __name__ == "__main__":
    main()