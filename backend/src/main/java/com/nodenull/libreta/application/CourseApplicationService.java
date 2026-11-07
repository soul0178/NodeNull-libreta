package com.nodenull.libreta.application;

import com.nodenull.libreta.domain.model.Course;
import com.nodenull.libreta.domain.model.EvaluationSlot;
import com.nodenull.libreta.domain.model.SubstituteOutcome;
import com.nodenull.libreta.domain.repository.CourseRepository;
import com.nodenull.libreta.domain.service.GoalStatusService;
import com.nodenull.libreta.domain.service.GradeCalculator;
import com.nodenull.libreta.domain.service.PassStatusService;
import com.nodenull.libreta.domain.service.SubstituteService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso sobre cursos: orquesta el repositorio y los servicios de dominio. */
@Service
@Transactional
public class CourseApplicationService {

    private final CourseRepository repository;
    private final IdGenerator ids;
    private final GradeCalculator calculator;
    private final PassStatusService passStatus;
    private final GoalStatusService goalStatus;
    private final SubstituteService substitute;

    public CourseApplicationService(CourseRepository repository, IdGenerator ids, GradeCalculator calculator,
                                    PassStatusService passStatus, GoalStatusService goalStatus,
                                    SubstituteService substitute) {
        this.repository = repository;
        this.ids = ids;
        this.calculator = calculator;
        this.passStatus = passStatus;
        this.goalStatus = goalStatus;
        this.substitute = substitute;
    }

    public Course create(CreateCourseCommand command) {
        Course course = Course.create(ids.next(), command.name(), command.careerId(),
                command.credits(), command.weights());
        repository.save(course);
        return course;
    }

    @Transactional(readOnly = true)
    public Course get(String id) {
        return repository.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Course> list() {
        return repository.findAll();
    }

    public Course recordGrade(String id, EvaluationSlot slot, Integer grade) {
        Course course = get(id);
        course.setGrade(slot, grade);
        repository.save(course);
        return course;
    }

    public Course changeWeights(String id, double[] weights) {
        Course course = get(id);
        course.changeWeights(weights);
        repository.save(course);
        return course;
    }

    public Course changeGoal(String id, int goal) {
        Course course = get(id);
        course.changeGoal(goal);
        repository.save(course);
        return course;
    }

    public Course archive(String id) {
        Course course = get(id);
        course.archive();
        repository.save(course);
        return course;
    }

    public Course unarchive(String id) {
        Course course = get(id);
        course.unarchive();
        repository.save(course);
        return course;
    }

    public SubstituteOutcome applySubstitute(String id, int value) {
        Course course = get(id);
        SubstituteOutcome outcome = substitute.apply(course, value);
        if (outcome == SubstituteOutcome.APPLIED) {
            repository.save(course);
        }
        return outcome;
    }

    public Course removeSubstitute(String id) {
        Course course = get(id);
        substitute.remove(course);
        repository.save(course);
        return course;
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new CourseNotFoundException(id);
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public CourseSummary summarize(String id) {
        Course course = get(id);
        return new CourseSummary(
                calculator.accumulated(course),
                calculator.remainingWeight(course),
                calculator.projection(course),
                passStatus.evaluate(course),
                goalStatus.evaluate(course),
                calculator.trendSlope(course));
    }
}
