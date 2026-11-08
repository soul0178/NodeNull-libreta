package com.nodenull.libreta.application;

/** No existe un curso con el id indicado. */
public class CourseNotFoundException extends RuntimeException {

    public CourseNotFoundException(String id) {
        super("No existe el curso con id " + id);
    }
}
