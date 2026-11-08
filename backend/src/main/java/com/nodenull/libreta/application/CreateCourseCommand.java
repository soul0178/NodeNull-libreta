package com.nodenull.libreta.application;

/** Datos para crear un curso. Los pesos son fracciones que suman 1 (p. ej. 0.15). */
public record CreateCourseCommand(String name, String careerId, int credits, double[] weights) {
}
