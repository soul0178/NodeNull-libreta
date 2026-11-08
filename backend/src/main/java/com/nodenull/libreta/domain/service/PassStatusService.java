package com.nodenull.libreta.domain.service;

import com.nodenull.libreta.domain.model.Course;
import com.nodenull.libreta.domain.model.PassResult;
import com.nodenull.libreta.domain.model.PassStatus;

/** Servicio de dominio: determina si el estudiante aprueba un curso (port de passStatus de calc.js). */
public class PassStatusService {

    private final GradeCalculator calculator;

    public PassStatusService(GradeCalculator calculator) {
        this.calculator = calculator;
    }

    public PassResult evaluate(Course course) {
        double accumulated = calculator.accumulated(course);
        double remaining = calculator.remainingWeight(course);

        if (remaining <= GradeCalculator.PENDING_EPSILON) {
            return PassResult.of(accumulated >= GradeCalculator.PASS_GRADE
                    ? PassStatus.APROBADO : PassStatus.DESAPROBADO);
        }
        double maxPossible = accumulated + remaining * Course.MAX_GRADE;
        if (maxPossible < GradeCalculator.PASS_GRADE) {
            return PassResult.of(PassStatus.NO_PODRAS_APROBAR);
        }
        double neededRaw = (GradeCalculator.PASS_GRADE - accumulated) / remaining;
        if (neededRaw <= 0) {
            return PassResult.of(PassStatus.APROBACION_ASEGURADA);
        }
        if (neededRaw > Course.MAX_GRADE) {
            return PassResult.of(PassStatus.MUY_DIFICIL_APROBAR);
        }
        int needed = Math.min(Course.MAX_GRADE, (int) Math.ceil(neededRaw - 1e-9));
        return new PassResult(PassStatus.EN_CURSO, needed);
    }
}
