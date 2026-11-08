package com.nodenull.libreta.infrastructure.config;

import com.nodenull.libreta.domain.service.GoalStatusService;
import com.nodenull.libreta.domain.service.GradeCalculator;
import com.nodenull.libreta.domain.service.PassStatusService;
import com.nodenull.libreta.domain.service.SemesterService;
import com.nodenull.libreta.domain.service.SubstituteService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registra como beans los servicios de dominio, que no conocen a Spring. */
@Configuration
public class DomainConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public GradeCalculator gradeCalculator() {
        return new GradeCalculator();
    }

    @Bean
    public PassStatusService passStatusService(GradeCalculator calculator) {
        return new PassStatusService(calculator);
    }

    @Bean
    public GoalStatusService goalStatusService(GradeCalculator calculator) {
        return new GoalStatusService(calculator);
    }

    @Bean
    public SubstituteService substituteService(GradeCalculator calculator) {
        return new SubstituteService(calculator);
    }

    @Bean
    public SemesterService semesterService(GradeCalculator calculator, Clock clock) {
        return new SemesterService(calculator, clock);
    }
}
