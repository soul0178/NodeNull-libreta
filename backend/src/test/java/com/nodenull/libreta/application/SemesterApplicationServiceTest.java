package com.nodenull.libreta.application;

import static com.nodenull.libreta.domain.model.CourseFixtures.courseNamed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nodenull.libreta.domain.model.Course;
import com.nodenull.libreta.domain.model.Semester;
import com.nodenull.libreta.domain.repository.CourseRepository;
import com.nodenull.libreta.domain.repository.SemesterRepository;
import com.nodenull.libreta.domain.service.GradeCalculator;
import com.nodenull.libreta.domain.service.SemesterService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SemesterApplicationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-15T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private CourseRepository courses;

    @Mock
    private SemesterRepository semesters;

    @Mock
    private IdGenerator ids;

    private SemesterApplicationService service;

    @BeforeEach
    void setUp() {
        service = new SemesterApplicationService(courses, semesters, ids,
                new SemesterService(new GradeCalculator(), CLOCK));
    }

    private Course complete(String id, String name) {
        return courseNamed(id, name, 12, 12, 12, 12, 12, 12);
    }

    @Test
    void cerrar_guardaElSnapshotYLuegoEliminaLosCursosActivos() {
        when(courses.findAll()).thenReturn(List.of(complete("1", "Cálculo"), complete("2", "Física")));
        when(semesters.count()).thenReturn(0L);
        when(ids.next()).thenReturn("s-1");

        Semester closed = service.close("2026-A");

        assertEquals("2026-A", closed.label());
        InOrder order = inOrder(semesters, courses);
        order.verify(semesters).save(closed);
        order.verify(courses).deleteById("1");
        order.verify(courses).deleteById("2");
    }

    @Test
    void cerrarConCursosPendientes_noGuardaNiElimina() {
        Course pending = courseNamed("2", "Física", 12, 12, null, null, null, null);
        when(courses.findAll()).thenReturn(List.of(complete("1", "Cálculo"), pending));
        when(semesters.count()).thenReturn(0L);
        when(ids.next()).thenReturn("s-1");

        assertThrows(IllegalStateException.class, () -> service.close("2026-A"));

        verify(semesters, never()).save(any(Semester.class));
        verify(courses, never()).deleteById(any(String.class));
    }

    @Test
    void cerrarSinCursos_lanzaExcepcion() {
        when(courses.findAll()).thenReturn(List.of());
        when(semesters.count()).thenReturn(0L);
        when(ids.next()).thenReturn("s-1");

        assertThrows(IllegalStateException.class, () -> service.close("2026-A"));

        verify(semesters, never()).save(any(Semester.class));
    }

    @Test
    void cerrarSinEtiqueta_numeraElSemestreSegunLosYaCerrados() {
        when(courses.findAll()).thenReturn(List.of(complete("1", "Cálculo")));
        when(semesters.count()).thenReturn(2L);
        when(ids.next()).thenReturn("s-3");

        assertEquals("Semestre 3", service.close("").label());
    }

    @Test
    void reabrir_restauraLosCursosYEliminaElSemestre() {
        Semester semester = new Semester("s-1", "2026-A", 1L,
                List.of(complete("1", "Cálculo"), complete("2", "Física")));
        when(semesters.findById("s-1")).thenReturn(Optional.of(semester));

        List<Course> restored = service.reopen("s-1");

        assertEquals(2, restored.size());
        verify(courses, times(2)).save(any(Course.class));
        verify(semesters).deleteById("s-1");
    }

    @Test
    void reabrirInexistente_lanzaSemesterNotFoundYNoToca() {
        when(semesters.findById("x")).thenReturn(Optional.empty());

        assertThrows(SemesterNotFoundException.class, () -> service.reopen("x"));

        verify(courses, never()).save(any(Course.class));
        verify(semesters, never()).deleteById(any(String.class));
    }

    @Test
    void listar_devuelveLosSemestresDelRepositorio() {
        Semester semester = new Semester("s-1", "2026-A", 1L, List.of(complete("1", "Cálculo")));
        when(semesters.findAllNewestFirst()).thenReturn(List.of(semester));

        assertEquals(1, service.list().size());
    }
}
