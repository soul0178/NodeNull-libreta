package com.nodenull.libreta.domain.service;

import com.nodenull.libreta.domain.model.Course;
import com.nodenull.libreta.domain.model.EvaluationSlot;
import com.nodenull.libreta.domain.model.Substitute;
import com.nodenull.libreta.domain.model.SubstituteOutcome;

/**
 * Servicio de dominio: aplica el examen sustitutorio, que reemplaza el parcial más bajo
 * (de P1 y P2) si lo supera (RN-06). Port de applySustitutorio de calc.js.
 */
public class SubstituteService {

    private final GradeCalculator calculator;

    public SubstituteService(GradeCalculator calculator) {
        this.calculator = calculator;
    }

    public SubstituteOutcome apply(Course course, int value) {
        Double[] effective = calculator.effectiveGrades(course);
        Double midterm1 = effective[EvaluationSlot.PARCIAL_1.index()];
        Double midterm2 = effective[EvaluationSlot.PARCIAL_2.index()];
        if (midterm1 == null && midterm2 == null) {
            return SubstituteOutcome.NO_MIDTERM_ENTERED;
        }
        EvaluationSlot target = chooseTarget(midterm1, midterm2);
        Double currentLowest = effective[target.index()];
        if (currentLowest != null && value <= currentLowest) {
            return SubstituteOutcome.NOT_HIGHER_THAN_LOWEST;
        }
        course.setSubstitute(new Substitute(value, target, course.gradeAt(target)));
        return SubstituteOutcome.APPLIED;
    }

    public void remove(Course course) {
        course.clearSubstitute();
    }

    private static EvaluationSlot chooseTarget(Double midterm1, Double midterm2) {
        if (midterm1 == null) {
            return EvaluationSlot.PARCIAL_2;
        }
        if (midterm2 == null) {
            return EvaluationSlot.PARCIAL_1;
        }
        return midterm1 <= midterm2 ? EvaluationSlot.PARCIAL_1 : EvaluationSlot.PARCIAL_2;
    }
}
