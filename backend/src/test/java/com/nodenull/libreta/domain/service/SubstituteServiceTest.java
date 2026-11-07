package com.nodenull.libreta.domain.service;

import static com.nodenull.libreta.domain.model.CourseFixtures.course;
import static com.nodenull.libreta.domain.model.CourseFixtures.courseWithGrades;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nodenull.libreta.domain.model.Course;
import com.nodenull.libreta.domain.model.EvaluationSlot;
import com.nodenull.libreta.domain.model.Substitute;
import com.nodenull.libreta.domain.model.SubstituteOutcome;
import org.junit.jupiter.api.Test;

class SubstituteServiceTest {

    private final SubstituteService service = new SubstituteService(new GradeCalculator());

    @Test
    void sinParcialesIngresados_noSeAplica() {
        Course course = course();
        assertEquals(SubstituteOutcome.NO_MIDTERM_ENTERED, service.apply(course, 18));
        assertTrue(course.substitute().isEmpty());
    }

    @Test
    void conAmbosParciales_reemplazaElMasBajo() {
        Course course = courseWithGrades(null, 10, null, 15, null, null);
        assertEquals(SubstituteOutcome.APPLIED, service.apply(course, 18));
        assertEquals(new Substitute(18, EvaluationSlot.PARCIAL_1, 10), course.substitute().orElseThrow());
    }

    @Test
    void sustitutorioQueNoSuperaLaNotaMasBaja_noSeAplica() {
        Course course = courseWithGrades(null, 10, null, 15, null, null);
        assertEquals(SubstituteOutcome.NOT_HIGHER_THAN_LOWEST, service.apply(course, 9));
        assertTrue(course.substitute().isEmpty());
    }

    @Test
    void sustitutorioIgualALaNotaMasBaja_noSeAplica() {
        Course course = courseWithGrades(null, 10, null, 15, null, null);
        assertEquals(SubstituteOutcome.NOT_HIGHER_THAN_LOWEST, service.apply(course, 10));
    }

    @Test
    void siFaltaParcial1_apuntaAParcial2() {
        Course course = courseWithGrades(null, null, null, 12, null, null);
        assertEquals(SubstituteOutcome.APPLIED, service.apply(course, 15));
        assertEquals(EvaluationSlot.PARCIAL_2, course.substitute().orElseThrow().target());
    }

    @Test
    void siFaltaParcial2_apuntaAParcial1() {
        Course course = courseWithGrades(null, 12, null, null, null, null);
        assertEquals(SubstituteOutcome.APPLIED, service.apply(course, 15));
        assertEquals(EvaluationSlot.PARCIAL_1, course.substitute().orElseThrow().target());
    }

    @Test
    void siLosParcialesEmpatan_reemplazaElPrimero() {
        Course course = courseWithGrades(null, 12, null, 12, null, null);
        service.apply(course, 15);
        assertEquals(EvaluationSlot.PARCIAL_1, course.substitute().orElseThrow().target());
    }

    @Test
    void notaDelSustitutorioFueraDeRango_lanzaExcepcion() {
        Course course = courseWithGrades(null, 10, null, 15, null, null);
        assertThrows(IllegalArgumentException.class, () -> service.apply(course, 21));
    }

    @Test
    void quitarSustitutorio_restauraLaSituacionOriginal() {
        Course course = courseWithGrades(null, 10, null, 15, null, null);
        service.apply(course, 18);
        service.remove(course);
        assertTrue(course.substitute().isEmpty());
        assertEquals(10, course.gradeAt(EvaluationSlot.PARCIAL_1));
    }
}
