package com.nodenull.libreta.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Verifica la regla de dependencia de la Arquitectura Limpia/DDD:
 * el dominio no importa Spring, JPA ni las capas externas, y la
 * aplicación no importa infraestructura.
 */
class LayerDependencyTest {

    private static final Path MAIN = Path.of("src/main/java/com/nodenull/libreta");

    @Test
    void domain_noDependeDeFrameworksNiDeOtrasCapas() throws IOException {
        List<String> prohibidos = List.of(
                "org.springframework", "jakarta.persistence",
                "com.nodenull.libreta.application", "com.nodenull.libreta.infrastructure");
        List<String> violaciones = importsEn("domain", prohibidos);
        assertTrue(violaciones.isEmpty(), "El dominio importa algo prohibido: " + violaciones);
    }

    @Test
    void application_noDependeDeInfraestructura() throws IOException {
        List<String> prohibidos = List.of("com.nodenull.libreta.infrastructure");
        List<String> violaciones = importsEn("application", prohibidos);
        assertTrue(violaciones.isEmpty(), "La aplicación importa infraestructura: " + violaciones);
    }

    private List<String> importsEn(String capa, List<String> prohibidos) throws IOException {
        try (Stream<Path> files = Files.walk(MAIN.resolve(capa))) {
            return files.filter(p -> p.toString().endsWith(".java"))
                    .flatMap(p -> lines(p).stream()
                            .filter(l -> l.startsWith("import "))
                            .filter(l -> prohibidos.stream().anyMatch(l::contains))
                            .map(l -> p.getFileName() + ": " + l))
                    .toList();
        }
    }

    private List<String> lines(Path p) {
        try {
            return Files.readAllLines(p);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
