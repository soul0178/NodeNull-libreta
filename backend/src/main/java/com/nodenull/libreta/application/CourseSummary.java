package com.nodenull.libreta.application;

import com.nodenull.libreta.domain.model.GoalStatus;
import com.nodenull.libreta.domain.model.PassResult;
import java.util.Optional;

/** Resumen calculado de un curso: acumulado, peso pendiente, proyección, estados y tendencia. */
public record CourseSummary(
        double accumulated,
        double remainingWeight,
        Optional<Double> projection,
        PassResult pass,
        GoalStatus goal,
        double trendSlope) {
}
