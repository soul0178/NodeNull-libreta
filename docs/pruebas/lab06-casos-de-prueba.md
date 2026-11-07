# Lab 06 — Pruebas funcionales (Selenium) · Libreta

Técnica: **caja negra** (clases de equivalencia y valores límite). Herramienta: Selenium WebDriver 4.25 + JUnit 5, patrón **Page Object** (`LibretaPage`).
Proyecto: `pruebas-funcionales/` (Maven independiente). Ejecución: `cd pruebas-funcionales && mvn test` (`-Dheadless=false` para ver el navegador, `-Dbrowser=firefox`).

## Objeto 1: Crear curso (`CrearCursoTest`)
Regla: nombre obligatorio; pesos suman 100 % con tolerancia ±0,5; créditos entero ≥ 0 o vacío; pesos ≥ 0.

| ID | Escenario | Valores de entrada | Resultado esperado |
|----|-----------|--------------------|--------------------|
| CP-01 | Datos válidos | "Cálculo III", 4, 15/15/15/15/20/20 | Curso creado; detalle con título y "4 créditos" |
| CP-02 | Nombre vacío | "" | Error "Ponle un nombre al curso." |
| CP-03 | Nombre de espacios | "   " | Error "Ponle un nombre al curso." |
| CP-04 | Pesos con ceros | 30/0/30/0/40/0 | Curso creado |
| CP-05 | Suma < 100 | suma 90 | Error "Los pesos deben sumar 100%." |
| CP-06 | Suma > 100 | suma 110 | Error de suma |
| CP-07 | Límite inferior | suma 99,5 | Curso creado |
| CP-08 | Bajo el límite | suma 99,4 | Error de suma |
| CP-09 | Límite superior | suma 100,5 | Curso creado |
| CP-10 | Sobre el límite | suma 100,6 | Error de suma |
| CP-11 | Sin pesos | (vacío) | Error de suma |
| CP-12 | Suma en vivo | 50 → 50+50 | "Suma actual: 50.0%" (mal) → "100.0%" (ok) |
| CP-13 | Cancelar | nombre + Cancelar | Modal cerrado, dashboard sigue vacío |
| CP-14 | Créditos vacíos | "" | Curso creado "sin créditos asignados" |
| CP-15 | Créditos negativos (DEF-01) | -3 | Error que menciona "créditos"; no se crea |
| CP-16 | Peso negativo con suma 100 (DEF-02) | -10/35/15/15/20/25 | Error que menciona "negativo"; no se crea |
| CP-17 | Créditos decimales (DEF-03) | 3.5 | Error que menciona "créditos"; no se crea |

## Objeto 2: Registrar nota (`RegistrarNotaTest`) — rango 0–20, redondeo al entero
| ID | Escenario | Entrada | Esperado |
|----|-----------|---------|----------|
| RN-01 | Válida | 15 | Celda 15; acumulado 2,25 |
| RN-02 | Redondeo arriba | 14.6 | 15 |
| RN-03 | Redondeo abajo | 14.4 | 14 |
| RN-04 | Límite inferior | 0 | 0; acumulado 0,00 |
| RN-05 | Límite superior | 20 | 20; acumulado 3,00 |
| RN-06 | Sobre el máximo | 21 | Se acota a 20 |
| RN-07 | Negativa | -1 | Se acota a 0 |
| RN-08 | Vacío | "" | "—"; proyectado "—" |
| RN-09 | Escape | 18, luego 7 + Esc | Conserva 18 |
| RN-10 | Todo 20 | 6×20 | "Aprobado" |
| RN-11 | Todo 5 | 6×5 | "Desaprobado" |
| RN-12 | Primera unidad 20/20 | C1=20, P1=20 | "En curso… al menos 7"; acumulado 6,00 |

## Objeto 3: Editar curso (`EditarCursoTest`)
| ID | Escenario | Entrada | Esperado |
|----|-----------|---------|----------|
| ED-01 | Créditos válidos | 6 | "6 créditos" |
| ED-02 | Créditos negativos (DEF-01) | -2 | Error "créditos"; modal abierto |
| ED-03 | Pesos inválidos | primer peso 50 | Error de suma |

## Defectos encontrados (v0.1)
| ID | Descripción | Pruebas que lo detectan | Corrección |
|----|-------------|-------------------------|------------|
| DEF-01 | Acepta créditos negativos | CP-15, ED-02 | Issue #26 |
| DEF-02 | Acepta pesos negativos si suman 100 | CP-16 | Issue #26 |
| DEF-03 | Acepta créditos decimales | CP-17 | Issue #26 |

Flujo: las pruebas se escribieron primero y **fallan** sobre v0.1 (4 fallos: CP-15, CP-16, CP-17, ED-02); tras #26 deben quedar en verde (32/32).
