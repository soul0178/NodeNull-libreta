package com.nodenull.libreta.domain.service;

import static com.nodenull.libreta.domain.model.CourseFixtures.course;
import static com.nodenull.libreta.domain.model.CourseFixtures.courseWithGrades;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nodenull.libreta.domain.model.GoalStatus;
import org.junit.jupiter.api.Test;

class GoalStatusServiceTest {

    private final GoalStatusService service = new GoalStatusService(new GradeCalculator());

    @Test
    void cursoCompletoQueSuperaLaMeta_metaAlcanzada() {
        var course = courseWithGrades(12, 12, 12, 12, 12, 12);
        course.changeGoal(11);
        assertEquals(GoalStatus.META_ALCANZADA, service.evaluate(course));
    }

    @Test
    void cursoCompletoQueSoloLlegaPorRedondeo_metaAlcanzadaPorRedondeo() {
        var course = courseWithGrades(11, 11, 11, 11, 10, 10); // acumulado 10,6
        course.changeGoal(11);
        assertEquals(GoalStatus.META_ALCANZADA_POR_REDONDEO, service.evaluate(course));
    }

    @Test
    void cursoCompletoQueNoLlegaNiRedondeando_metaNoAlcanzada() {
        var course = courseWithGrades(8, 8, 8, 8, 8, 8);
        course.changeGoal(11);
        assertEquals(GoalStatus.META_NO_ALCANZADA, service.evaluate(course));
    }

    @Test
    void sinNingunaNota_sinDatos() {
        assertEquals(GoalStatus.SIN_DATOS, service.evaluate(course()));
    }

    @Test
    void proyeccionQueAlcanzaLaMeta_vasBien() {
        var course = courseWithGrades(18, null, null, null, null, null);
        course.changeGoal(11);
        assertEquals(GoalStatus.VAS_BIEN, service.evaluate(course));
    }

    @Test
    void proyeccionQueSoloAlcanzaRedondeando_metaAjustada() {
        // C1=12 y P1=11 (mismo peso): la proyección es 11,5, que no llega a 12 pero redondea a 12
        var course = courseWithGrades(12, 11, null, null, null, null);
        course.changeGoal(12);
        assertEquals(GoalStatus.META_AJUSTADA, service.evaluate(course));
    }

    @Test
    void proyeccionLejosDeLaMeta_enRiesgo() {
        var course = courseWithGrades(5, null, null, null, null, null);
        course.changeGoal(11);
        assertEquals(GoalStatus.EN_RIESGO, service.evaluate(course));
    }
}
