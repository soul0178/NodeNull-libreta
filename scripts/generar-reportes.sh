#!/usr/bin/env bash
# Genera los reportes del Lab 05 y los copia a docs/reportes/ para versionarlos:
#   - Reporte de ejecución de pruebas (Surefire): docs/reportes/ejecucion/
#   - Reporte de cobertura de código (JaCoCo):    docs/reportes/cobertura/
# Uso (desde la raíz del repositorio): ./scripts/generar-reportes.sh
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/backend"

mvn -B clean verify | tee "$ROOT/docs/reportes/salida-mvn-verify.txt"

rm -rf "$ROOT/docs/reportes/ejecucion" "$ROOT/docs/reportes/cobertura"
mkdir -p "$ROOT/docs/reportes/ejecucion" "$ROOT/docs/reportes/cobertura"
cp -r target/surefire-reports/. "$ROOT/docs/reportes/ejecucion/"
[ -f target/site/surefire-report.html ] && cp target/site/surefire-report.html "$ROOT/docs/reportes/ejecucion/"
cp -r target/site/jacoco/. "$ROOT/docs/reportes/cobertura/"

echo
echo "Reporte de ejecución : docs/reportes/ejecucion/ (surefire-report.html)"
echo "Reporte de cobertura : docs/reportes/cobertura/index.html"
