package com.nodenull.libreta.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data de semestres cerrados (detalle de infraestructura). */
public interface SemesterJpaRepository extends JpaRepository<SemesterJpaEntity, String> {

    List<SemesterJpaEntity> findAllByOrderByClosedAtDesc();
}
