package com.nodenull.libreta.domain.repository;

import com.nodenull.libreta.domain.model.Semester;
import java.util.List;
import java.util.Optional;

/** Puerto de salida del dominio: persistencia de semestres cerrados. */
public interface SemesterRepository {

    Semester save(Semester semester);

    Optional<Semester> findById(String id);

    /** Semestres cerrados, del más reciente al más antiguo. */
    List<Semester> findAllNewestFirst();

    long count();

    void deleteById(String id);
}
