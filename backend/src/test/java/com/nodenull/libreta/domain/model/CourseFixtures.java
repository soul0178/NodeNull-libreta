package com.nodenull.libreta.domain.model;

/** Utilidades de prueba para construir cursos con los pesos por defecto (15/15/15/15/20/20). */
public final class CourseFixtures {

    public static final double[] DEFAULT_WEIGHTS = {0.15, 0.15, 0.15, 0.15, 0.20, 0.20};

    private CourseFixtures() {
    }

    public static Course course() {
        return Course.create("c-1", "Cálculo III", "career-1", 4, DEFAULT_WEIGHTS.clone());
    }

    public static Course courseWithWeights(double... weights) {
        return Course.create("c-1", "Cálculo III", "career-1", 4, weights);
    }

    /** Crea un curso con pesos por defecto y las notas dadas (null = sin ingresar). */
    public static Course courseWithGrades(Integer... grades) {
        Course course = course();
        applyGrades(course, grades);
        return course;
    }

    public static void applyGrades(Course course, Integer... grades) {
        for (int i = 0; i < grades.length; i++) {
            course.setGrade(EvaluationSlot.values()[i], grades[i]);
        }
    }
}
