package com.nodenull.libreta.domain.repository;

import com.nodenull.libreta.domain.model.Course;
import java.util.List;
import java.util.Optional;

/** Puerto de salida del dominio: persistencia de cursos activos. */
public interface CourseRepository {

    /** Crea el curso o actualiza el existente con el mismo id. */
    Course save(Course course);

    Optional<Course> findById(String id);

    /** Todos los cursos activos, en orden de creación. */
    List<Course> findAll();

    List<Course> findByCareerId(String careerId);

    boolean existsById(String id);

    void deleteById(String id);
}
