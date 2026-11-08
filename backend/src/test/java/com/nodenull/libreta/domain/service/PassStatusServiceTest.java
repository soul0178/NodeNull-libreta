package com.nodenull.libreta.domain.service;

import static com.nodenull.libreta.domain.model.CourseFixtures.courseWithGrades;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.nodenull.libreta.domain.model.PassResult;
import com.nodenull.libreta.domain.model.PassStatus;
import org.junit.jupiter.api.Test;

class PassStatusServiceTest {

    private final PassStatusService service = new PassStatusService(new GradeCalculator());

    @Test
    void cursoCompletoConPromedioSuficiente_aprobado() {
        assertEquals(PassStatus.APROBADO, service.evaluate(courseWithGrades(12, 12, 12, 12, 12, 12)).status());
    }

    @Test
    void cursoCompletoConPromedioInsuficiente_desaprobado() {
        assertEquals(PassStatus.DESAPROBADO, service.evaluate(courseWithGrades(9, 9, 9, 9, 9, 9)).status());
    }

    @Test
    void notaLimite105_aprueba() {
        // Promedio exacto de 10,5: 0.15*21*2 + 0.20*21 = 6,3 + 4,2
        var course = courseWithGrades(10, 11, 10, 11, 10, 11);
        assertEquals(PassStatus.APROBADO, service.evaluate(course).status());
    }

    @Test
    void siNiConVeinteEnLoRestanteSeLlegaA105_noPodrasAprobar() {
        assertEquals(PassStatus.NO_PODRAS_APROBAR,
                service.evaluate(courseWithGrades(0, 0, 0, 0, null, null)).status());
    }

    @Test
    void siYaSeAcumuloElMinimoAunqueFaltePeso_aprobacionAsegurada() {
        assertEquals(PassStatus.APROBACION_ASEGURADA,
                service.evaluate(courseWithGrades(20, 20, 20, 20, null, null)).status());
    }

    @Test
    void enCurso_indicaLaNotaMinimaPorEvaluacionPendiente() {
        PassResult result = service.evaluate(courseWithGrades(10, 10, null, null, null, null));
        assertEquals(PassStatus.EN_CURSO, result.status());
        // ceil((10.5 - 3.0) / 0.7) = 11
        assertEquals(Integer.valueOf(11), result.neededPerPendingEvaluation());
    }

    @Test
    void estadosSinNotaNecesaria_noInformanNotaPorEvaluacion() {
        assertNull(service.evaluate(courseWithGrades(12, 12, 12, 12, 12, 12)).neededPerPendingEvaluation());
    }
}
