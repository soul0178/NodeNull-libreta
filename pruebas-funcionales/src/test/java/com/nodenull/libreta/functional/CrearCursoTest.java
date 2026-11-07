package com.nodenull.libreta.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Funcionalidad: Crear curso (formulario con nombre, carrera, créditos y pesos por evaluación).
 * Diseño de caja negra: clases de equivalencia y valores límite. Ver docs/pruebas/lab06-casos-de-prueba.md.
 */
class CrearCursoTest extends BaseFunctionalTest {

    private static final String SUM_ERROR = "Los pesos deben sumar 100%.";
    private static final String NAME_ERROR = "Ponle un nombre al curso.";

    @Test
    @DisplayName("CP-01 Datos válidos crean el curso y abren su detalle")
    void cp01_datosValidos_creaElCurso() {
        page.createCourse("Cálculo III", "4", DEFAULT_WEIGHTS).waitForCourseDetail();

        assertEquals("Cálculo III", page.courseTitle());
        assertTrue(page.courseMeta().contains("4 créditos"));
    }

    @Test
    @DisplayName("CP-02 Nombre vacío muestra error y no crea el curso")
    void cp02_nombreVacio_muestraError() {
        page.createCourse("", "4", DEFAULT_WEIGHTS);

        assertEquals(NAME_ERROR, page.waitForError());
        assertTrue(page.isModalOpen());
    }

    @Test
    @DisplayName("CP-03 Nombre de solo espacios se trata como vacío")
    void cp03_nombreSoloEspacios_muestraError() {
        page.createCourse("   ", "4", DEFAULT_WEIGHTS);

        assertEquals(NAME_ERROR, page.waitForError());
    }

    @Test
    @DisplayName("CP-04 Pesos con ceros que suman 100 % se aceptan")
    void cp04_pesosConCeros_seAcepta() {
        page.createCourse("Física", "3", "30", "0", "30", "0", "40", "0").waitForCourseDetail();

        assertEquals("Física", page.courseTitle());
    }

    @Test
    @DisplayName("CP-05 Suma de pesos menor a 100 % (90) se rechaza")
    void cp05_sumaMenorA100_seRechaza() {
        page.createCourse("Química", "3", "15", "15", "15", "15", "15", "15");

        assertEquals(SUM_ERROR, page.waitForError());
        assertTrue(page.isModalOpen());
    }

    @Test
    @DisplayName("CP-06 Suma de pesos mayor a 100 % (110) se rechaza")
    void cp06_sumaMayorA100_seRechaza() {
        page.createCourse("Química", "3", "20", "20", "20", "20", "20", "10");

        assertEquals(SUM_ERROR, page.waitForError());
    }

    @Test
    @DisplayName("CP-07 Valor límite inferior: suma 99,5 % se acepta")
    void cp07_limiteInferior995_seAcepta() {
        page.createCourse("Límites A", "3", "15", "15", "15", "15", "19.75", "19.75").waitForCourseDetail();

        assertEquals("Límites A", page.courseTitle());
    }

    @Test
    @DisplayName("CP-08 Justo debajo del límite: suma 99,4 % se rechaza")
    void cp08_debajoDelLimite994_seRechaza() {
        page.createCourse("Límites B", "3", "15", "15", "15", "15", "19.7", "19.7");

        assertEquals(SUM_ERROR, page.waitForError());
    }

    @Test
    @DisplayName("CP-09 Valor límite superior: suma 100,5 % se acepta")
    void cp09_limiteSuperior1005_seAcepta() {
        page.createCourse("Límites C", "3", "15", "15", "15", "15", "20.25", "20.25").waitForCourseDetail();

        assertEquals("Límites C", page.courseTitle());
    }

    @Test
    @DisplayName("CP-10 Justo encima del límite: suma 100,6 % se rechaza")
    void cp10_encimaDelLimite1006_seRechaza() {
        page.createCourse("Límites D", "3", "15", "15", "15", "15", "20.3", "20.3");

        assertEquals(SUM_ERROR, page.waitForError());
    }

    @Test
    @DisplayName("CP-11 Sin ingresar pesos se rechaza")
    void cp11_sinPesos_seRechaza() {
        page.createCourse("Sin pesos", "3");

        assertEquals(SUM_ERROR, page.waitForError());
    }

    @Test
    @DisplayName("CP-12 El indicador de suma se actualiza mientras se escribe")
    void cp12_indicadorDeSuma_seActualizaEnVivo() {
        page.openCreateCourseModal().typeWeights("50");
        assertEquals("Suma actual: 50.0%", page.weightSumText());
        assertFalse(page.weightSumIsOk());

        page.typeWeights("50", "50");
        assertEquals("Suma actual: 100.0%", page.weightSumText());
        assertTrue(page.weightSumIsOk());
    }

    @Test
    @DisplayName("CP-13 Cancelar cierra el formulario sin crear el curso")
    void cp13_cancelar_noCreaNada() {
        page.openCreateCourseModal().typeName("Se cancela").cancelModal();

        assertFalse(page.isModalOpen());
        assertTrue(page.isEmptyDashboardVisible());
    }

    @Test
    @DisplayName("CP-14 Créditos vacíos: el curso se crea sin créditos")
    void cp14_creditosVacios_creaSinCreditos() {
        page.createCourse("Sin créditos", null, DEFAULT_WEIGHTS).waitForCourseDetail();

        assertTrue(page.courseMeta().contains("sin créditos"));
    }

    @Test
    @DisplayName("CP-15 Créditos negativos se rechazan (DEF-01)")
    void cp15_creditosNegativos_seRechazan() {
        page.createCourse("Negativo", "-3", DEFAULT_WEIGHTS);

        assertTrue(page.waitForError().contains("créditos"));
        assertTrue(page.isModalOpen());
    }

    @Test
    @DisplayName("CP-16 Un peso negativo se rechaza aunque la suma dé 100 % (DEF-02)")
    void cp16_pesoNegativo_seRechaza() {
        page.createCourse("Peso negativo", "3", "-10", "35", "15", "15", "20", "25");

        assertTrue(page.waitForError().contains("negativo"));
        assertTrue(page.isModalOpen());
    }

    @Test
    @DisplayName("CP-17 Créditos decimales se rechazan (DEF-03)")
    void cp17_creditosDecimales_seRechazan() {
        page.createCourse("Decimal", "3.5", DEFAULT_WEIGHTS);

        assertTrue(page.waitForError().contains("créditos"));
        assertTrue(page.isModalOpen());
    }
}
