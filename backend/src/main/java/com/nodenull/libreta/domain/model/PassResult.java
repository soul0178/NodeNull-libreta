package com.nodenull.libreta.domain.model;

/**
 * Resultado de evaluar la aprobación de un curso.
 *
 * @param status                      estado de aprobación
 * @param neededPerPendingEvaluation  nota mínima entera necesaria en cada evaluación pendiente;
 *                                    solo tiene valor en {@link PassStatus#EN_CURSO}
 */
public record PassResult(PassStatus status, Integer neededPerPendingEvaluation) {

    public static PassResult of(PassStatus status) {
        return new PassResult(status, null);
    }
}
