package com.nodenull.libreta.infrastructure.persistence;

import com.nodenull.libreta.domain.model.Course;
import com.nodenull.libreta.domain.repository.CourseRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Adaptador: implementa el puerto {@link CourseRepository} con Spring Data JPA. */
@Repository
public class CourseRepositoryAdapter implements CourseRepository {

    private final CourseJpaRepository jpa;

    public CourseRepositoryAdapter(CourseJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Course save(Course course) {
        CourseJpaEntity entity = jpa.findById(course.id()).orElseGet(() -> new CourseJpaEntity(course.id()));
        CourseMapper.copyToColumns(course, entity);
        jpa.save(entity);
        return course;
    }

    @Override
    public Optional<Course> findById(String id) {
        return jpa.findById(id).map(entity -> CourseMapper.toDomain(entity.getId(), entity));
    }

    @Override
    public List<Course> findAll() {
        return jpa.findAllByOrderByCreatedAtAscIdAsc().stream()
                .map(entity -> CourseMapper.toDomain(entity.getId(), entity))
                .toList();
    }

    @Override
    public List<Course> findByCareerId(String careerId) {
        return jpa.findByCareerIdOrderByCreatedAtAscIdAsc(careerId).stream()
                .map(entity -> CourseMapper.toDomain(entity.getId(), entity))
                .toList();
    }

    @Override
    public boolean existsById(String id) {
        return jpa.existsById(id);
    }

    @Override
    public void deleteById(String id) {
        jpa.deleteById(id);
    }
}
