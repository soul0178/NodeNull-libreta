package com.nodenull.libreta.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EvaluationSlotTest {

    @Test
    void hayExactamenteSeisEvaluaciones() {
        assertEquals(6, EvaluationSlot.values().length);
    }

    @Test
    void continuaYParcialDeUnaMismaUnidadComparteUnidad() {
        assertEquals(1, EvaluationSlot.CONTINUA_1.unit());
        assertEquals(1, EvaluationSlot.PARCIAL_1.unit());
        assertEquals(2, EvaluationSlot.CONTINUA_2.unit());
        assertEquals(2, EvaluationSlot.PARCIAL_2.unit());
        assertEquals(3, EvaluationSlot.CONTINUA_3.unit());
        assertEquals(3, EvaluationSlot.PARCIAL_3.unit());
    }

    @Test
    void tipoAlternaContinuaYParcial() {
        assertEquals(EvaluationType.CONTINUA, EvaluationSlot.CONTINUA_2.type());
        assertEquals(EvaluationType.PARCIAL, EvaluationSlot.PARCIAL_2.type());
    }
}
