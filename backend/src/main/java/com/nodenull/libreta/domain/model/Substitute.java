package com.nodenull.libreta.domain.model;

/**
 * Examen sustitutorio aplicado a un curso: reemplaza la nota de {@code target}.
 *
 * @param value         nota obtenida en el sustitutorio (0-20)
 * @param target        evaluación que reemplaza
 * @param originalValue nota original de la evaluación reemplazada (puede ser null)
 */
public record Substitute(int value, EvaluationSlot target, Integer originalValue) {

    public Substitute {
        if (value < Course.MIN_GRADE || value > Course.MAX_GRADE) {
            throw new IllegalArgumentException("La nota del sustitutorio debe estar entre 0 y 20");
        }
        if (target == null) {
            throw new IllegalArgumentException("El sustitutorio debe indicar la evaluación que reemplaza");
        }
    }
}
