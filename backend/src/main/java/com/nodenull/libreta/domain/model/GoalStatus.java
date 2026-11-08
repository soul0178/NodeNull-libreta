package com.nodenull.libreta.domain.model;

/** Estado de un curso frente a la meta personal del estudiante. */
public enum GoalStatus {
    META_ALCANZADA("Meta alcanzada"),
    META_ALCANZADA_POR_REDONDEO("Meta alcanzada (por redondeo)"),
    META_NO_ALCANZADA("Meta no alcanzada"),
    SIN_DATOS("Meta: sin datos aún"),
    VAS_BIEN("Vas bien para tu meta"),
    META_AJUSTADA("Meta ajustada (por redondeo)"),
    EN_RIESGO("En riesgo de no llegar a tu meta");

    private final String label;

    GoalStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
