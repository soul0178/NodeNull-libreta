package com.nodenull.libreta.domain.model;

import java.util.Objects;

/** Carrera universitaria a la que pertenecen los cursos. Entidad identificada por {@code id}. */
public final class Career {

    private final String id;
    private String name;

    public Career(String id, String name) {
        this.id = requireText(id, "El id de la carrera es obligatorio");
        this.name = requireText(name, "El nombre de la carrera es obligatorio");
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void rename(String newName) {
        this.name = requireText(newName, "El nombre de la carrera es obligatorio");
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Career career && id.equals(career.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
