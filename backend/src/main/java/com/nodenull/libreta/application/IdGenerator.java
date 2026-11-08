package com.nodenull.libreta.application;

/** Puerto para generar identificadores; permite sustituirlo por un doble en las pruebas. */
public interface IdGenerator {

    String next();
}
