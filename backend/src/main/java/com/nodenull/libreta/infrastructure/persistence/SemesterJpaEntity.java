package com.nodenull.libreta.infrastructure.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/** Fila de un semestre cerrado con sus cursos. */
@Entity
@Table(name = "semesters")
public class SemesterJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String label;

    @Column(name = "closed_at", nullable = false)
    private long closedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "semester_id", nullable = false)
    private List<SemesterCourseJpaEntity> courses = new ArrayList<>();

    protected SemesterJpaEntity() {
        // requerido por JPA
    }

    public SemesterJpaEntity(String id, String label, long closedAt) {
        this.id = id;
        this.label = label;
        this.closedAt = closedAt;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public long getClosedAt() {
        return closedAt;
    }

    public List<SemesterCourseJpaEntity> getCourses() {
        return courses;
    }
}
