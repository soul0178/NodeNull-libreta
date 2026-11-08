package com.nodenull.libreta.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Fila de un curso activo. */
@Entity
@Table(name = "courses")
public class CourseJpaEntity extends CourseColumns {

    @Id
    private String id;

    @Column(name = "created_at", nullable = false)
    private long createdAt;

    protected CourseJpaEntity() {
        // requerido por JPA
    }

    public CourseJpaEntity(String id) {
        this.id = id;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
