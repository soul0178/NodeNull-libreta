package com.nodenull.libreta.domain.service;

import static com.nodenull.libreta.domain.model.CourseFixtures.courseWithGrades;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RoundingAndFinalValueTest {

    private final GradeCalculator calculator = new GradeCalculator();

    @Test
    void roundGrade_redondeaAlEnteroMasCercano() {
        assertEquals(12, calculator.roundGrade(12.2));
        assertEquals(13, calculator.roundGrade(12.5));
        assertEquals(13, calculator.roundGrade(12.8));
    }

    @Test
    void roundGrade_corrigeImprecisionesDePuntoFlotante() {
        // 12.49999999999996 es conceptualmente 12.5 y debe subir a 13
        assertEquals(13, calculator.roundGrade(12.49999999999996));
    }

    @Test
    void finalValue_usaElAcumuladoCuandoNoQuedaPesoPendiente() {
        var course = courseWithGrades(12, 12, 12, 12, 12, 12);
        assertEquals(12.0, calculator.finalValue(course).orElseThrow(), 1e-6);
    }

    @Test
    void finalValue_usaLaProyeccionCuandoFaltaPeso() {
        var course = courseWithGrades(16, null, null, null, null, null);
        assertEquals(calculator.projection(course).orElseThrow(), calculator.finalValue(course).orElseThrow(), 1e-6);
    }
}
