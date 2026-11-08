package com.nodenull.libreta.functional;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object de Libreta: encapsula los selectores y las acciones del usuario sobre la interfaz. */
public class LibretaPage {

    private static final By CREATE_BUTTON = By.cssSelector("[data-act='open-create']");
    private static final By NAME = By.id("new-name");
    private static final By CREDITS = By.id("new-creditos");
    private static final By WEIGHTS = By.cssSelector(".w-input");
    private static final By CONFIRM_CREATE = By.id("confirm-create");
    private static final By ERROR = By.id("create-err");
    private static final By WEIGHT_SUM = By.id("weight-sum");
    private static final By OVERLAY = By.id("overlay");
    private static final By CLOSE_MODAL = By.cssSelector("[data-act='close-modal']");
    private static final By DETAIL_TITLE = By.cssSelector(".detail-header h2");
    private static final By DETAIL_META = By.cssSelector(".detail-header .meta-tag");
    private static final By EMPTY_DASHBOARD = By.cssSelector(".empty h3");
    private static final By EDIT_COURSE = By.cssSelector("[data-act='edit-course']");
    private static final By EDIT_CREDITS = By.id("edit-creditos");
    private static final By CONFIRM_EDIT = By.id("confirm-edit");
    private static final By STAMPS = By.cssSelector(".stamp-circle");
    private static final By RISK_BANNER = By.cssSelector(".risk-banner");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public LibretaPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /** URL de la aplicación: -Dbase.url=... o, por defecto, el index.html del repositorio (file://). */
    public static String baseUrl() {
        String configured = DriverFactory.property("base.url", "");
        if (!configured.isBlank()) {
            return configured;
        }
        return Path.of("..", "index.html").toAbsolutePath().normalize().toUri().toString();
    }

    public LibretaPage open() {
        driver.get(baseUrl());
        wait.until(ExpectedConditions.presenceOfElementLocated(CREATE_BUTTON));
        return this;
    }

    // ---------- crear curso ----------

    public LibretaPage openCreateCourseModal() {
        wait.until(ExpectedConditions.elementToBeClickable(CREATE_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(NAME));
        return this;
    }

    public LibretaPage typeName(String name) {
        type(driver.findElement(NAME), name);
        return this;
    }

    public LibretaPage typeCredits(String credits) {
        type(driver.findElement(CREDITS), credits);
        return this;
    }

    /** Escribe los pesos (en %) en los campos de las evaluaciones, en orden: C1, P1, C2, P2, C3, P3. */
    public LibretaPage typeWeights(String... weights) {
        List<WebElement> inputs = driver.findElements(WEIGHTS);
        for (int i = 0; i < weights.length; i++) {
            type(inputs.get(i), weights[i]);
        }
        return this;
    }

    public LibretaPage confirmCreate() {
        driver.findElement(CONFIRM_CREATE).click();
        return this;
    }

    /** Atajo: abre el formulario, lo llena y confirma. */
    public LibretaPage createCourse(String name, String credits, String... weights) {
        openCreateCourseModal().typeName(name);
        if (credits != null) {
            typeCredits(credits);
        }
        typeWeights(weights);
        return confirmCreate();
    }

    public LibretaPage cancelModal() {
        driver.findElement(CLOSE_MODAL).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(OVERLAY));
        return this;
    }

    /** Espera a que aparezca un mensaje de error; si la app no lo muestra, falla la prueba con un mensaje claro. */
    public String waitForError() {
        try {
            wait.until(d -> !d.findElement(ERROR).getText().isBlank());
        } catch (TimeoutException e) {
            throw new AssertionError("Se esperaba un mensaje de validación, pero la aplicación aceptó el dato "
                    + "(modal abierto: " + isModalOpen() + ")");
        }
        return driver.findElement(ERROR).getText();
    }

    public boolean isModalOpen() {
        return !driver.findElements(OVERLAY).isEmpty();
    }

    public String weightSumText() {
        return driver.findElement(WEIGHT_SUM).getText();
    }

    public boolean weightSumIsOk() {
        return driver.findElement(WEIGHT_SUM).getAttribute("class").contains("ok");
    }

    // ---------- detalle del curso ----------

    public LibretaPage waitForCourseDetail() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(DETAIL_TITLE));
        return this;
    }

    public String courseTitle() {
        return driver.findElement(DETAIL_TITLE).getText();
    }

    public String courseMeta() {
        return driver.findElement(DETAIL_META).getText();
    }

    public boolean isEmptyDashboardVisible() {
        return !driver.findElements(EMPTY_DASHBOARD).isEmpty();
    }

    public String emptyDashboardMessage() {
        return driver.findElement(EMPTY_DASHBOARD).getText();
    }

    // ---------- notas ----------

    /** Escribe una nota en la evaluación {@code index} (0 = C1 ... 5 = P3) y confirma con Enter. */
    public LibretaPage enterGrade(int index, String raw) {
        startEditingGrade(index);
        By input = By.cssSelector(".val[data-idx='" + index + "'] input");
        WebElement field = driver.findElement(input);
        replaceWithoutBlur(field, raw);
        field.sendKeys(Keys.ENTER);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(input));
        return this;
    }

    /** Escribe una nota pero la cancela con Escape. */
    public LibretaPage typeGradeAndCancel(int index, String raw) {
        startEditingGrade(index);
        By input = By.cssSelector(".val[data-idx='" + index + "'] input");
        WebElement field = driver.findElement(input);
        replaceWithoutBlur(field, raw);
        field.sendKeys(Keys.ESCAPE);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(input));
        return this;
    }

    private void startEditingGrade(int index) {
        By cell = By.cssSelector(".val[data-act='edit-grade'][data-idx='" + index + "']");
        wait.until(ExpectedConditions.elementToBeClickable(cell)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".val[data-idx='" + index + "'] input")));
    }

    public String gradeText(int index) {
        return driver.findElement(By.cssSelector(".grade-cell[data-idx='" + index + "'] .val")).getText().trim();
    }

    public String accumulatedText() {
        return driver.findElements(STAMPS).get(0).getText().trim();
    }

    public String projectedText() {
        return driver.findElements(STAMPS).get(1).getText().trim();
    }

    public String passBannerText() {
        return driver.findElements(RISK_BANNER).get(0).getText();
    }

    // ---------- editar curso ----------

    public LibretaPage openEditCourseModal() {
        wait.until(ExpectedConditions.elementToBeClickable(EDIT_COURSE)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(EDIT_CREDITS));
        return this;
    }

    public LibretaPage typeEditCredits(String credits) {
        type(driver.findElement(EDIT_CREDITS), credits);
        return this;
    }

    public LibretaPage confirmEdit() {
        driver.findElement(CONFIRM_EDIT).click();
        return this;
    }

    public LibretaPage waitForModalClosed() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(OVERLAY));
        return this;
    }

    /**
     * Reemplaza el contenido borrando con BACKSPACE. No se usa WebElement.clear() porque dispara "blur",
     * y en la celda de nota la app interpreta el blur como "confirmar" y redibuja (StaleElementReference).
     */
    private static void replaceWithoutBlur(WebElement field, String value) {
        String current = field.getAttribute("value");
        field.sendKeys(Keys.END);
        for (int i = 0; current != null && i < current.length(); i++) {
            field.sendKeys(Keys.BACK_SPACE);
        }
        if (!value.isEmpty()) {
            field.sendKeys(value);
        }
    }

    private static void type(WebElement field, String value) {
        field.clear();
        if (!value.isEmpty()) {
            field.sendKeys(value);
        }
    }
}
