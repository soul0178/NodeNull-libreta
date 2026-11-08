package com.nodenull.libreta.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Funcionalidad: Registrar la nota de una evaluación (0 a 20, se redondea al entero y se acota al rango).
 * Curso base: pesos 15/15/15/15/20/20; la evaluación 0 es Continua 1 (peso 15 %).
 */
class RegistrarNotaTest extends BaseFunctionalTest {

    @BeforeEach
    void crearCursoBase() {
        createDefaultCourse();
    }

    @Test
    @DisplayName("RN-01 Nota válida (15) se muestra y actualiza el acumulado")
    void rn01_notaValida_seMuestraYAcumula() {
        page.enterGrade(0, "15");

        assertEquals("15", page.gradeText(0));
        assertEquals("2,25", page.accumulatedText());
    }

    @Test
    @DisplayName("RN-02 Decimal 14,6 se redondea hacia arriba a 15")
    void rn02_decimalHaciaArriba_redondea() {
        page.enterGrade(0, "14.6");

        assertEquals("15", page.gradeText(0));
    }

    @Test
    @DisplayName("RN-03 Decimal 14,4 se redondea hacia abajo a 14")
    void rn03_decimalHaciaAbajo_redondea() {
        page.enterGrade(0, "14.4");

        assertEquals("14", page.gradeText(0));
    }

    @Test
    @DisplayName("RN-04 Valor límite inferior: 0 se acepta")
    void rn04_limiteInferior0_seAcepta() {
        page.enterGrade(0, "0");

        assertEquals("0", page.gradeText(0));
        assertEquals("0,00", page.accumulatedText());
    }

    @Test
    @DisplayName("RN-05 Valor límite superior: 20 se acepta")
    void rn05_limiteSuperior20_seAcepta() {
        page.enterGrade(0, "20");

        assertEquals("20", page.gradeText(0));
        assertEquals("3,00", page.accumulatedText());
    }

    @Test
    @DisplayName("RN-06 Nota sobre el máximo (21) se acota a 20")
    void rn06_sobreElMaximo_seAcotaA20() {
        page.enterGrade(0, "21");

        assertEquals("20", page.gradeText(0));
    }

    @Test
    @DisplayName("RN-07 Nota negativa (-1) se acota a 0")
    void rn07_negativa_seAcotaA0() {
        page.enterGrade(0, "-1");

        assertEquals("0", page.gradeText(0));
    }

    @Test
    @DisplayName("RN-08 Dejar el campo vacío borra la nota")
    void rn08_campoVacio_borraLaNota() {
        page.enterGrade(0, "15").enterGrade(0, "");

        assertEquals("—", page.gradeText(0));
        assertEquals("—", page.projectedText());
    }

    @Test
    @DisplayName("RN-09 Escape cancela la edición y conserva la nota anterior")
    void rn09_escape_conservaLaNota() {
        page.enterGrade(0, "18").typeGradeAndCancel(0, "7");

        assertEquals("18", page.gradeText(0));
    }

    @Test
    @DisplayName("RN-10 Con todas las notas en 20 el curso queda Aprobado")
    void rn10_todasEn20_aprobado() {
        for (int i = 0; i < 6; i++) {
            page.enterGrade(i, "20");
        }

        assertTrue(page.passBannerText().contains("Aprobado"));
    }

    @Test
    @DisplayName("RN-11 Con todas las notas en 5 el curso queda Desaprobado")
    void rn11_todasEn5_desaprobado() {
        for (int i = 0; i < 6; i++) {
            page.enterGrade(i, "5");
        }

        assertTrue(page.passBannerText().contains("Desaprobado"));
    }

    @Test
    @DisplayName("RN-12 Con 20 y 20 en la primera unidad el estado es En curso y pide 7")
    void rn12_primeraUnidadEn20_enCursoPideSiete() {
        page.enterGrade(0, "20").enterGrade(1, "20");

        assertTrue(page.passBannerText().contains("En curso"));
        assertTrue(page.passBannerText().contains("al menos 7"));
        assertEquals("6,00", page.accumulatedText());
    }
}
