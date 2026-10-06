#!/usr/bin/env python3
"""Plays the part of the plant's sensors during a demonstration.

    python3 simulator.py run                      # normal readings every few seconds
    python3 simulator.py spike PUMP-001           # one reading above the limit: raises an alert
    python3 simulator.py spike COMP-003 PRESSURE  # the same, for a specific sensor
    python3 simulator.py status                   # what the manager would see right now

It reads demo-state.json, created by seed_demo.py (it holds each sensor's key).
"""
import argparse
import math
import random
import sys
import time
from datetime import datetime, timezone

from common import ApiError, call, login, post_reading, require_state


def sensors_of(state, only=None):
    rows = []
    for code, asset in state["assets"].items():
        if only and code not in only:
            continue
        for metric, sensor in asset["sensors"].items():
            rows.append((code, metric, sensor))
    return rows


def normal_value(sensor, tick, phase, rng):
    """Stays well inside the range, so a normal run never raises an alert by accident."""
    span = sensor["upper"] - sensor["lower"]
    fraction = sensor["base"] + 0.03 * math.sin(tick / 6.0 + phase) + rng.gauss(0, 0.012)
    fraction = min(max(fraction, 0.05), 0.93)
    return round(sensor["lower"] + fraction * span, 3)


def run(state, interval, duration, only):
    rows = sensors_of(state, only)
    if not rows:
        sys.exit("No hay sensores para simular (revisa los códigos de --only).")
    rng = random.Random()
    phases = {(code, metric): rng.uniform(0, 6.28) for code, metric, _ in rows}
    base = state["base_url"]
    print(f"Simulando {len(rows)} sensores cada {interval} s en {base}. Ctrl+C para detener.", flush=True)
    started, tick = time.monotonic(), 0
    try:
        while duration <= 0 or time.monotonic() - started < duration:
            cycle_started = time.monotonic()
            accepted = alerts = 0
            for code, metric, sensor in rows:
                now = datetime.now(timezone.utc)
                value = normal_value(sensor, tick, phases[(code, metric)], rng)
                try:
                    answer = post_reading(base, sensor, value, now, f"sim-{int(now.timestamp() * 1000)}-{sensor['id'][:8]}")
                except ApiError as error:
                    if error.code == "SUBSCRIPTION_NOT_ACTIVE":
                        sys.exit("La suscripción de la empresa demo venció: renuévala desde la app o la API.")
                    print(f"  {code} {metric}: {error}", flush=True)
                    continue
                accepted += 1
                alerts += 1 if answer.get("alertRaised") else 0
            print(f"[{datetime.now():%H:%M:%S}] ronda {tick + 1}: {accepted}/{len(rows)} lecturas aceptadas"
                  + (f", {alerts} alerta(s)" if alerts else ""), flush=True)
            tick += 1
            time.sleep(max(0.0, interval - (time.monotonic() - cycle_started)))
    except KeyboardInterrupt:
        print("\nSimulación detenida.")


def spike(state, code, metric, over):
    asset = state["assets"].get(code.upper())
    if not asset:
        sys.exit(f"No existe el activo {code}. Activos: {', '.join(state['assets'])}")
    metric = (metric or next(iter(asset["sensors"]))).upper()
    sensor = asset["sensors"].get(metric)
    if not sensor:
        sys.exit(f"{code} no tiene un sensor {metric}. Sensores: {', '.join(asset['sensors'])}")
    value = round(sensor["upper"] + over * (sensor["upper"] - sensor["lower"]), 3)
    now = datetime.now(timezone.utc)
    answer = post_reading(state["base_url"], sensor, value, now, f"spike-{int(now.timestamp() * 1000)}")
    print(f"Lectura enviada: {code} {metric} = {value} {sensor['unit']} (el límite superior es {sensor['upper']}).")
    if answer.get("alertRaised"):
        print(f"-> ALERTA CREADA con severidad {answer.get('severity')}. Debe aparecer en la app en unos segundos.")
    elif answer.get("outOfRange"):
        print("-> Fuera de rango, pero NO se creó otra alerta: este sensor ya generó una hace menos de 10 minutos "
              "(enfriamiento). Prueba con otro sensor u otro activo.")
    else:
        print("-> El servidor la tomó como una lectura normal. ¿Cambiaron los umbrales del sensor?")


def status(state):
    base = state["base_url"]
    token = login(base, state["manager_email"], state["password"])
    for code, asset in state["assets"].items():
        info = next((a for a in call(base, "GET", "/assets?size=100", token=token)[1]["items"] if a["code"] == code), None)
        print(f"\n{code}  [{info['status'] if info else '?'}]")
        for sensor in call(base, "GET", f"/assets/{asset['id']}/sensors", token=token)[1]:
            latest = sensor.get("latestReading") or {}
            print(f"  {sensor['metric']:<12} {str(latest.get('value', '-')):>9} {sensor['unit']:<5} "
                  f"rango: {sensor.get('rangeStatus')}  comunicación: {sensor.get('communication')}")


def main():
    parser = argparse.ArgumentParser(description="Simulador de sensores de PredictiveMaintain.")
    commands = parser.add_subparsers(dest="command", required=True)
    run_parser = commands.add_parser("run", help="envía lecturas normales de forma continua")
    run_parser.add_argument("--interval", type=float, default=5.0, help="segundos entre rondas (mínimo 2)")
    run_parser.add_argument("--duration", type=float, default=0, help="segundos totales (0 = hasta Ctrl+C)")
    run_parser.add_argument("--only", nargs="+", help="solo estos activos, por código")
    spike_parser = commands.add_parser("spike", help="envía una lectura fuera de rango")
    spike_parser.add_argument("asset", help="código del activo, por ejemplo PUMP-001")
    spike_parser.add_argument("metric", nargs="?", help="VIBRATION, TEMPERATURE, PRESSURE, CURRENT o NOISE")
    spike_parser.add_argument("--over", type=float, default=0.25, help="cuánto se pasa del límite, como fracción del rango")
    commands.add_parser("status", help="muestra el estado actual de los sensores")
    args = parser.parse_args()

    state = require_state()
    if args.command == "run":
        if args.interval < 2:
            sys.exit("--interval debe ser de al menos 2 segundos.")
        run(state, args.interval, args.duration, {c.upper() for c in args.only or []})
    elif args.command == "spike":
        spike(state, args.asset, args.metric, args.over)
    else:
        status(state)


if __name__ == "__main__":
    main()