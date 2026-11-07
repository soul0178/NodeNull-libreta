package com.nodenull.libreta.application;

/** No existe un semestre cerrado con el id indicado. */
public class SemesterNotFoundException extends RuntimeException {

    public SemesterNotFoundException(String id) {
        super("No existe el semestre con id " + id);
    }
}
