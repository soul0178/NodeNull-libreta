package com.nodenull.libreta.infrastructure.persistence;

import com.nodenull.libreta.domain.model.Course;
import com.nodenull.libreta.domain.model.EvaluationSlot;
import com.nodenull.libreta.domain.model.Substitute;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Convierte entre el curso del dominio y sus columnas de persistencia. */
final class CourseMapper {

    private static final String SEPARATOR = ",";

    private CourseMapper() {
    }

    static void copyToColumns(Course course, CourseColumns target) {
        target.setName(course.name());
        target.setCareerId(course.careerId());
        target.setCredits(course.credits());
        target.setGoal(course.goal());
        target.setArchived(course.isArchived());
        target.setWeights(Arrays.stream(course.weights())
                .mapToObj(Double::toString)
                .collect(Collectors.joining(SEPARATOR)));
        target.setGrades(Arrays.stream(EvaluationSlot.values())
                .map(slot -> course.gradeAt(slot) == null ? "" : course.gradeAt(slot).toString())
                .collect(Collectors.joining(SEPARATOR)));
        course.substitute().ifPresentOrElse(substitute -> {
            target.setSubstituteValue(substitute.value());
            target.setSubstituteTarget(substitute.target().index());
            target.setSubstituteOriginal(substitute.originalValue());
        }, () -> {
            target.setSubstituteValue(null);
            target.setSubstituteTarget(null);
            target.setSubstituteOriginal(null);
        });
    }

    static Course toDomain(String id, CourseColumns columns) {
        double[] weights = Arrays.stream(columns.getWeights().split(SEPARATOR))
                .mapToDouble(Double::parseDouble)
                .toArray();
        Course course = Course.create(id, columns.getName(), columns.getCareerId(), columns.getCredits(), weights);
        String[] grades = columns.getGrades().split(SEPARATOR, -1);
        IntStream.range(0, EvaluationSlot.COUNT).forEach(i -> {
            if (!grades[i].isBlank()) {
                course.setGrade(EvaluationSlot.values()[i], Integer.valueOf(grades[i]));
            }
        });
        course.changeGoal(columns.getGoal());
        if (columns.isArchived()) {
            course.archive();
        }
        if (columns.getSubstituteValue() != null) {
            course.setSubstitute(new Substitute(
                    columns.getSubstituteValue(),
                    EvaluationSlot.values()[columns.getSubstituteTarget()],
                    columns.getSubstituteOriginal()));
        }
        return course;
    }
}
