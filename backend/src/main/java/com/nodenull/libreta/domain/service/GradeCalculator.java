package com.nodenull.libreta.domain.service;

import com.nodenull.libreta.domain.model.Course;
import com.nodenull.libreta.domain.model.EvaluationSlot;
import com.nodenull.libreta.domain.model.Substitute;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de dominio: acumulado, peso restante, proyección y tendencia de un curso.
 * Port de js/calc.js (v0.1). Sin dependencias de frameworks.
 */
public class GradeCalculator {

    /** Nota mínima aprobatoria (RN-03). */
    public static final double PASS_GRADE = 10.5;
    /** Por debajo de este peso se considera que no queda nada pendiente. */
    public static final double PENDING_EPSILON = 0.0001;

    private static final double MIN = Course.MIN_GRADE;
    private static final double MAX = Course.MAX_GRADE;
    private static final double EPSILON = 1e-9;

    /** Notas efectivas: las ingresadas, con el sustitutorio reemplazando su evaluación objetivo. */
    public Double[] effectiveGrades(Course course) {
        Double[] effective = new Double[EvaluationSlot.COUNT];
        for (EvaluationSlot slot : EvaluationSlot.values()) {
            Integer grade = course.gradeAt(slot);
            effective[slot.index()] = grade == null ? null : grade.doubleValue();
        }
        Optional<Substitute> substitute = course.substitute();
        substitute.ifPresent(s -> effective[s.target().index()] = (double) s.value());
        return effective;
    }

    public List<EvaluationSlot> enteredSlots(Course course) {
        Double[] effective = effectiveGrades(course);
        List<EvaluationSlot> entered = new ArrayList<>();
        for (EvaluationSlot slot : EvaluationSlot.values()) {
            if (effective[slot.index()] != null) {
                entered.add(slot);
            }
        }
        return entered;
    }

    public List<EvaluationSlot> missingSlots(Course course) {
        List<EvaluationSlot> entered = enteredSlots(course);
        List<EvaluationSlot> missing = new ArrayList<>();
        for (EvaluationSlot slot : EvaluationSlot.values()) {
            if (!entered.contains(slot)) {
                missing.add(slot);
            }
        }
        return missing;
    }

    /** Suma ponderada de lo ingresado. */
    public double accumulated(Course course) {
        Double[] effective = effectiveGrades(course);
        double sum = 0;
        for (EvaluationSlot slot : enteredSlots(course)) {
            sum += effective[slot.index()] * course.weightAt(slot);
        }
        return sum;
    }

    /** Peso (fracción) de las evaluaciones aún sin nota. */
    public double remainingWeight(Course course) {
        double weight = 0;
        for (EvaluationSlot slot : missingSlots(course)) {
            weight += course.weightAt(slot);
        }
        return weight;
    }

    /**
     * Nota final proyectada: lo ingresado más una recta de tendencia por unidad para lo faltante,
     * acotada a [0, 20]. Vacío si no hay ninguna nota.
     */
    public Optional<Double> projection(Course course) {
        Optional<LinearFit> fit = fit(course);
        if (fit.isEmpty()) {
            return Optional.empty();
        }
        Double[] full = effectiveGrades(course);
        for (EvaluationSlot slot : missingSlots(course)) {
            full[slot.index()] = clip(fit.get().predict(slot.unit()));
        }
        double sum = 0;
        for (EvaluationSlot slot : EvaluationSlot.values()) {
            sum += full[slot.index()] * course.weightAt(slot);
        }
        return Optional.of(sum);
    }

    /** Pendiente de la tendencia entre unidades; 0 con menos de dos notas. */
    public double trendSlope(Course course) {
        if (enteredSlots(course).size() < 2) {
            return 0;
        }
        return fit(course).map(LinearFit::slope).orElse(0.0);
    }

    /**
     * Nota final del curso: el acumulado si ya no queda peso pendiente; si no, la proyección.
     * Vacío si falta peso y no hay ninguna nota para proyectar.
     */
    public Optional<Double> finalValue(Course course) {
        if (remainingWeight(course) <= PENDING_EPSILON) {
            return Optional.of(accumulated(course));
        }
        return projection(course);
    }

    /** Redondea al entero más cercano (mitad hacia arriba), corrigiendo ruido de punto flotante. */
    public int roundGrade(double value) {
        if (Double.isNaN(value)) {
            return 0;
        }
        double cleaned = new BigDecimal(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
        return (int) Math.floor(cleaned + 0.5);
    }

    // ---------- internos ----------

    private Optional<LinearFit> fit(Course course) {
        List<EvaluationSlot> entered = enteredSlots(course);
        if (entered.isEmpty()) {
            return Optional.empty();
        }
        Double[] effective = effectiveGrades(course);
        List<Point> points = new ArrayList<>();
        for (EvaluationSlot slot : entered) {
            double weight = course.weightAt(slot);
            if (weight > 0) {
                points.add(new Point(slot.unit(), effective[slot.index()], weight));
            }
        }
        if (points.isEmpty()) { // respaldo: peso igual si todo lo ingresado pesa 0 %
            for (EvaluationSlot slot : entered) {
                points.add(new Point(slot.unit(), effective[slot.index()], 1));
            }
        }
        return Optional.of(LinearFit.of(points));
    }

    private static double clip(double value) {
        return Math.max(MIN, Math.min(MAX, value));
    }

    private record Point(double x, double y, double weight) {
    }

    /** Regresión lineal ponderada, centrada en la media ponderada para ser estable con pesos decimales. */
    private record LinearFit(double slope, double intercept) {

        static LinearFit of(List<Point> points) {
            double totalWeight = points.stream().mapToDouble(Point::weight).sum();
            double xMean = points.stream().mapToDouble(p -> p.weight() * p.x()).sum() / totalWeight;
            double yMean = points.stream().mapToDouble(p -> p.weight() * p.y()).sum() / totalWeight;
            double sxx = 0;
            double sxy = 0;
            for (Point p : points) {
                sxx += p.weight() * (p.x() - xMean) * (p.x() - xMean);
                sxy += p.weight() * (p.x() - xMean) * (p.y() - yMean);
            }
            if (sxx < EPSILON) { // todos en la misma unidad: línea plana en el promedio
                return new LinearFit(0, yMean);
            }
            double slope = sxy / sxx;
            return new LinearFit(slope, yMean - slope * xMean);
        }

        double predict(double x) {
            return slope * x + intercept;
        }
    }
}
