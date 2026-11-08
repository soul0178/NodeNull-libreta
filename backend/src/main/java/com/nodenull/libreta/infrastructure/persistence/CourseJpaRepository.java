package com.nodenull.libreta.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data de cursos activos (detalle de infraestructura). */
public interface CourseJpaRepository extends JpaRepository<CourseJpaEntity, String> {

    List<CourseJpaEntity> findAllByOrderByCreatedAtAscIdAsc();

    List<CourseJpaEntity> findByCareerIdOrderByCreatedAtAscIdAsc(String careerId);
}
