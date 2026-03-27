package com.pixology.backend.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SignUpIT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private final String UI = System.getProperty("UI_BASE_URL", "http://localhost:5173");

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

    @Test
    @Order(1)
    void signUp_happy_path_redirects_to_login() {
        String uname = "user_" + UUID.randomUUID().toString().substring(0,8);
        String email = "u_" + UUID.randomUUID().toString().substring(0,8) + "@example.com";
        String password = "secret123";

        driver.get(UI + "/signup");
        type(By.name("username"), uname);
        type(By.name("email"), email);
        type(By.name("password"), password);

        click(By.xpath("//button[normalize-space()='Sign up' or contains(.,'Sign up')]"));

        // wait for success message from your component
        wait.until(ExpectedConditions.or(
                ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".text-green-700"), "Account created"),
                ExpectedConditions.urlContains("/login")
        ));

        // assert either success banner visible or redirected to login
        boolean redirected = driver.getCurrentUrl().contains("/login");
        boolean successShown = !driver.findElements(By.cssSelector(".text-green-700")).isEmpty();

        Assertions.assertTrue(redirected || successShown, "Expected success message or redirect to /login");
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
