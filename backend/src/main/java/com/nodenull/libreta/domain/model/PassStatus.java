package com.nodenull.libreta.domain.model;

/** Estado de aprobación de un curso frente a la nota mínima (10,5). */
public enum PassStatus {
    APROBADO("Aprobado"),
    DESAPROBADO("Desaprobado"),
    NO_PODRAS_APROBAR("No podrás aprobar"),
    MUY_DIFICIL_APROBAR("Muy difícil aprobar"),
    APROBACION_ASEGURADA("Aprobación asegurada"),
    EN_CURSO("En curso");

    private final String label;

    PassStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
