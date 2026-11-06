package com.nodenull.libreta.domain.service;

import static com.nodenull.libreta.domain.model.CourseFixtures.applyGrades;
import static com.nodenull.libreta.domain.model.CourseFixtures.course;
import static com.nodenull.libreta.domain.model.CourseFixtures.courseWithGrades;
import static com.nodenull.libreta.domain.model.CourseFixtures.courseWithWeights;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nodenull.libreta.domain.model.Course;
import com.nodenull.libreta.domain.model.EvaluationSlot;
import com.nodenull.libreta.domain.model.Substitute;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Especificación portada de test/calc.test.js (Vitest) de la v0.1. */
class GradeCalculatorTest {

    private final GradeCalculator calculator = new GradeCalculator();

    // ---------- notas efectivas / ingresadas / faltantes ----------

    @Test
    void sinSustitutorio_devuelveLasNotasTalCual() {
        Course c = courseWithGrades(15, 10, null, null, null, null);
        assertEquals(List.of(EvaluationSlot.CONTINUA_1, EvaluationSlot.PARCIAL_1), calculator.enteredSlots(c));
        assertEquals(4, calculator.missingSlots(c).size());
        assertEquals(15.0, calculator.effectiveGrades(c)[0], 1e-9);
    }

    @Test
    void conSustitutorio_reemplazaLaNotaObjetivo() {
        Course c = courseWithGrades(15, 8, null, 12, null, null);
        c.setSubstitute(new Substitute(18, EvaluationSlot.PARCIAL_1, 8));
        assertEquals(18.0, calculator.effectiveGrades(c)[EvaluationSlot.PARCIAL_1.index()], 1e-9);
        assertEquals(3, calculator.enteredSlots(c).size());
    }

    // ---------- acumulado / peso restante ----------

    @Test
    void acumulado_sumaPonderadaSoloDeLoIngresado() {
        Course c = courseWithGrades(15, 10, null, null, null, null);
        // 15*0.15 + 10*0.15 = 3.75
        assertEquals(3.75, calculator.accumulated(c), 1e-6);
        assertEquals(0.70, calculator.remainingWeight(c), 1e-6);
    }

    @Test
    void todoIngresado_acumuladoEsElPromedioYNoQuedaPeso() {
        Course c = courseWithGrades(12, 12, 12, 12, 12, 12);
        assertEquals(12.0, calculator.accumulated(c), 1e-6);
        assertEquals(0.0, calculator.remainingWeight(c), 1e-6);
    }

    // ---------- proyección ----------

    @Test
    void sinNotas_proyeccionVacia() {
        assertTrue(calculator.projection(course()).isEmpty());
    }

    @Test
    void conUnaSolaNota_proyectaLineaPlana() {
        Course c = courseWithGrades(16, null, null, null, null, null);
        assertEquals(16.0, calculator.projection(c).orElseThrow(), 1e-6);
    }

    @Test
    void dosNotasDeLaMismaUnidad_proyectanSuPromedio() {
        Course c = courseWithGrades(19, 17, null, null, null, null);
        assertEquals(18.0, calculator.projection(c).orElseThrow(), 1e-6);
    }

    @Test
    void notasDeUnidadesDistintas_ajustanRectaYClipean() {
        Course c = courseWithGrades(10, 10, 14, null, null, null);
        // recta y = 4x + 6 -> P2 = 14, U3 = 18 -> acumulado total 14.4
        assertEquals(14.4, calculator.projection(c).orElseThrow(), 1e-6);
    }

    @Test
    void evaluacionConPesoCero_noInfluyeEnLaProyeccion() {
        Course c = courseWithWeights(0.3, 0, 0.3, 0, 0.4, 0);
        applyGrades(c, 17, 0, null, null, null, null);
        assertEquals(17.0, calculator.projection(c).orElseThrow(), 1e-6);
        assertEquals(0.0, calculator.trendSlope(c), 1e-9);
    }

    @Test
    void dentroDeUnaUnidad_laNotaConMasPesoPesaMas() {
        Course c = courseWithWeights(0.1, 0.4, 0.2, 0.1, 0.1, 0.1);
        applyGrades(c, 10, 16, null, null, null, null);
        assertEquals(14.8, calculator.projection(c).orElseThrow(), 1e-6);
    }

    @Test
    void siTodasLasNotasPesanCero_usaPesoIgualComoRespaldo() {
        Course c = courseWithWeights(0.5, 0, 0.5, 0, 0, 0);
        applyGrades(c, null, 15, null, null, null, null);
        assertTrue(calculator.projection(c).isPresent());
    }

    @Test
    void proyeccionNuncaSuperaElRangoDe0a20() {
        Course c = courseWithGrades(0, 0, 10, 10, 20, null);
        double p = calculator.projection(c).orElseThrow();
        assertTrue(p >= 0 && p <= 20);
    }

    // ---------- tendencia ----------

    @Test
    void conMenosDeDosNotas_pendienteCero() {
        Course c = courseWithGrades(15, null, null, null, null, null);
        assertEquals(0.0, calculator.trendSlope(c), 1e-9);
    }

    @Test
    void dosNotasDeLaMismaUnidad_noGeneranTendencia() {
        Course c = courseWithGrades(19, 17, null, null, null, null);
        assertEquals(0.0, calculator.trendSlope(c), 1e-9);
    }

    @Test
    void tendenciaAlAlza_entreUnidades() {
        Course c = courseWithGrades(10, 10, 14, 14, null, null);
        assertEquals(4.0, calculator.trendSlope(c), 1e-6);
    }

    @Test
    void tendenciaALaBaja_entreUnidades() {
        Course c = courseWithGrades(16, 16, 12, 12, null, null);
        assertTrue(calculator.trendSlope(c) < 0);
    }
}
