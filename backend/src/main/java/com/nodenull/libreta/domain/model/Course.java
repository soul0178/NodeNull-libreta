package com.nodenull.libreta.domain.model;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Agregado raíz: un curso con sus seis evaluaciones, pesos, meta y sustitutorio.
 * Protege las invariantes RN-01 (pesos suman 100 %) y RN-02 (notas enteras 0-20).
 */
public final class Course {

    public static final int MIN_GRADE = 0;
    public static final int MAX_GRADE = 20;
    public static final int DEFAULT_GOAL = 11;
    /** Tolerancia para la suma de pesos: 0,5 % (en fracción). */
    public static final double WEIGHT_TOLERANCE = 0.005;

    private final String id;
    private String name;
    private String careerId;
    private int credits;
    private int goal = DEFAULT_GOAL;
    private boolean archived;
    private double[] weights;
    private final Integer[] grades = new Integer[EvaluationSlot.COUNT];
    private Substitute substitute;

    private Course(String id, String name, String careerId, int credits, double[] weights) {
        this.id = requireText(id, "El id del curso es obligatorio");
        this.name = requireText(name, "El nombre del curso es obligatorio");
        this.careerId = requireText(careerId, "El curso debe pertenecer a una carrera");
        this.credits = requireCredits(credits);
        this.weights = validatedWeights(weights);
    }

    /** Fábrica: valida todas las invariantes antes de crear el curso. */
    public static Course create(String id, String name, String careerId, int credits, double[] weights) {
        return new Course(id, name, careerId, credits, weights);
    }

    // ---------- comportamiento ----------

    public void rename(String newName) {
        this.name = requireText(newName, "El nombre del curso es obligatorio");
    }

    public void changeCareer(String newCareerId) {
        this.careerId = requireText(newCareerId, "El curso debe pertenecer a una carrera");
    }

    public void changeCredits(int newCredits) {
        this.credits = requireCredits(newCredits);
    }

    public void changeWeights(double[] newWeights) {
        this.weights = validatedWeights(newWeights);
    }

    /** Registra la nota de una evaluación; {@code null} la borra. */
    public void setGrade(EvaluationSlot slot, Integer grade) {
        Objects.requireNonNull(slot, "La evaluación es obligatoria");
        if (grade != null && (grade < MIN_GRADE || grade > MAX_GRADE)) {
            throw new IllegalArgumentException("La nota debe estar entre 0 y 20");
        }
        grades[slot.index()] = grade;
    }

    public void changeGoal(int newGoal) {
        if (newGoal < MIN_GRADE || newGoal > MAX_GRADE) {
            throw new IllegalArgumentException("La meta debe estar entre 0 y 20");
        }
        this.goal = newGoal;
    }

    public void archive() {
        this.archived = true;
    }

    public void unarchive() {
        this.archived = false;
    }

    public void setSubstitute(Substitute substitute) {
        this.substitute = substitute;
    }

    public void clearSubstitute() {
        this.substitute = null;
    }

    // ---------- consultas ----------

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String careerId() {
        return careerId;
    }

    public int credits() {
        return credits;
    }

    public int goal() {
        return goal;
    }

    public boolean isArchived() {
        return archived;
    }

    public Integer gradeAt(EvaluationSlot slot) {
        return grades[slot.index()];
    }

    public double weightAt(EvaluationSlot slot) {
        return weights[slot.index()];
    }

    /** Copia defensiva de los pesos (fracciones que suman 1). */
    public double[] weights() {
        return weights.clone();
    }

    public Optional<Substitute> substitute() {
        return Optional.ofNullable(substitute);
    }

    // ---------- invariantes ----------

    private static double[] validatedWeights(double[] candidate) {
        if (candidate == null || candidate.length != EvaluationSlot.COUNT) {
            throw new IllegalArgumentException("Se requieren exactamente 6 pesos");
        }
        double sum = 0;
        for (double w : candidate) {
            if (w < 0 || w > 1) {
                throw new IllegalArgumentException("Cada peso debe estar entre 0 y 100 %");
            }
            sum += w;
        }
        if (Math.abs(sum - 1.0) > WEIGHT_TOLERANCE) {
            throw new IllegalArgumentException("Los pesos deben sumar 100 %");
        }
        return Arrays.copyOf(candidate, candidate.length);
    }

    private static int requireCredits(int credits) {
        if (credits < 0) {
            throw new IllegalArgumentException("Los créditos no pueden ser negativos");
        }
        return credits;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Course course && id.equals(course.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
