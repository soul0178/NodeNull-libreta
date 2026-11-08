package com.nodenull.libreta.infrastructure.persistence;

import static com.nodenull.libreta.domain.model.CourseFixtures.courseNamed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nodenull.libreta.domain.model.EvaluationSlot;
import com.nodenull.libreta.domain.model.Semester;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(SemesterRepositoryAdapter.class)
class SemesterRepositoryAdapterTest {

    @Autowired
    private SemesterRepositoryAdapter repository;

    private Semester semester(String id, String label, long closedAt) {
        return new Semester(id, label, closedAt, List.of(
                courseNamed("c-1", "Cálculo", 12, 12, 12, 12, 12, 12),
                courseNamed("c-2", "Física", 15, 14, 13, 12, 11, 10)));
    }

    @Test
    void guardarYBuscar_conservaLosCursosDelSnapshot() {
        repository.save(semester("s-1", "2026-A", 1000L));

        Semester found = repository.findById("s-1").orElseThrow();

        assertEquals("2026-A", found.label());
        assertEquals(1000L, found.closedAt());
        assertEquals(2, found.courses().size());
        assertEquals(15, found.courses().get(1).gradeAt(EvaluationSlot.CONTINUA_1));
    }

    @Test
    void findAllNewestFirst_ordenaDelMasRecienteAlMasAntiguo() {
        repository.save(semester("s-1", "Primero", 1000L));
        repository.save(semester("s-2", "Segundo", 3000L));
        repository.save(semester("s-3", "Tercero", 2000L));

        List<String> labels = repository.findAllNewestFirst().stream().map(Semester::label).toList();
        assertEquals(List.of("Segundo", "Tercero", "Primero"), labels);
    }

    @Test
    void count_cuentaLosSemestresGuardados() {
        assertEquals(0, repository.count());
        repository.save(semester("s-1", "2026-A", 1000L));
        assertEquals(1, repository.count());
    }

    @Test
    void eliminar_quitaElSemestreYSusCursos() {
        repository.save(semester("s-1", "2026-A", 1000L));
        repository.deleteById("s-1");
        assertFalse(repository.findById("s-1").isPresent());
        assertTrue(repository.findAllNewestFirst().isEmpty());
    }

    @Test
    void elMismoCursoPuedeEstarEnVariosSemestres() {
        repository.save(semester("s-1", "2026-A", 1000L));
        repository.save(semester("s-2", "2026-B", 2000L));
        assertEquals(2, repository.findAllNewestFirst().size());
    }
}
