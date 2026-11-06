#!/usr/bin/env bash
# Uso: ./docs/gestion/crear_issues.sh  (requiere gh autenticado: gh auth login)
set -euo pipefail
CSV="$(dirname "$0")/issues.csv"
for l in "nuevo-requisito:1D76DB" "mejora:FBCA04" "correccion:D73A4A" "unit test:0E8A16"; do
  n="${l%%:*}"; c="${l##*:}"; gh label create "$n" --color "$c" --force
done
python3 - "$CSV" <<'PY'
import csv, subprocess, sys
for r in csv.DictReader(open(sys.argv[1], encoding="utf-8")):
    check = "\n".join("- " + x for x in r["checklist"].split("|"))
    body = f"{r['descripcion']}\n\n**Módulo:** {r['modulo']}  \n**Prioridad:** {r['prioridad']}  \n**Estimación:** {r['estimacion']}\n\n### Checklist\n{check}\n"
    subprocess.run(["gh","issue","create","--title",r["titulo"],"--body",body,"--label",r["etiqueta"],"--assignee","@me"],check=True)
PY
