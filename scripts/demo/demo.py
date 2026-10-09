#!/usr/bin/env python3
"""Everything a teammate needs to try PredictiveMaintain, in one command each.

    python3 demo.py setup     creates YOUR OWN demo company (your accounts, your data)
    python3 demo.py run       turns on the simulated plant (Ctrl+C to stop it)
    python3 demo.py spike     raises an alert on the next sensor, no arguments needed
    python3 demo.py calm      brings every sensor back to normal
    python3 demo.py status    shows the sensors as the manager would see them
    python3 demo.py accounts  shows your accounts and password

Nothing has to be typed or invented: the company name, the e-mail domain and the password
are generated for you and saved in your home folder, outside any repository.
"""
import argparse
import json
import os
import secrets
import subprocess
import sys
import time
from pathlib import Path

HERE = Path(__file__).resolve().parent
DEFAULT_URL = "https://predictivemaintain-backend-production.up.railway.app"
HOME_DIR = Path(os.environ.get("DEMO_HOME") or Path.home() / ".predictivemaintain-demo")
STATE = HOME_DIR / "state.json"
ACCOUNTS = HOME_DIR / "cuentas.txt"
ROTATION = HOME_DIR / "next-spike.txt"
PY = "py" if os.name == "nt" else "python3"


def say(message=""):
    print(message, flush=True)


def server_url():
    return (os.environ.get("API_URL") or DEFAULT_URL).rstrip("/")


def run_script(script, *args):
    """Runs one of the scripts of this folder with this teammate's own state file."""
    env = dict(os.environ, DEMO_STATE=str(STATE), PYTHONUTF8="1", PYTHONIOENCODING="utf-8")
    try:
        return subprocess.run([sys.executable, str(HERE / script), *args], cwd=str(HERE), env=env).returncode
    except KeyboardInterrupt:
        return 0


def load_state():
    if not STATE.exists():
        sys.exit(f"Todavía no tienes tu empresa de demostración. Primero ejecuta:  {PY} demo.py setup")
    with open(STATE, encoding="utf-8") as handle:
        return json.load(handle)


def save_accounts(state):
    lines = ["Tus cuentas de demostración (la contraseña es la misma para las cuatro):", ""]
    lines += [f"  {role:<20} {email}" for email, role in state["users"].items()]
    lines += ["", f"Contraseña: {state['password']}", "",
              "Este archivo está fuera de cualquier repositorio. No lo subas a Git ni lo pegues en un chat público."]
    HOME_DIR.mkdir(parents=True, exist_ok=True)
    ACCOUNTS.write_text("\n".join(lines) + "\n", encoding="utf-8")
    try:
        os.chmod(ACCOUNTS, 0o600)
    except OSError:
        pass
    return "\n".join(lines)


def cmd_setup(args):
    HOME_DIR.mkdir(parents=True, exist_ok=True)
    if args.new and STATE.exists():
        STATE.rename(HOME_DIR / f"state-{int(time.time())}.old.json")
        say("La empresa anterior queda guardada en un archivo .old.json; se crea una nueva.")
    extra = []
    if not STATE.exists():
        domain = f"eq{secrets.token_hex(2)}.example.com"
        password = "Demo" + "".join(str(secrets.randbelow(10)) for _ in range(8))
        extra = ["--domain", domain, "--password", password]
        say(f"Creando tu empresa de demostración (dominio {domain})...")
    else:
        say("Ya tienes una empresa de demostración: se comprueba y se completa lo que falte.")
    code = run_script("seed_demo.py", "--url", server_url(), "--history-hours", str(args.history_hours), *extra)
    if code != 0:
        sys.exit("La creación no terminó. Revisa el mensaje de arriba; puedes volver a ejecutar el mismo comando.")
    say()
    say(save_accounts(load_state()))
    say()
    say(f"Guardadas también en: {ACCOUNTS}")
    say()
    say("Siguiente:")
    say(f"  1. Abre la app y entra con una de las cuentas de arriba.")
    say(f"  2. En una terminal:  {PY} demo.py run      (la planta simulada)")
    say(f"  3. En otra:          {PY} demo.py spike    (provoca una alerta)")


