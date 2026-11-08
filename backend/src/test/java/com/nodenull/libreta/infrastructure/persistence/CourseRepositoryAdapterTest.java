package com.nodenull.libreta.infrastructure.persistence;

import static com.nodenull.libreta.domain.model.CourseFixtures.courseNamed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nodenull.libreta.domain.model.Course;
import com.nodenull.libreta.domain.model.EvaluationSlot;
import com.nodenull.libreta.domain.model.Substitute;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(CourseRepositoryAdapter.class)
class CourseRepositoryAdapterTest {

    @Autowired
    private CourseRepositoryAdapter repository;

    @Test
    void guardarYBuscar_conservaTodosLosDatos() {
        Course course = courseNamed("c-1", "Cálculo III", 15, null, 12, null, null, 8);
        course.changeGoal(14);
        course.changeCredits(5);

        repository.save(course);
        Course found = repository.findById("c-1").orElseThrow();

        assertEquals("Cálculo III", found.name());
        assertEquals(5, found.credits());
        assertEquals(14, found.goal());
        assertEquals(15, found.gradeAt(EvaluationSlot.CONTINUA_1));
        assertNull(found.gradeAt(EvaluationSlot.PARCIAL_1));
        assertEquals(8, found.gradeAt(EvaluationSlot.PARCIAL_3));
        assertEquals(0.20, found.weightAt(EvaluationSlot.CONTINUA_3), 1e-9);
    }

    @Test
    void guardarCursoConSustitutorio_loRecuperaIgual() {
        Course course = courseNamed("c-1", "Física", null, 10, null, 15, null, null);
        course.setSubstitute(new Substitute(18, EvaluationSlot.PARCIAL_1, 10));

        repository.save(course);

        Substitute substitute = repository.findById("c-1").orElseThrow().substitute().orElseThrow();
        assertEquals(new Substitute(18, EvaluationSlot.PARCIAL_1, 10), substitute);
    }

    @Test
    void guardarCursoSinSustitutorio_noLoInventa() {
        repository.save(courseNamed("c-1", "Física"));
        assertTrue(repository.findById("c-1").orElseThrow().substitute().isEmpty());
    }

    @Test
    void guardarUnCursoExistente_loActualizaSinDuplicar() {
        Course course = courseNamed("c-1", "Cálculo III", 10);
        repository.save(course);

        course.setGrade(EvaluationSlot.PARCIAL_1, 17);
        course.archive();
        repository.save(course);

        assertEquals(1, repository.findAll().size());
        Course found = repository.findById("c-1").orElseThrow();
        assertEquals(17, found.gradeAt(EvaluationSlot.PARCIAL_1));
        assertTrue(found.isArchived());
    }

    @Test
    void buscarIdInexistente_devuelveVacio() {
        assertEquals(Optional.empty(), repository.findById("no-existe"));
    }

    @Test
    void findAll_devuelveLosCursosEnOrdenDeCreacion() {
        repository.save(courseNamed("c-1", "Primero"));
        repository.save(courseNamed("c-2", "Segundo"));
        repository.save(courseNamed("c-3", "Tercero"));

        List<String> names = repository.findAll().stream().map(Course::name).toList();
        assertEquals(List.of("Primero", "Segundo", "Tercero"), names);
    }

    @Test
    void findByCareerId_filtraPorCarrera() {
        Course other = courseNamed("c-2", "Historia");
        other.changeCareer("career-2");
        repository.save(courseNamed("c-1", "Cálculo"));
        repository.save(other);

        List<Course> found = repository.findByCareerId("career-2");
        assertEquals(1, found.size());
        assertEquals("Historia", found.get(0).name());
    }

    @Test
    void eliminar_quitaElCurso() {
        repository.save(courseNamed("c-1", "Cálculo"));
        repository.deleteById("c-1");
        assertFalse(repository.existsById("c-1"));
    }

    @Test
    void existsById_distingueExistentesDeInexistentes() {
        repository.save(courseNamed("c-1", "Cálculo"));
        assertTrue(repository.existsById("c-1"));
        assertFalse(repository.existsById("c-9"));
    }
}
