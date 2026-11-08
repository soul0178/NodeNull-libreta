package com.nodenull.libreta.infrastructure.persistence;

import com.nodenull.libreta.domain.model.Course;
import com.nodenull.libreta.domain.model.Semester;
import com.nodenull.libreta.domain.repository.SemesterRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Adaptador: implementa el puerto {@link SemesterRepository} con Spring Data JPA. */
@Repository
public class SemesterRepositoryAdapter implements SemesterRepository {

    private final SemesterJpaRepository jpa;

    public SemesterRepositoryAdapter(SemesterJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Semester save(Semester semester) {
        SemesterJpaEntity entity = new SemesterJpaEntity(semester.id(), semester.label(), semester.closedAt());
        for (Course course : semester.courses()) {
            SemesterCourseJpaEntity row = new SemesterCourseJpaEntity(course.id());
            CourseMapper.copyToColumns(course, row);
            entity.getCourses().add(row);
        }
        jpa.save(entity);
        return semester;
    }

    @Override
    public Optional<Semester> findById(String id) {
        return jpa.findById(id).map(SemesterRepositoryAdapter::toDomain);
    }

    @Override
    public List<Semester> findAllNewestFirst() {
        return jpa.findAllByOrderByClosedAtDesc().stream().map(SemesterRepositoryAdapter::toDomain).toList();
    }

    @Override
    public long count() {
        return jpa.count();
    }

    @Override
    public void deleteById(String id) {
        jpa.deleteById(id);
    }

    private static Semester toDomain(SemesterJpaEntity entity) {
        List<Course> courses = entity.getCourses().stream()
                .map(row -> CourseMapper.toDomain(row.getCourseId(), row))
                .toList();
        return new Semester(entity.getId(), entity.getLabel(), entity.getClosedAt(), courses);
    }
}
