package com.nodenull.libreta.domain.service;

import com.nodenull.libreta.domain.model.Course;
import com.nodenull.libreta.domain.model.Semester;
import java.time.Clock;
import java.util.List;

/** Servicio de dominio: cierre de semestre (RN-07, RN-09). Port de closeSemester de semesters-courses.js. */
public class SemesterService {

    private final GradeCalculator calculator;
    private final Clock clock;

    public SemesterService(GradeCalculator calculator, Clock clock) {
        this.calculator = calculator;
        this.clock = clock;
    }

    /** Cursos no archivados que todavía tienen evaluaciones sin nota. */
    public List<Course> incompleteCourses(List<Course> activeCourses) {
        return activeCourses.stream()
                .filter(course -> !course.isArchived())
                .filter(course -> !calculator.missingSlots(course).isEmpty())
                .toList();
    }

    /**
     * Cierra el semestre actual.
     *
     * @param closedSemesters cantidad de semestres ya cerrados, para numerar la etiqueta por defecto
     * @throws IllegalStateException si no hay cursos o alguno no archivado tiene notas pendientes
     */
    public Semester close(String id, String label, List<Course> activeCourses, int closedSemesters) {
        if (activeCourses.isEmpty()) {
            throw new IllegalStateException("No hay cursos activos para cerrar el semestre");
        }
        List<Course> pending = incompleteCourses(activeCourses);
        if (!pending.isEmpty()) {
            String names = String.join(", ", pending.stream().map(Course::name).toList());
            throw new IllegalStateException("No puedes cerrar el semestre: hay cursos con notas pendientes: " + names);
        }
        String finalLabel = (label == null || label.isBlank()) ? "Semestre " + (closedSemesters + 1) : label;
        return new Semester(id, finalLabel, clock.millis(), activeCourses);
    }
}
