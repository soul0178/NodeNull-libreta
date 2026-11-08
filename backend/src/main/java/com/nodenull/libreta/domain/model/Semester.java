package com.nodenull.libreta.domain.model;

import java.util.List;

/**
 * Agregado raíz: semestre cerrado, con una copia (snapshot) de sus cursos.
 * Se crea mediante {@code SemesterService.close(...)}, que valida las reglas de cierre.
 */
public final class Semester {

    private final String id;
    private final String label;
    private final long closedAt;
    private final List<Course> courses;

    public Semester(String id, String label, long closedAt, List<Course> courses) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id del semestre es obligatorio");
        }
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("La etiqueta del semestre es obligatoria");
        }
        if (courses == null || courses.isEmpty()) {
            throw new IllegalArgumentException("Un semestre cerrado debe tener al menos un curso");
        }
        this.id = id;
        this.label = label.trim();
        this.closedAt = closedAt;
        this.courses = courses.stream().map(Course::copy).toList();
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public long closedAt() {
        return closedAt;
    }

    /** Cursos del semestre (copias de solo lectura de lo guardado). */
    public List<Course> courses() {
        return courses;
    }

    /** Cursos para volver a la lista activa al reabrir el semestre: copias editables. */
    public List<Course> reopen() {
        return courses.stream().map(Course::copy).toList();
    }
}
