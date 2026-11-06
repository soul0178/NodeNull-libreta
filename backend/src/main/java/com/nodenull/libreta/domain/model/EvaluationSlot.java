package com.nodenull.libreta.domain.model;

/**
 * Las seis evaluaciones de un curso, en orden: C1, P1, C2, P2, C3, P3.
 * La continua y el parcial de una misma unidad miden el mismo tramo del curso (RN-04).
 */
public enum EvaluationSlot {
    CONTINUA_1(EvaluationType.CONTINUA),
    PARCIAL_1(EvaluationType.PARCIAL),
    CONTINUA_2(EvaluationType.CONTINUA),
    PARCIAL_2(EvaluationType.PARCIAL),
    CONTINUA_3(EvaluationType.CONTINUA),
    PARCIAL_3(EvaluationType.PARCIAL);

    public static final int COUNT = 6;

    private final EvaluationType type;

    EvaluationSlot(EvaluationType type) {
        this.type = type;
    }

    public EvaluationType type() {
        return type;
    }

    /** Unidad a la que pertenece: C1,P1 = 1; C2,P2 = 2; C3,P3 = 3. */
    public int unit() {
        return ordinal() / 2 + 1;
    }

    public int index() {
        return ordinal();
    }
}
