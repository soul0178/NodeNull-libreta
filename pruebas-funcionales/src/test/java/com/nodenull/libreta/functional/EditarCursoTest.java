package com.nodenull.libreta.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Funcionalidad: Editar curso (carrera, créditos y pesos). */
class EditarCursoTest extends BaseFunctionalTest {

    @BeforeEach
    void crearCursoBase() {
        createDefaultCourse();
    }

    @Test
    @DisplayName("ED-01 Cambiar los créditos a un entero válido se guarda")
    void ed01_creditosValidos_seGuardan() {
        page.openEditCourseModal().typeEditCredits("6").confirmEdit().waitForModalClosed();

        assertTrue(page.courseMeta().contains("6 créditos"));
    }

    @Test
    @DisplayName("ED-02 Créditos negativos se rechazan (DEF-01)")
    void ed02_creditosNegativos_seRechazan() {
        page.openEditCourseModal().typeEditCredits("-2").confirmEdit();

        assertTrue(page.waitForError().contains("créditos"));
        assertTrue(page.isModalOpen());
    }

    @Test
    @DisplayName("ED-03 Pesos que no suman 100 % se rechazan")
    void ed03_pesosQueNoSuman100_seRechazan() {
        page.openEditCourseModal().typeWeights("50").confirmEdit();

        assertEquals("Los pesos deben sumar 100%.", page.waitForError());
    }
}
