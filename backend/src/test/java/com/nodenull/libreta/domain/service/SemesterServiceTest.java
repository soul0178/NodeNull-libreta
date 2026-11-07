package com.nodenull.libreta.domain.service;

import static com.nodenull.libreta.domain.model.CourseFixtures.courseNamed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nodenull.libreta.domain.model.Course;
import com.nodenull.libreta.domain.model.EvaluationSlot;
import com.nodenull.libreta.domain.model.Semester;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class SemesterServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-15T10:00:00Z"), ZoneOffset.UTC);

    private final SemesterService service = new SemesterService(new GradeCalculator(), CLOCK);

    private Course complete(String id, String name) {
        return courseNamed(id, name, 12, 12, 12, 12, 12, 12);
    }

    private Course incomplete(String id, String name) {
        return courseNamed(id, name, 12, 12, null, null, null, null);
    }

    @Test
    void sinCursosActivos_lanzaExcepcion() {
        assertThrows(IllegalStateException.class, () -> service.close("s-1", "2026-A", List.of(), 0));
    }

    @Test
    void conCursosConNotasPendientes_lanzaExcepcionConSusNombres() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> service.close("s-1", "2026-A", List.of(complete("1", "Cálculo"), incomplete("2", "Física")), 0));
        assertTrue(e.getMessage().contains("Física"));
    }

    @Test
    void cursoArchivadoIncompleto_noImpideElCierre() {
        Course archived = incomplete("2", "Física");
        archived.archive();
        Semester semester = service.close("s-1", "2026-A", List.of(complete("1", "Cálculo"), archived), 0);
        assertEquals(2, semester.courses().size());
    }

    @Test
    void cierreExitoso_registraEtiquetaYFechaDelReloj() {
        Semester semester = service.close("s-1", "2026-A", List.of(complete("1", "Cálculo")), 0);
        assertEquals("2026-A", semester.label());
        assertEquals(CLOCK.millis(), semester.closedAt());
    }

    @Test
    void sinEtiqueta_usaSemestreNumerado() {
        Semester semester = service.close("s-3", "  ", List.of(complete("1", "Cálculo")), 2);
        assertEquals("Semestre 3", semester.label());
    }

    @Test
    void elSemestreGuardaUnaCopia_loCambiosPosterioresNoLoAfectan() {
        Course original = complete("1", "Cálculo");
        Semester semester = service.close("s-1", "2026-A", List.of(original), 0);
        original.setGrade(EvaluationSlot.CONTINUA_1, 0);
        assertEquals(12, semester.courses().get(0).gradeAt(EvaluationSlot.CONTINUA_1));
    }

    @Test
    void incompleteCourses_ignoraArchivadosYCompletos() {
        Course archived = incomplete("3", "Química");
        archived.archive();
        List<Course> pending = service.incompleteCourses(
                List.of(complete("1", "Cálculo"), incomplete("2", "Física"), archived));
        assertEquals(1, pending.size());
        assertEquals("Física", pending.get(0).name());
    }

    @Test
    void reabrir_devuelveCopiasDeLosCursos() {
        Semester semester = service.close("s-1", "2026-A", List.of(complete("1", "Cálculo")), 0);
        List<Course> reopened = semester.reopen();
        assertEquals(1, reopened.size());
        reopened.get(0).setGrade(EvaluationSlot.CONTINUA_1, 0);
        assertEquals(12, semester.courses().get(0).gradeAt(EvaluationSlot.CONTINUA_1));
    }
}
