package com.nodenull.libreta.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CareerTest {

    @Test
    void datosValidos_creaCarrera() {
        Career career = new Career("k-1", "Ciencia de la Computación");
        assertEquals("Ciencia de la Computación", career.name());
    }

    @Test
    void nombreVacio_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Career("k-1", "   "));
    }

    @Test
    void nombreNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Career("k-1", null));
    }

    @Test
    void renombrar_recortaEspacios() {
        Career career = new Career("k-1", "Filosofía");
        career.rename("  Literatura ");
        assertEquals("Literatura", career.name());
    }

    @Test
    void renombrarConVacio_lanzaExcepcion() {
        Career career = new Career("k-1", "Filosofía");
        assertThrows(IllegalArgumentException.class, () -> career.rename(""));
    }
}
