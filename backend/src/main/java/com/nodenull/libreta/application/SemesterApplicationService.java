package com.nodenull.libreta.application;

import com.nodenull.libreta.domain.model.Course;
import com.nodenull.libreta.domain.model.Semester;
import com.nodenull.libreta.domain.repository.CourseRepository;
import com.nodenull.libreta.domain.repository.SemesterRepository;
import com.nodenull.libreta.domain.service.SemesterService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso de semestres: cerrar el semestre actual, reabrir uno cerrado y listarlos. */
@Service
@Transactional
public class SemesterApplicationService {

    private final CourseRepository courses;
    private final SemesterRepository semesters;
    private final IdGenerator ids;
    private final SemesterService semesterService;

    public SemesterApplicationService(CourseRepository courses, SemesterRepository semesters,
                                      IdGenerator ids, SemesterService semesterService) {
        this.courses = courses;
        this.semesters = semesters;
        this.ids = ids;
        this.semesterService = semesterService;
    }

    /** Cierra el semestre actual: guarda un snapshot de los cursos y vacía la lista activa. */
    public Semester close(String label) {
        List<Course> active = courses.findAll();
        int alreadyClosed = (int) semesters.count();
        String id = ids.next();
        Semester semester = semesterService.close(id, label, active, alreadyClosed);
        semesters.save(semester);
        active.forEach(course -> courses.deleteById(course.id()));
        return semester;
    }

    /** Reabre un semestre: sus cursos vuelven a la lista activa y el semestre se elimina. */
    public List<Course> reopen(String semesterId) {
        Semester semester = semesters.findById(semesterId)
                .orElseThrow(() -> new SemesterNotFoundException(semesterId));
        List<Course> restored = semester.reopen();
        semesters.deleteById(semesterId);
        restored.forEach(courses::save);
        return restored;
    }

    @Transactional(readOnly = true)
    public List<Semester> list() {
        return semesters.findAllNewestFirst();
    }
}
