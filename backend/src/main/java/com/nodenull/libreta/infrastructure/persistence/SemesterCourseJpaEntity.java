package com.nodenull.libreta.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Fila de un curso dentro de un semestre cerrado (snapshot). */
@Entity
@Table(name = "semester_courses")
public class SemesterCourseJpaEntity extends CourseColumns {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Column(name = "course_id", nullable = false)
    private String courseId;

    protected SemesterCourseJpaEntity() {
        // requerido por JPA
    }

    public SemesterCourseJpaEntity(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseId() {
        return courseId;
    }
}
