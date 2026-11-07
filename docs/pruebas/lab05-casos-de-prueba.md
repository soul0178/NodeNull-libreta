# Lab 05 — Casos de prueba unitarios y de integración

**Módulos elegidos (uno por capa):** `Course` (dominio), `CourseRepositoryAdapter` (repositorio) y
`CourseApplicationService` (servicios de aplicación, con dobles del repositorio).
Todos los casos siguen el patrón **Arrange-Act-Assert** y se escribieron antes que el código (TDD).
Herramientas: JUnit 5, Mockito, JaCoCo, Maven (`mvn test`, `mvn verify`). Reportes en `docs/reportes/`.

## 1. Dominio — `Course` (`CourseTest`)

| Clase / Método | Caso de prueba | Valores | Resultado esperado |
|---|---|---|---|
| `Course.create` | Datos válidos | nombre "Cálculo III", 4 créditos, pesos 15/15/15/15/20/20 | Curso creado con meta 11, sin archivar, sin notas |
| `Course.create` | Nombre vacío | nombre "  " | `IllegalArgumentException` |
| `Course.create` | Créditos negativos | créditos -1 | `IllegalArgumentException` |
| `Course.create` | Sin carrera | carrera " " | `IllegalArgumentException` |
| `Course.create` | Pesos que no suman 100 % | seis pesos de 20 % (120 %) | `IllegalArgumentException` |
| `Course.create` | Cantidad de pesos incorrecta | 2 pesos | `IllegalArgumentException` |
| `Course.create` | Peso negativo | -10 %, 30 %, 20 %, 20 %, 20 %, 20 % | `IllegalArgumentException` |
| `Course.create` | Suma dentro de la tolerancia (0,5 %) | pesos que suman 99,6 % | Se acepta |
| `Course.create` | Pesos con ceros | 30/0/30/0/40/0 | Se acepta |
| `Course.changeWeights` | Pesos inválidos | 50/10/0/0/0/0 | `IllegalArgumentException` y se conservan los pesos anteriores |
| `Course.setGrade` | Nota válida | P1 = 15 | `gradeAt(P1) = 15` |
| `Course.setGrade` | Borrar nota | P1 = null | `gradeAt(P1)` es nulo |
| `Course.setGrade` | Fuera de rango | 21 y -1 | `IllegalArgumentException` |
| `Course.setGrade` | Valores límite | 0 y 20 | Se aceptan |
| `Course.changeGoal` | Meta fuera de rango | 25 | `IllegalArgumentException` |
| `Course.archive / unarchive` | Archivar y desarchivar | — | `isArchived()` verdadero y luego falso |
| `Course.weights` | Encapsulación | modificar la copia devuelta | El curso conserva sus pesos |
| `Substitute` | Nota inválida | sustitutorio = 25 | `IllegalArgumentException` |

Otros casos de dominio: `GradeCalculatorTest` (16 casos de acumulado, proyección y tendencia),
`PassStatusServiceTest` (7), `GoalStatusServiceTest` (7), `SubstituteServiceTest` (9), `SemesterServiceTest` (8).

## 2. Repositorio — `CourseRepositoryAdapter` (`CourseRepositoryAdapterTest`, H2 en memoria)

| Clase / Método | Caso de prueba | Valores | Resultado esperado |
|---|---|---|---|
| `save` + `findById` | Ida y vuelta de todos los datos | notas 15, vacío, 12, vacío, vacío, 8; meta 14; 5 créditos | Se recuperan los mismos valores, incluidas las notas vacías |
| `save` + `findById` | Curso con sustitutorio | sustitutorio 18 sobre P1 (original 10) | Se recupera el mismo sustitutorio |
| `save` + `findById` | Curso sin sustitutorio | — | `substitute()` vacío |
| `save` | Actualizar existente | nota P1 = 17 y archivar | Un solo curso, con los cambios |
| `findById` | Id inexistente | "no-existe" | `Optional.empty()` |
| `findAll` | Orden de creación | Primero, Segundo, Tercero | Se devuelven en ese orden |
| `findByCareerId` | Filtrar por carrera | cursos de `career-1` y `career-2` | Solo el de la carrera pedida |
| `deleteById` | Eliminar | id "c-1" | `existsById` falso |
| `existsById` | Existente e inexistente | "c-1" y "c-9" | Verdadero y falso |

`SemesterRepositoryAdapterTest` agrega 5 casos (ida y vuelta del snapshot, orden por fecha, conteo, eliminación en cascada
y un mismo curso en varios semestres).

## 3. Servicios de aplicación — `CourseApplicationService` (`CourseApplicationServiceTest`, con Mockito)

El repositorio (`CourseRepository`) y el generador de ids (`IdGenerator`) son **dobles (mocks)**; los servicios de dominio son reales.

| Clase / Método | Caso de prueba | Valores | Resultado esperado |
|---|---|---|---|
| `create` | Asigna id y guarda | id generado "c-1" | Curso con ese id; `repository.save` invocado una vez |
| `create` | Pesos inválidos | 50/50/50/0/0/0 | `IllegalArgumentException`; `save` nunca se invoca |
| `get` | Existente | `findById` devuelve el curso | Devuelve el mismo curso |
| `get` | Inexistente | `findById` vacío | `CourseNotFoundException` |
| `list` | Varios cursos | repositorio con 2 cursos | Devuelve 2 |
| `recordGrade` | Registra y guarda | P1 = 15 | Nota actualizada; `save` invocado |
| `recordGrade` | Nota inválida | 25 | `IllegalArgumentException`; `save` nunca |
| `recordGrade` | Curso inexistente | id "x" | `CourseNotFoundException`; `save` nunca |
| `summarize` | Cálculos | C1 = 15, P1 = 10 | acumulado 3,75; peso pendiente 0,70; proyección 12,5; estado EN_CURSO; meta VAS_BIEN |
| `summarize` | Sin notas | curso vacío | Proyección vacía |
| `applySubstitute` | Se aplica | P1 = 10, P2 = 15, sustitutorio 18 | `APPLIED`; `save` invocado |
| `applySubstitute` | No supera | sustitutorio 9 | `NOT_HIGHER_THAN_LOWEST`; `save` nunca |
| `archive` | Archiva y guarda | — | Curso archivado; `save` invocado |
| `changeWeights` / `changeGoal` / `unarchive` / `removeSubstitute` | Operaciones complementarias | valores válidos | Cambio aplicado y guardado |
| `delete` | Existente | `existsById` verdadero | `deleteById` invocado |
| `delete` | Inexistente | `existsById` falso | `CourseNotFoundException`; `deleteById` nunca |

`SemesterApplicationServiceTest` (7 casos con dobles) verifica, entre otros, el **orden de las operaciones al cerrar un semestre**
(primero guardar el semestre y luego eliminar los cursos activos).

### Pruebas de integración (`ApplicationServicesIntegrationTest`)

Servicios de aplicación + dominio + repositorios JPA reales sobre H2, sin dobles:
crear, registrar notas y resumir; cerrar semestre; reabrir semestre; y cierre rechazado por un curso incompleto.
