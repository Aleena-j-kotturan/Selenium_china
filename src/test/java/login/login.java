package login;

import org.testng.annotations.Test;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class login {
    WebDriver driver;
    WebDriverWait wait;

    @BeforeClass
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20)); // Increased wait time for stability

        // Login steps
        driver.get("https://auth.dev.simadvisory.com/auth/realms/D_SPRICED/protocol/openid-connect/auth?client_id=CHN_D_SPRICED_Client&redirect_uri=https%3A%2F%2Fcdbu-dev.alpha.simadvisory.com%2Fspriced-data&state=919d809b-1a17-4fff-abba-1b5411944dcd&response_mode=fragment&response_type=code&scope=openid&nonce=1dbd7be4-eb87-43fa-8c5e3cf56531");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys("gopika");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password"))).sendKeys("Rainy$Nights2Read");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='kc-login']"))).click();
        
        // Wait for the main page content to load after login
        wait.until(ExpectedConditions.urlContains("spriced-data"));
        System.out.println("Login successful. Current URL: " + driver.getCurrentUrl());
    }

    @Test(priority = 1)
    public void test1_CheckListPrice() {
        // Find dropdown by label to make it more robust
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//mat-select[contains(.,'003 List Pricing')]")
        ));
        dropdown.click();

        // Use more specific XPath to find the option
        wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//mat-option/span[normalize-space(text())='003 List Pricing']")
        )).click();

        // Locate input field by its associated label
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(
        		By.xpath("//mat-label[contains(text(),'Future USD List Price')]/following::input[1]")
        ));
        
        // Wait for the value to be populated
        wait.until(d -> !input.getAttribute("value").trim().isEmpty());
        
        String value = input.getAttribute("value");
        System.out.println("Future USD List Price is: " + value);
        
        // Assert that the value is not empty and check for a specific format if possible
        Assert.assertNotNull(value, "Future USD List Price is null");
        Assert.assertFalse(value.trim().isEmpty(), "Future USD List Price is empty");
        // For a more robust test, assert a specific value or a valid number format
        // Assert.assertEquals(value, "123.45", "Incorrect price value"); 
    }

    @Test(priority = 2)
    public void test2_CheckEffectiveDate() {
        // Locate the date input field by its label
        WebElement dateInput = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//mat-form-field[.//mat-label[normalize-space(text())='Future Local Currency LP Effective Date']]//input")
        ));
        
        // Scroll into view using JavaScript
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", dateInput);

        // Get the value and assert it's empty
        String dateValue = dateInput.getAttribute("value");
        System.out.println("Future LP Effective Date is: " + (dateValue == null ? "null" : "'" + dateValue + "'"));
        Assert.assertTrue(dateValue == null || dateValue.trim().isEmpty(), 
            "Expected empty effective date, but found: '" + dateValue + "'");
    }

    @Test(priority = 3)
    public void test3_CheckPricingActionApproval() {
        // Find the "Pricing Action" dropdown
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//mat-label[normalize-space(text())='Pricing Action']/following-sibling::mat-select")
        ));
        dropdown.click();
        
        // Select the "007 Pricing Action" option
        wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//mat-option/span[normalize-space(text())='007 Pricing Action']")
        )).click();
        
        // Wait for the form to update and show the approval field
        WebElement approvalElement = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//mat-select[@aria-label='Approval' or contains(.,'Approval')]//span[contains(@class,'mat-mdc-select-value-text')]")
        ));
        
        // Get the text value and assert it's "Yes"
        String approvalValue = approvalElement.getText().trim();
        System.out.println("Pricing Action Approval value: " + approvalValue);
        Assert.assertEquals(approvalValue, "Yes", "Pricing Action Approval is not 'Yes'");
    }

    @Test(priority = 4)
    public void test4_CheckProductGroupValues() {
        // Select "003 List Pricing" again to reset the state
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//mat-label[normalize-space(text())='Pricing Action']/following-sibling::mat-select")
        ));
        dropdown.click();
        wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//mat-option/span[normalize-space(text())='003 List Pricing']")
        )).click();
        
        // Wait for the values to appear
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//mat-label[normalize-space(text())='Current Product Group']/following-sibling::input")
        ));

        // Get the value from the "Current Product Group" input
        WebElement currentProductGroupInput = driver.findElement(By.xpath("//mat-label[normalize-space(text())='Current Product Group']/following-sibling::input"));
        String currentValue = currentProductGroupInput.getAttribute("value");
        System.out.println("Current Product Group: '" + currentValue + "'");

        // Get the value from the "Future Product Group" input
        WebElement futureProductGroupInput = driver.findElement(By.xpath("//mat-label[normalize-space(text())='Future Product Group']/following-sibling::input"));
        String futureValue = futureProductGroupInput.getAttribute("value");
        System.out.println("Future Product Group: '" + futureValue + "'");

        // Assert that both are not empty and are equal
        Assert.assertFalse(currentValue == null || currentValue.trim().isEmpty(), "Current Product Group is empty!");
        Assert.assertFalse(futureValue == null || futureValue.trim().isEmpty(), "Future Product Group is empty!");
        Assert.assertEquals(currentValue, futureValue, "Current and Future Product Groups do not match!");
    }

    @AfterClass
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}