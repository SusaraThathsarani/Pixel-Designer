package com.pixology.backend.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.net.http.*;
import java.net.URI;
import java.time.Duration;

public class LoginIT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private final String UI = System.getProperty("UI_BASE_URL", "http://localhost:5173");
    private final String API = System.getProperty("BACKEND_BASE_URL", "http://localhost:8080");

    // fixed test user
    private final String email = "e2e_login@example.com";
    private final String password = "secret123";

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(UiOpts.options());
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) driver.quit();
    }

    @BeforeEach
    void ensureUserExists() throws Exception {
        // register (409 means already exists -> OK)
        String body = String.format("{\"username\":\"E2E\",\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create(API + "/api/users/register"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    @Test
    void login_happy_path_redirects_home_or_shows_success() {
        driver.get(UI + "/login");

        type(By.name("email"), email);
        type(By.name("password"), password);
        click(By.xpath("//button[normalize-space()='Log in' or contains(.,'Log in')]"));

        // success banner “Welcome back! Redirecting…” or URL changes (your app returns to /)
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlToBe(UI + "/"),
                ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".text-green-700"), "Welcome back")
        ));

        boolean redirectedHome = driver.getCurrentUrl().equals(UI + "/");
        boolean successShown = !driver.findElements(By.cssSelector(".text-green-700")).isEmpty();

        Assertions.assertTrue(redirectedHome || successShown, "Expected redirect to home or success banner");
    }

    private void type(By locator, String value) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        el.clear();
        el.sendKeys(value);
    }

    private void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }
}