def cmd_accounts(_args):
    say(save_accounts(load_state()))


def cmd_run(args):
    load_state()
    extra = ["--duration", str(args.duration)] if args.duration else []
    only = ["--only", *args.only] if args.only else []
    run_script("simulator.py", "run", "--interval", str(args.interval), *extra, *only)


def cmd_calm(_args):
    load_state()
    say("Enviando lecturas normales a todos los sensores (6 segundos)...")
    run_script("simulator.py", "run", "--interval", "2", "--duration", "6")


def cmd_status(_args):
    load_state()
    run_script("simulator.py", "status")


def rotation(state):
    """Every asset gets its first sensor, then every asset its second one... so alerts spread over the plant."""
    per_asset = [[(code, metric) for metric in asset["sensors"]] for code, asset in state["assets"].items()]
    order = []
    for level in range(max(len(sensors) for sensors in per_asset)):
        order += [sensors[level] for sensors in per_asset if level < len(sensors)]
    return order


def cmd_spike(args):
    state = load_state()
    if args.asset:
        target = [args.asset] + ([args.metric] if args.metric else [])
        automatic = False
    else:
        order = rotation(state)
        try:
            index = int(ROTATION.read_text().strip())
        except (OSError, ValueError):
            index = 0
        asset, metric = order[index % len(order)]
        target = [asset, metric]
        automatic = True
        say(f"Sensor elegido automáticamente: {asset} {metric} ({index % len(order) + 1} de {len(order)})")
    code = run_script("simulator.py", "spike", *target)
    if automatic and code == 0:
        ROTATION.write_text(str(index + 1))
    say()
    say("En la app: Alertas, y desliza hacia abajo para refrescar.")
    say("Cada sensor admite una alerta nueva cada 10 minutos; sin argumentos, este comando pasa al siguiente sensor.")


def main():
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    parser = argparse.ArgumentParser(description="Kit de pruebas de PredictiveMaintain.")
    commands = parser.add_subparsers(dest="command")
    setup = commands.add_parser("setup", help="crea tu propia empresa de demostración")
    setup.add_argument("--new", action="store_true", help="crea otra empresa nueva en lugar de reutilizar la tuya")
    setup.add_argument("--history-hours", type=int, default=24, help=argparse.SUPPRESS)
    setup.set_defaults(func=cmd_setup)
    run = commands.add_parser("run", help="enciende la planta simulada (Ctrl+C para detenerla)")
    run.add_argument("--interval", type=float, default=10.0, help="segundos entre rondas (mínimo 2)")
    run.add_argument("--only", nargs="+", help="solo estos activos, por ejemplo: FAJA-021 PUMP-001")
    run.add_argument("--duration", type=float, default=0, help=argparse.SUPPRESS)
    run.set_defaults(func=cmd_run)
    spike = commands.add_parser("spike", help="provoca una alerta")
    spike.add_argument("asset", nargs="?", help="opcional: código del activo, por ejemplo FAJA-021")
    spike.add_argument("metric", nargs="?", help="opcional: VIBRATION, TEMPERATURE, PRESSURE, CURRENT o NOISE")
    spike.set_defaults(func=cmd_spike)
    commands.add_parser("calm", help="devuelve todos los sensores a la normalidad").set_defaults(func=cmd_calm)
    commands.add_parser("status", help="muestra el estado de los sensores").set_defaults(func=cmd_status)
    commands.add_parser("accounts", help="muestra tus cuentas y tu contraseña").set_defaults(func=cmd_accounts)
    args = parser.parse_args()
    if not args.command:
        parser.print_help()
        return
    args.func(args)


if __name__ == "__main__":
    main()
