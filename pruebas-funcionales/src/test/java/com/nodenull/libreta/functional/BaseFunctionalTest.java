package com.nodenull.libreta.functional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;

/** Cada prueba usa un navegador nuevo (perfil limpio, sin datos previos) abierto en Libreta. */
abstract class BaseFunctionalTest {

    protected static final String[] DEFAULT_WEIGHTS = {"15", "15", "15", "15", "20", "20"};

    protected WebDriver driver;
    protected LibretaPage page;

    @BeforeEach
    void openApplication() {
        driver = DriverFactory.create();
        page = new LibretaPage(driver).open();
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    /** Crea un curso válido y espera a ver su detalle. */
    protected void createDefaultCourse() {
        page.createCourse("Cálculo III", "4", DEFAULT_WEIGHTS).waitForCourseDetail();
    }
}
