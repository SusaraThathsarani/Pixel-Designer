package com.pixology.backend.e2e.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class RegisterUiIT {

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        // headless for CI; keep your local override via -DHEADLESS=false if desired
        opts.addArguments("--headless=new","--no-sandbox","--disable-gpu","--window-size=1280,800");
        driver = new ChromeDriver(opts);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        baseUrl = System.getProperty("UI_BASE_URL",
                System.getenv().getOrDefault("UI_BASE_URL", "http://localhost:5173"));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    void user_can_register_successfully() {
        driver.get(baseUrl + "/signup");

        // Wait until form is ready
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='signup-form']")));

        String unique = String.valueOf(System.currentTimeMillis());
        driver.findElement(By.name("username")).sendKeys("TestUser" + unique);
        driver.findElement(By.name("email")).sendKeys("test" + unique + "@example.com");
        driver.findElement(By.name("password")).sendKeys("secret123");

        // Prefer a stable data-testid (falls back to type='submit' if needed)
        By submit = By.cssSelector("[data-testid='signup-submit'], button[type='submit']");
        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();

        // success navigates to /login (frontend delays ~800ms)
        wait.until(ExpectedConditions.urlContains("/login"));
        assertThat(driver.getCurrentUrl()).contains("/login");
    }
}
