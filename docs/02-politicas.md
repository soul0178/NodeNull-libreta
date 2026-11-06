# Políticas de trabajo — Equipo NodeNull

Estas políticas aplican a todo cambio del repositorio. Están alineadas con los lineamientos del curso
(Gitflow, GitHub Project, TDD, pruebas automatizadas y CI/CD).

## 1. Política de ramas (Gitflow)

| Rama | Propósito | Regla |
|---|---|---|
| `master` | Versiones estables y releases | Solo recibe merges desde `desarrollo`; cada release lleva tag `vX.Y.Z` |
| `desarrollo` | Integración continua del trabajo | Solo recibe merges de `feature/*`; debe compilar y pasar las pruebas |
| `feature/<n>-<nombre>` | Un issue por rama | Se crea desde `desarrollo`; se borra tras el merge |

- Flujo: `master → desarrollo → feature` para crear, y `feature → desarrollo → master` para integrar.
- Los merges a `desarrollo` y `master` se hacen con `--no-ff` para dejar visible la integración.
- Sincronización: `master → desarrollo` (merge o rebase) tras cada release; `desarrollo → feature` si la rama vive más de un día.
- No se hacen commits directos a `master` ni a `desarrollo`, salvo documentación menor.

## 2. Política de commits

- Formato: `tipo(alcance): descripción en imperativo`. Tipos: `feat`, `fix`, `test`, `refactor`, `docs`, `chore`, `ci`.
- Cada commit referencia su issue: `Refs #n` mientras avanza y `Fix #n` en el commit que lo completa.
- Un commit = un cambio coherente y revisable. No se mezclan refactor y funcionalidad.
- El historial es real: no se reescribe ni se fechan commits de forma artificial.
- Cuando se trabaja con asistencia de IA, el commit incluye la línea `Co-Authored-By`.

## 3. Política de issues y tablero

- Todo trabajo nace como issue con: título, descripción, etiqueta, módulo, prioridad, estimación y checklist.
- Etiquetas: `nuevo-requisito` (historias de usuario o tareas técnicas), `mejora` (refactorizaciones y smells),
  `correccion` (bugs, vulnerabilidades y defectos), `unit test`.
- Columnas: **TO-DO** (pendiente) → **CURRENT ITERATION** (comprometido para la iteración) → **IN PROGRESS**
  (hay rama abierta) → **FIX VALIDATION** (código listo, falta verificar pruebas y pipeline) → **DONE** (mergeado y cerrado).
- Solo se mueve a DONE cuando el merge a `desarrollo` está hecho y el issue cerrado.

## 3.1 Definición de terminado (DoD)

Un issue está terminado cuando:
1. Cumple su checklist.
2. Tiene pruebas automatizadas y `mvn test` termina en `BUILD SUCCESS`.
3. El dominio no importa Spring ni JPA (`LayerDependencyTest` en verde).
4. Se integró a `desarrollo` con `--no-ff` y el issue se cerró con `Fix #n`.
5. El README refleja el cambio (estado, endpoints o diagramas, según corresponda).

## 4. Política de pruebas

- **TDD:** prueba que falla (rojo) → código mínimo (verde) → refactor. La prueba se commitea antes que la implementación.
- **Patrón Arrange-Act-Assert** y nombres descriptivos (`precioNegativo_lanzaExcepcion`).
- **Dominio:** pruebas unitarias sin frameworks. **Servicios de aplicación:** Mockito sobre las *interfaces* de repositorio.
  **Repositorios:** pruebas con H2. **API:** pruebas de integración.
- **Cobertura (JaCoCo):** objetivo mínimo de 80 % en `domain` y `application`.
- El frontend conserva sus pruebas Vitest; los cálculos no se duplican sin una prueba que los compare.

## 5. Política de arquitectura y código

- Regla de dependencia: `infrastructure → application → domain`. El dominio no conoce frameworks.
- Principios SOLID, nombres claros, métodos cortos, sin números mágicos (constantes con nombre).
- Estilo Java: Google Java Style (4 espacios, llaves siempre). Estilo JS: el existente del proyecto.
- Los repositorios se declaran como interfaces en `domain` y se implementan en `infrastructure.persistence`.

## 6. Política de seguridad

- No se suben secretos ni contraseñas. La configuración sensible va en variables de entorno.
- Las reglas de Firestore deben limitar cada documento a su propietario.
- Toda salida al DOM con datos del usuario se escapa (`esc()`) o se asigna como texto.
- El pipeline ejecuta OWASP ZAP y los hallazgos se registran como issues `correccion`.

## 7. Política de versionado y releases

- Versionado semántico `MAJOR.MINOR.PATCH`. `v0.1` es la línea base; la evolución publica `v0.2.0`, `v0.3.0`, etc.
- Cada release: merge `desarrollo → master`, tag y **GitHub Release** con notas generadas de los issues cerrados.
- Trazabilidad exigida: GitHub Project → Issue → Commits → GitHub Release.

## 8. Política de revisión

El equipo tiene un integrante, así que no hay revisión por pares. En su lugar, antes de cada merge se revisa el *diff*
con una lista de verificación (pruebas, nombres, dependencias entre capas, README) y se deja el resultado del
pipeline como evidencia.
