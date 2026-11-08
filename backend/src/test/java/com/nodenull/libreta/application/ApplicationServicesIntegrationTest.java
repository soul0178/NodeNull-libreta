package com.nodenull.libreta.application;

import static com.nodenull.libreta.domain.model.CourseFixtures.DEFAULT_WEIGHTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nodenull.libreta.domain.model.Course;
import com.nodenull.libreta.domain.model.EvaluationSlot;
import com.nodenull.libreta.domain.model.PassStatus;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prueba de integración: servicios de aplicación + dominio + repositorios JPA reales sobre H2 en memoria.
 * Cada prueba se revierte al terminar (@Transactional).
 */
@SpringBootTest
@Transactional
class ApplicationServicesIntegrationTest {

    @Autowired
    private CourseApplicationService courses;

    @Autowired
    private SemesterApplicationService semesters;

    private Course createWithAllGrades(String name, int grade) {
        Course course = courses.create(new CreateCourseCommand(name, "k-1", 4, DEFAULT_WEIGHTS.clone()));
        for (EvaluationSlot slot : EvaluationSlot.values()) {
            courses.recordGrade(course.id(), slot, grade);
        }
        return course;
    }

    @Test
    void crearRegistrarNotasYResumir_persisteYCalculaEnLaBaseReal() {
        Course course = courses.create(new CreateCourseCommand("Cálculo III", "k-1", 4, DEFAULT_WEIGHTS.clone()));
        courses.recordGrade(course.id(), EvaluationSlot.CONTINUA_1, 15);
        courses.recordGrade(course.id(), EvaluationSlot.PARCIAL_1, 10);

        CourseSummary summary = courses.summarize(course.id());

        assertEquals(3.75, summary.accumulated(), 1e-6);
        assertEquals(PassStatus.EN_CURSO, summary.pass().status());
        assertEquals(15, courses.get(course.id()).gradeAt(EvaluationSlot.CONTINUA_1));
    }

    @Test
    void cerrarSemestre_guardaElSnapshotYLimpiaLosCursosActivos() {
        createWithAllGrades("Cálculo", 12);
        createWithAllGrades("Física", 14);

        semesters.close("2026-A");

        assertTrue(courses.list().isEmpty());
        assertEquals(1, semesters.list().size());
        assertEquals(2, semesters.list().get(0).courses().size());
    }

    @Test
    void reabrirSemestre_devuelveLosCursosALaListaActiva() {
        createWithAllGrades("Cálculo", 12);
        createWithAllGrades("Física", 14);
        String semesterId = semesters.close("2026-A").id();

        List<Course> restored = semesters.reopen(semesterId);

        assertEquals(2, restored.size());
        assertEquals(2, courses.list().size());
        assertTrue(semesters.list().isEmpty());
    }

    @Test
    void cerrarConCursoIncompleto_noCambiaNada() {
        createWithAllGrades("Cálculo", 12);
        courses.create(new CreateCourseCommand("Física", "k-1", 4, DEFAULT_WEIGHTS.clone()));

        try {
            semesters.close("2026-A");
        } catch (IllegalStateException expected) {
            // regla RN-07
        }

        assertEquals(2, courses.list().size());
        assertTrue(semesters.list().isEmpty());
    }
}
