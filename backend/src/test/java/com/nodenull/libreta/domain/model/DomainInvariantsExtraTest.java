package com.nodenull.libreta.domain.model;

import static com.nodenull.libreta.domain.model.CourseFixtures.course;
import static com.nodenull.libreta.domain.model.CourseFixtures.courseNamed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Casos complementarios de invariantes, para completar la cobertura del dominio. */
class DomainInvariantsExtraTest {

    @Test
    void renombrarCurso_recortaEspacios() {
        Course course = course();
        course.rename("  Álgebra ");
        assertEquals("Álgebra", course.name());
    }

    @Test
    void renombrarCursoConVacio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> course().rename(" "));
    }

    @Test
    void cambiarCreditosNegativos_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> course().changeCredits(-2));
    }

    @Test
    void cambiarCarreraVacia_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> course().changeCareer(""));
    }

    @Test
    void dosCursosConElMismoId_sonIguales() {
        assertEquals(courseNamed("c-1", "A"), courseNamed("c-1", "B"));
    }

    @Test
    void copiaDeUnCurso_esIndependiente() {
        Course original = courseNamed("c-1", "A", 10);
        Course copy = original.copy();
        copy.setGrade(EvaluationSlot.CONTINUA_1, 20);
        assertEquals(10, original.gradeAt(EvaluationSlot.CONTINUA_1));
    }

    @Test
    void semestreSinCursos_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Semester("s-1", "2026-A", 1L, List.of()));
    }

    @Test
    void semestreSinEtiqueta_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> new Semester("s-1", " ", 1L, List.of(course())));
    }

    @Test
    void estadosTienenEtiquetaLegible() {
        assertEquals("Aprobado", PassStatus.APROBADO.label());
        assertTrue(GoalStatus.EN_RIESGO.label().contains("riesgo"));
    }

    @Test
    void sustitutorioSinEvaluacionObjetivo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Substitute(15, null, 10));
    }
}
