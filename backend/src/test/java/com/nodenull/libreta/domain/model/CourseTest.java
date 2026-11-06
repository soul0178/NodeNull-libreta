package com.nodenull.libreta.domain.model;

import static com.nodenull.libreta.domain.model.CourseFixtures.DEFAULT_WEIGHTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CourseTest {

    // ---------- creación ----------

    @Test
    void datosValidos_creaCursoConValoresPorDefecto() {
        Course course = CourseFixtures.course();
        assertEquals("Cálculo III", course.name());
        assertEquals(11, course.goal());
        assertFalse(course.isArchived());
        assertTrue(course.substitute().isEmpty());
        for (EvaluationSlot slot : EvaluationSlot.values()) {
            assertNull(course.gradeAt(slot));
        }
    }

    @Test
    void nombreVacio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> Course.create("c-1", "  ", "k-1", 4, DEFAULT_WEIGHTS.clone()));
    }

    @Test
    void creditosNegativos_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> Course.create("c-1", "Física", "k-1", -1, DEFAULT_WEIGHTS.clone()));
    }

    @Test
    void sinCarrera_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> Course.create("c-1", "Física", " ", 3, DEFAULT_WEIGHTS.clone()));
    }

    // ---------- pesos (RN-01) ----------

    @Test
    void pesosQueNoSuman100_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> CourseFixtures.courseWithWeights(0.2, 0.2, 0.2, 0.2, 0.2, 0.2));
    }

    @Test
    void pesosConCantidadIncorrecta_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> CourseFixtures.courseWithWeights(0.5, 0.5));
    }

    @Test
    void pesoNegativo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> CourseFixtures.courseWithWeights(-0.1, 0.3, 0.2, 0.2, 0.2, 0.2));
    }

    @Test
    void pesosQueSumanDentroDeLaTolerancia_seAceptan() {
        // 99.6 % está dentro de la tolerancia de 0,5 %
        CourseFixtures.courseWithWeights(0.15, 0.15, 0.15, 0.15, 0.20, 0.196);
    }

    @Test
    void pesosConCeros_seAceptanSiSuman100() {
        CourseFixtures.courseWithWeights(0.3, 0, 0.3, 0, 0.4, 0);
    }

    @Test
    void cambiarPesosInvalidos_lanzaExcepcionYConservaLosAnteriores() {
        Course course = CourseFixtures.course();
        assertThrows(IllegalArgumentException.class,
                () -> course.changeWeights(new double[] {0.5, 0.1, 0, 0, 0, 0}));
        assertEquals(0.15, course.weightAt(EvaluationSlot.CONTINUA_1), 1e-9);
    }

    // ---------- notas (RN-02) ----------

    @Test
    void notaValida_seRegistra() {
        Course course = CourseFixtures.course();
        course.setGrade(EvaluationSlot.PARCIAL_1, 15);
        assertEquals(15, course.gradeAt(EvaluationSlot.PARCIAL_1));
    }

    @Test
    void notaNula_borraLaNota() {
        Course course = CourseFixtures.course();
        course.setGrade(EvaluationSlot.PARCIAL_1, 15);
        course.setGrade(EvaluationSlot.PARCIAL_1, null);
        assertNull(course.gradeAt(EvaluationSlot.PARCIAL_1));
    }

    @Test
    void notaFueraDeRango_lanzaExcepcion() {
        Course course = CourseFixtures.course();
        assertThrows(IllegalArgumentException.class, () -> course.setGrade(EvaluationSlot.PARCIAL_1, 21));
        assertThrows(IllegalArgumentException.class, () -> course.setGrade(EvaluationSlot.PARCIAL_1, -1));
    }

    @Test
    void notasEnLosLimites_seAceptan() {
        Course course = CourseFixtures.course();
        course.setGrade(EvaluationSlot.CONTINUA_1, 0);
        course.setGrade(EvaluationSlot.CONTINUA_2, 20);
        assertEquals(0, course.gradeAt(EvaluationSlot.CONTINUA_1));
        assertEquals(20, course.gradeAt(EvaluationSlot.CONTINUA_2));
    }

    // ---------- meta y archivado ----------

    @Test
    void metaFueraDeRango_lanzaExcepcion() {
        Course course = CourseFixtures.course();
        assertThrows(IllegalArgumentException.class, () -> course.changeGoal(25));
    }

    @Test
    void archivarYDesarchivar() {
        Course course = CourseFixtures.course();
        course.archive();
        assertTrue(course.isArchived());
        course.unarchive();
        assertFalse(course.isArchived());
    }

    // ---------- encapsulación ----------

    @Test
    void modificarLaCopiaDePesos_noAfectaAlCurso() {
        Course course = CourseFixtures.course();
        double[] copia = course.weights();
        copia[0] = 0.99;
        assertEquals(0.15, course.weightAt(EvaluationSlot.CONTINUA_1), 1e-9);
    }

    @Test
    void sustitutorioConNotaInvalida_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> new Substitute(25, EvaluationSlot.PARCIAL_1, 8));
    }
}
