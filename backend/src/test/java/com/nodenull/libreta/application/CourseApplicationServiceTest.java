package com.nodenull.libreta.application;

import static com.nodenull.libreta.domain.model.CourseFixtures.DEFAULT_WEIGHTS;
import static com.nodenull.libreta.domain.model.CourseFixtures.courseNamed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nodenull.libreta.domain.model.Course;
import com.nodenull.libreta.domain.model.EvaluationSlot;
import com.nodenull.libreta.domain.model.GoalStatus;
import com.nodenull.libreta.domain.model.PassStatus;
import com.nodenull.libreta.domain.model.SubstituteOutcome;
import com.nodenull.libreta.domain.repository.CourseRepository;
import com.nodenull.libreta.domain.service.GoalStatusService;
import com.nodenull.libreta.domain.service.GradeCalculator;
import com.nodenull.libreta.domain.service.PassStatusService;
import com.nodenull.libreta.domain.service.SubstituteService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas unitarias del servicio de aplicación: el repositorio y el generador de ids son dobles (mocks). */
@ExtendWith(MockitoExtension.class)
class CourseApplicationServiceTest {

    @Mock
    private CourseRepository repository;

    @Mock
    private IdGenerator ids;

    private CourseApplicationService service;

    @BeforeEach
    void setUp() {
        GradeCalculator calculator = new GradeCalculator();
        service = new CourseApplicationService(repository, ids, calculator,
                new PassStatusService(calculator), new GoalStatusService(calculator),
                new SubstituteService(calculator));
    }

    // ---------- crear ----------

    @Test
    void crear_asignaElIdGeneradoYGuardaElCurso() {
        when(ids.next()).thenReturn("c-1");

        Course created = service.create(new CreateCourseCommand("Cálculo III", "k-1", 4, DEFAULT_WEIGHTS.clone()));

        assertEquals("c-1", created.id());
        verify(repository).save(created);
    }

    @Test
    void crearConPesosInvalidos_lanzaExcepcionYNoGuarda() {
        when(ids.next()).thenReturn("c-1");
        double[] invalid = {0.5, 0.5, 0.5, 0, 0, 0};

        assertThrows(IllegalArgumentException.class,
                () -> service.create(new CreateCourseCommand("Cálculo III", "k-1", 4, invalid)));

        verify(repository, never()).save(any(Course.class));
    }

    // ---------- consultar ----------

    @Test
    void obtener_devuelveElCursoDelRepositorio() {
        Course course = courseNamed("c-1", "Cálculo");
        when(repository.findById("c-1")).thenReturn(Optional.of(course));

        assertSame(course, service.get("c-1"));
    }

    @Test
    void obtenerInexistente_lanzaCourseNotFound() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class, () -> service.get("x"));
    }

    @Test
    void listar_devuelveTodosLosCursos() {
        List<Course> all = List.of(courseNamed("c-1", "Cálculo"), courseNamed("c-2", "Física"));
        when(repository.findAll()).thenReturn(all);

        assertEquals(2, service.list().size());
    }

    // ---------- registrar notas ----------

    @Test
    void registrarNota_actualizaElCursoYLoGuarda() {
        Course course = courseNamed("c-1", "Cálculo");
        when(repository.findById("c-1")).thenReturn(Optional.of(course));

        service.recordGrade("c-1", EvaluationSlot.PARCIAL_1, 15);

        assertEquals(15, course.gradeAt(EvaluationSlot.PARCIAL_1));
        verify(repository).save(course);
    }

    @Test
    void registrarNotaInvalida_lanzaExcepcionYNoGuarda() {
        Course course = courseNamed("c-1", "Cálculo");
        when(repository.findById("c-1")).thenReturn(Optional.of(course));

        assertThrows(IllegalArgumentException.class,
                () -> service.recordGrade("c-1", EvaluationSlot.PARCIAL_1, 25));

        verify(repository, never()).save(any(Course.class));
    }

    @Test
    void registrarNotaEnCursoInexistente_lanzaCourseNotFound() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class,
                () -> service.recordGrade("x", EvaluationSlot.PARCIAL_1, 15));

        verify(repository, never()).save(any(Course.class));
    }

    // ---------- resumen ----------

    @Test
    void resumen_calculaAcumuladoProyeccionYEstados() {
        Course course = courseNamed("c-1", "Cálculo", 15, 10, null, null, null, null);
        when(repository.findById("c-1")).thenReturn(Optional.of(course));

        CourseSummary summary = service.summarize("c-1");

        assertEquals(3.75, summary.accumulated(), 1e-6);
        assertEquals(0.70, summary.remainingWeight(), 1e-6);
        assertEquals(12.5, summary.projection().orElseThrow(), 1e-6);
        assertEquals(PassStatus.EN_CURSO, summary.pass().status());
        assertEquals(GoalStatus.VAS_BIEN, summary.goal());
    }

    @Test
    void resumenSinNotas_noTieneProyeccion() {
        when(repository.findById("c-1")).thenReturn(Optional.of(courseNamed("c-1", "Cálculo")));

        assertTrue(service.summarize("c-1").projection().isEmpty());
    }

    // ---------- sustitutorio ----------

    @Test
    void aplicarSustitutorio_guardaCuandoSeAplica() {
        Course course = courseNamed("c-1", "Cálculo", null, 10, null, 15, null, null);
        when(repository.findById("c-1")).thenReturn(Optional.of(course));

        assertEquals(SubstituteOutcome.APPLIED, service.applySubstitute("c-1", 18));

        verify(repository).save(course);
    }

    @Test
    void aplicarSustitutorioQueNoSupera_noGuarda() {
        Course course = courseNamed("c-1", "Cálculo", null, 10, null, 15, null, null);
        when(repository.findById("c-1")).thenReturn(Optional.of(course));

        assertEquals(SubstituteOutcome.NOT_HIGHER_THAN_LOWEST, service.applySubstitute("c-1", 9));

        verify(repository, never()).save(any(Course.class));
    }

    // ---------- archivar y eliminar ----------

    @Test
    void archivar_marcaElCursoYLoGuarda() {
        Course course = courseNamed("c-1", "Cálculo");
        when(repository.findById("c-1")).thenReturn(Optional.of(course));

        service.archive("c-1");

        assertTrue(course.isArchived());
        verify(repository).save(course);
    }

    @Test
    void eliminarExistente_delegaEnElRepositorio() {
        when(repository.existsById("c-1")).thenReturn(true);

        service.delete("c-1");

        verify(repository).deleteById("c-1");
    }

    @Test
    void eliminarInexistente_lanzaCourseNotFoundYNoElimina() {
        when(repository.existsById("x")).thenReturn(false);

        assertThrows(CourseNotFoundException.class, () -> service.delete("x"));

        verify(repository, never()).deleteById(any(String.class));
    }
}
