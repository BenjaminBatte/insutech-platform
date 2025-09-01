package com.insurance.policy.insutech.e2e;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AutoPolicyUITest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    void setupDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        // Increase wait to 30s for backend + frontend response
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        driver.manage().window().maximize();
    }

    @AfterEach
    void cleanup() {
        if (driver != null) driver.quit();
    }
    @AfterAll
    void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    // 🔹 Helper to set date values using JavaScript (more reliable for React)
    private void setDateByJs(String elementId, String value) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        // Set the value directly via JavaScript
        js.executeScript("document.getElementById(arguments[0]).value = arguments[1];", elementId, value);
        // Trigger both input and change events for React
        js.executeScript(
                "var element = document.getElementById(arguments[0]);" +
                        "var inputEvent = new Event('input', { bubbles: true });" +
                        "var changeEvent = new Event('change', { bubbles: true });" +
                        "element.dispatchEvent(inputEvent);" +
                        "element.dispatchEvent(changeEvent);", elementId);
    }

    // 🔹 Helper to fill the form
    private void fillPolicyForm(String policyNumber, String firstName, String lastName,
                                String make, String model, String year, String premium,
                                String type, String status, String startDate, String endDate) {

        driver.findElement(By.id("policyNumber")).sendKeys(policyNumber);
        driver.findElement(By.id("firstName")).sendKeys(firstName);
        driver.findElement(By.id("lastName")).sendKeys(lastName);
        driver.findElement(By.id("vehicleMake")).sendKeys(make);
        driver.findElement(By.id("vehicleModel")).sendKeys(model);
        driver.findElement(By.id("vehicleYear")).sendKeys(year);
        driver.findElement(By.id("premiumAmount")).sendKeys(premium);
        driver.findElement(By.id("premiumAmount")).sendKeys(Keys.ENTER);

        new Select(driver.findElement(By.id("policyType"))).selectByVisibleText(type);
        new Select(driver.findElement(By.id("status"))).selectByVisibleText(status);

        setDateByJs("startDate", startDate);
        setDateByJs("endDate", endDate);
    }

    @Test
    void shouldLoadHomePage() {
        driver.get("http://localhost:5173/");
        wait.until(ExpectedConditions.titleContains("Insu-Tech"));
        assertTrue(driver.getTitle().contains("Insu-Tech"));
    }
    @AfterEach
    void takeScreenshot(TestInfo testInfo) {
        if (driver instanceof TakesScreenshot ts) {
            File screenshot = ts.getScreenshotAs(OutputType.FILE);
            screenshot.renameTo(new File("E:\\ins-tec\\screen-shot\\" + testInfo.getDisplayName() + ".png"));
        }
    }

    @Test
    @Transactional
    void shouldCreatePolicy_AP202_AlexWilliams() {
        driver.get("http://localhost:5173/create");
        fillPolicyForm(
                "AP-6178",
                "Ananda",
                "Wills",
                "Toyota",
                "Camry",
                "2020",
                "700",
                "LIABILITY",
                "ACTIVE",
                "2025-08-26",
                "2026-04-26"
        );

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement error = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".error-message")
                )
        );

        assertTrue(
                error.getText().toLowerCase().contains("end date must be after start date"),
                "Expected error: 'End date must be after start date.'"
        );
    }
    @Test
    void shouldShowErrorForMissingRequiredFields() {
        driver.get("http://localhost:5173/create");

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Form should not submit → still on /create
        assertTrue(driver.getCurrentUrl().contains("/create"));
    }

    @Test
    @Transactional
    void shouldNavigateToPoliciesPageAfterSubmit() {
        driver.get("http://localhost:5173/create");
        fillPolicyForm("AP-5965", "Navi", "Gate", "Honda", "Civic", "2022", "500",
                "COLLISION", "ACTIVE", "2025-09-01", "2025-12-01");

        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/policies"));
        assertTrue(driver.getCurrentUrl().contains("policies"));
    }

    @Test
    void shouldFailOnInvalidDateRange() {
        driver.get("http://localhost:5173/create");

        // Fill with an invalid date range (end date before start date)
        fillPolicyForm(
                "AP-1999",
                "John",
                "Invalid",
                "Tesla",
                "Model 3",
                "2023",
                "1200",
                "COMPREHENSIVE",
                "ACTIVE",
                "2025-12-01", // start date
                "2025-01-01"  // end date before start date
        );

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Wait for the error message
        WebElement error = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".error-message")
                )
        );


        assertTrue(
                error.getText().toLowerCase().contains("end date must be after start date"),
                "Expected error message: 'End date must be after start date.' but got: " + error.getText()
        );
    }

    @Test
    void shouldThrowExceptionWhenElementNotFound() {
        driver.get("http://localhost:5173/create");
        assertThrows(NoSuchElementException.class, () -> {
            driver.findElement(By.id("nonExistentElement")).click();
        });
    }

    @Test
    @Disabled("Example of skipping a test")
    void skippedTestExample() {
        driver.get("http://localhost:5173/");
        assertTrue(true); // Just demo
    }
    @Test
    @Tag("smoke")
    void smokeTestHomepageTitle() {
        driver.get("http://localhost:5173/");
        assertEquals("Insu-Tech", driver.getTitle(), "Home page title should match");
    }

    @Test
    void shouldCreatePolicy_AP200_BenjaminMukasa() {
        driver.get("http://localhost:5173/create");
        fillPolicyForm(
                "AP-8895",
                "Benja",
                "Muka",
                "Ford",
                "F-150",
                "2010",
                "900",
                "LIABILITY",
                "ACTIVE",
                "2025-08-26",
                "2025-08-27"  // End date after start date
        );

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement success = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".success-message, [data-testid='success-message']")
                )
        );
        assertTrue(success.getText().toLowerCase().contains("success"));
    }
}