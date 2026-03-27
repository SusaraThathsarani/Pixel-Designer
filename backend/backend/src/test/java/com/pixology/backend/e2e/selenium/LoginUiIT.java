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

public class LoginUiIT {

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
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
    void user_can_log_in_with_valid_credentials() {
        driver.get(baseUrl + "/login");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='login-form']")));
        driver.findElement(By.name("email")).sendKeys("a@example.com");
        driver.findElement(By.name("password")).sendKeys("secret123");

        By submit = By.cssSelector("[data-testid='login-submit'], button[type='submit']");
        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();

        // expect navigation to home (/) after short delay
        wait.until(ExpectedConditions.urlMatches(baseUrl + "/?$"));
        assertThat(driver.getCurrentUrl()).isIn(baseUrl + "/", baseUrl);
    }
}
