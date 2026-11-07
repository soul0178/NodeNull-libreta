package com.nodenull.libreta.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * Columnas comunes de un curso, compartidas por la tabla de cursos activos y la de cursos de un semestre.
 * Pesos y notas se guardan como texto separado por comas (6 valores; nota vacía = sin ingresar).
 */
@MappedSuperclass
public abstract class CourseColumns {

    @Column(nullable = false)
    private String name;

    @Column(name = "career_id", nullable = false)
    private String careerId;

    @Column(nullable = false)
    private int credits;

    @Column(nullable = false)
    private int goal;

    @Column(nullable = false)
    private boolean archived;

    @Column(nullable = false, length = 120)
    private String weights;

    @Column(nullable = false, length = 120)
    private String grades;

    @Column(name = "substitute_value")
    private Integer substituteValue;

    @Column(name = "substitute_target")
    private Integer substituteTarget;

    @Column(name = "substitute_original")
    private Integer substituteOriginal;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCareerId() {
        return careerId;
    }

    public void setCareerId(String careerId) {
        this.careerId = careerId;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public String getWeights() {
        return weights;
    }

    public void setWeights(String weights) {
        this.weights = weights;
    }

    public String getGrades() {
        return grades;
    }

    public void setGrades(String grades) {
        this.grades = grades;
    }

    public Integer getSubstituteValue() {
        return substituteValue;
    }

    public void setSubstituteValue(Integer substituteValue) {
        this.substituteValue = substituteValue;
    }

    public Integer getSubstituteTarget() {
        return substituteTarget;
    }

    public void setSubstituteTarget(Integer substituteTarget) {
        this.substituteTarget = substituteTarget;
    }

    public Integer getSubstituteOriginal() {
        return substituteOriginal;
    }

    public void setSubstituteOriginal(Integer substituteOriginal) {
        this.substituteOriginal = substituteOriginal;
    }
}
