package com.nodenull.libreta.domain.service;

import com.nodenull.libreta.domain.model.Course;
import com.nodenull.libreta.domain.model.GoalStatus;
import java.util.Optional;

/** Servicio de dominio: compara el avance del curso con la meta personal (port de metaStatus de calc.js). */
public class GoalStatusService {

    private final GradeCalculator calculator;

    public GoalStatusService(GradeCalculator calculator) {
        this.calculator = calculator;
    }

    public GoalStatus evaluate(Course course) {
        double accumulated = calculator.accumulated(course);
        double remaining = calculator.remainingWeight(course);
        int goal = course.goal();

        if (remaining <= GradeCalculator.PENDING_EPSILON) {
            if (accumulated >= goal) {
                return GoalStatus.META_ALCANZADA;
            }
            return calculator.roundGrade(accumulated) >= goal
                    ? GoalStatus.META_ALCANZADA_POR_REDONDEO : GoalStatus.META_NO_ALCANZADA;
        }
        Optional<Double> projection = calculator.projection(course);
        if (projection.isEmpty()) {
            return GoalStatus.SIN_DATOS;
        }
        double projected = projection.get();
        if (projected >= goal) {
            return GoalStatus.VAS_BIEN;
        }
        return calculator.roundGrade(projected) >= goal ? GoalStatus.META_AJUSTADA : GoalStatus.EN_RIESGO;
    }
}
