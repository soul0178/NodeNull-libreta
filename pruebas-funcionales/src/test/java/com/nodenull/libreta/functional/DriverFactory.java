package com.nodenull.libreta.functional;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * Crea el WebDriver según las propiedades del sistema:
 * -Dbrowser=chrome|firefox|auto (por defecto auto: intenta Chrome y, si no está, Firefox)
 * -Dheadless=false para ver el navegador (por defecto se ejecuta sin ventana).
 * Selenium Manager descarga el driver necesario automáticamente.
 */
public final class DriverFactory {

    private DriverFactory() {
    }

    public static WebDriver create() {
        boolean headless = !"false".equalsIgnoreCase(property("headless", "true"));
        String browser = property("browser", "auto").toLowerCase();
        return switch (browser) {
            case "chrome" -> chrome(headless);
            case "firefox" -> firefox(headless);
            default -> {
                try {
                    yield chrome(headless);
                } catch (RuntimeException chromeNotAvailable) {
                    yield firefox(headless);
                }
            }
        };
    }

    private static WebDriver chrome(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1366,900", "--no-sandbox", "--disable-dev-shm-usage");
        return new ChromeDriver(options);
    }

    private static WebDriver firefox(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        options.addArguments("--width=1366", "--height=900");
        return new FirefoxDriver(options);
    }

    /** Lee una propiedad; Maven pasa "${...}" literal cuando no se definió, por eso se ignora ese valor. */
    static String property(String key, String defaultValue) {
        String value = System.getProperty(key);
        return (value == null || value.isBlank() || value.startsWith("${")) ? defaultValue : value;
    }
}
