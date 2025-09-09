package com.cdbu.pricing.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.*;
import java.time.Duration;

/**
 * TestNG Suite for testing Price Movement Business Logic
 * 
 * Business Logic: Eligible Records for Moving Future USD List Prices to Publish USD List Prices
 * 
 * Conditions:
 * 1. Future USD List Price is not null
 * 2. Future Local Currency LP Effective Date is not null
 * 3. Pricing action's (China Pricing Action) Approval value is 'Yes' in 007 Pricing Action Entity
 * 4. Any one of Current product group, Future product group is not null in List Pricing Entity
 * 5. Publish USD List Price is null
 * 6. Any one of the following conditions should be satisfied:
 *    i) LP Overwrite Flag is No and All 3 Pricing Actions (DN, PVC, China) are not in Emergency
 *    ii) LP Overwrite Flag is No and Any one of the Pricing Actions (DN, PVC, China) is Emergency 
 *        and all 3 approvals (Pricing Manager Approval Status, Approver 1, Approver 2) contain 'Yes/Auto'
 *    iii) LP Overwrite Flag is Null and any one of the below conditions should satisfy:
 *         a) All 3 Pricing Actions (DN, PVC, China) are Null/None and Current Local Currency List Price is null
 *         b) Any one of the Pricing Actions (DN, PVC, China) is Emergency and all 3 approvals contain 'Yes/Auto'
 *    iv) LP Overwrite Flag is Yes and all 5 approvals contain 'Yes/Auto'
 * 
 * Movement: Move Future USD List Price to Publish USD List Price and 
 *          Future Local Currency LP Effective Date to Publish USD LP Effective Date
 *          Make Future values null after movement
 */
public class spriced_test {
    
    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "https://cdbu-dev.alpha.simadvisory.com/spriced-data";
    private static final String USERNAME = "gopika";
    private static final String PASSWORD = "Rainy$Nights2Read";
    private static final int TIMEOUT = 30;
    
    @BeforeClass
    public void setUpClass() {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\aleena.j_simadvisory\\eclipse-workspace\\login_from_excel\\servers\\chromedriver.exe"); // Update with actual path
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));
        
        // Login to the application
        login();
    }
    
    @AfterClass
    public void tearDownClass() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    private void login() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement submitButton = driver.findElement(By.xpath("//input[@type='submit']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        submitButton.click();

        // Use the class-level 'wait' (don’t redeclare a new one)
        wait.until(ExpectedConditions.titleContains("Dashboard"));
    }

    
    @Test(priority = 1, description = "Test Scenario 1: All basic conditions met with LP Overwrite Flag = No, no emergency actions")
    public void testScenario1_BasicConditions_NoOverwrite_NoEmergency() {
        // Navigate to pricing records page
        navigateToPricingRecords();
        
        // Create/Select a record that meets basic conditions 1-5
        String recordId = createTestRecordWithDefaults(
            100.50, // Future USD List Price (not null)
            "2024-01-15", // Future Local Currency LP Effective Date (not null)
            "Yes", // China Pricing Action Approval
            "ProductGroup1", // Current Product Group (not null)
            null, // Future Product Group
            null, // Publish USD List Price (null)
            "No", // LP Overwrite Flag
            "Normal", "Normal", "Normal", // All actions are Normal (not Emergency)
            null // Current Local Currency List Price
        );
        
        // Execute price movement operation
        boolean movementResult = executePriceMovement(recordId);
        
        // Verify the movement was successful
        Assert.assertTrue(movementResult, "Price movement should be successful for scenario 1");
        
        // Verify the data after movement
        verifyPriceMovement(recordId, 100.50, "2024-01-15");
    }
    
    @Test(priority = 2, description = "Test Scenario 2: LP Overwrite Flag = No, one emergency action with all approvals Yes/Auto")
    public void testScenario2_NoOverwrite_EmergencyWithApprovals() {
        navigateToPricingRecords();
        
        String recordId = createTestRecordWithApprovals(
            250.75, // Future USD List Price
            "2024-02-20", // Future Local Currency LP Effective Date
            "Yes", // China Pricing Action Approval
            "ProductGroup2", // Current Product Group
            null, // Future Product Group
            null, // Publish USD List Price
            "No", // LP Overwrite Flag
            "Emergency", "Normal", "Normal", // One emergency action
            null, // Current Local Currency List Price
            "Yes", "Auto", "Yes" // All 3 approvals are Yes/Auto
        );
        
        boolean movementResult = executePriceMovement(recordId);
        Assert.assertTrue(movementResult, "Price movement should be successful for scenario 2");
        verifyPriceMovement(recordId, 250.75, "2024-02-20");
    }
    
    @Test(priority = 3, description = "Test Scenario 3: LP Overwrite Flag = Null, all actions Null and Current Local Currency LP is null")
    public void testScenario3_NullOverwrite_AllActionsNull() {
        navigateToPricingRecords();
        
        String recordId = createTestRecordWithDefaults(
            150.25, // Future USD List Price
            "2024-03-10", // Future Local Currency LP Effective Date
            "Yes", // China Pricing Action Approval
            null, // Current Product Group
            "ProductGroup3", // Future Product Group (condition 4)
            null, // Publish USD List Price
            null, // LP Overwrite Flag (null)
            null, null, null, // All actions are null
            null // Current Local Currency List Price is null
        );
        
        boolean movementResult = executePriceMovement(recordId);
        Assert.assertTrue(movementResult, "Price movement should be successful for scenario 3");
        verifyPriceMovement(recordId, 150.25, "2024-03-10");
    }
    
    @Test(priority = 4, description = "Test Scenario 4: LP Overwrite Flag = Null, emergency action with all approvals")
    public void testScenario4_NullOverwrite_EmergencyWithApprovals() {
        navigateToPricingRecords();
        
        String recordId = createTestRecordWithApprovals(
            300.00, // Future USD List Price
            "2024-04-15", // Future Local Currency LP Effective Date
            "Yes", // China Pricing Action Approval
            "ProductGroup4", // Current Product Group
            null, // Future Product Group
            null, // Publish USD List Price
            null, // LP Overwrite Flag (null)
            "Normal", "Emergency", "Normal", // One emergency action
            null, // Current Local Currency List Price
            "Auto", "Yes", "Auto" // All 3 approvals are Yes/Auto
        );
        
        boolean movementResult = executePriceMovement(recordId);
        Assert.assertTrue(movementResult, "Price movement should be successful for scenario 4");
        verifyPriceMovement(recordId, 300.00, "2024-04-15");
    }
    
    @Test(priority = 5, description = "Test Scenario 5: LP Overwrite Flag = Yes, all 5 approvals are Yes/Auto")
    public void testScenario5_YesOverwrite_AllApprovalsYes() {
        navigateToPricingRecords();
        
        String recordId = createTestRecordWithAllApprovals(
            450.80, // Future USD List Price
            "2024-05-20", // Future Local Currency LP Effective Date
            "Yes", // China Pricing Action Approval
            "ProductGroup5", // Current Product Group
            null, // Future Product Group
            null, // Publish USD List Price
            "Yes", // LP Overwrite Flag
            "Normal", "Normal", "Normal", // Actions don't matter when overwrite is Yes
            null, // Current Local Currency List Price
            "Yes", "Auto", "Yes", "Auto", "Yes" // All 5 approvals are Yes/Auto
        );
        
        boolean movementResult = executePriceMovement(recordId);
        Assert.assertTrue(movementResult, "Price movement should be successful for scenario 5");
        verifyPriceMovement(recordId, 450.80, "2024-05-20");
    }
    
    @Test(priority = 6, description = "Test Negative Scenario: Future USD List Price is null - should fail")
    public void testNegativeScenario_FutureUSDPriceNull() {
        navigateToPricingRecords();
        
        String recordId = createTestRecordWithDefaults(
            null, // Future USD List Price (null - violates condition 1)
            "2024-06-15", // Future Local Currency LP Effective Date
            "Yes", // China Pricing Action Approval
            "ProductGroup6", // Current Product Group
            null, // Future Product Group
            null, // Publish USD List Price
            "No", // LP Overwrite Flag
            "Normal", "Normal", "Normal", // Actions
            null // Current Local Currency List Price
        );
        
        boolean movementResult = executePriceMovement(recordId);
        Assert.assertFalse(movementResult, "Price movement should fail when Future USD List Price is null");
    }
    
    @Test(priority = 7, description = "Test Negative Scenario: China Pricing Action Approval is not Yes - should fail")
    public void testNegativeScenario_ChinaApprovalNotYes() {
        navigateToPricingRecords();
        
        String recordId = createTestRecordWithDefaults(
            200.00, // Future USD List Price
            "2024-07-10", // Future Local Currency LP Effective Date
            "No", // China Pricing Action Approval (not Yes - violates condition 3)
            "ProductGroup7", // Current Product Group
            null, // Future Product Group
            null, // Publish USD List Price
            "No", // LP Overwrite Flag
            "Normal", "Normal", "Normal", // Actions
            null // Current Local Currency List Price
        );
        
        boolean movementResult = executePriceMovement(recordId);
        Assert.assertFalse(movementResult, "Price movement should fail when China Pricing Action Approval is not Yes");
    }
    
    @Test(priority = 8, description = "Test Negative Scenario: Emergency action without proper approvals - should fail")
    public void testNegativeScenario_EmergencyWithoutApprovals() {
        navigateToPricingRecords();
        
        String recordId = createTestRecordWithApprovals(
            175.50, // Future USD List Price
            "2024-08-05", // Future Local Currency LP Effective Date
            "Yes", // China Pricing Action Approval
            "ProductGroup8", // Current Product Group
            null, // Future Product Group
            null, // Publish USD List Price
            "No", // LP Overwrite Flag
            "Emergency", "Normal", "Normal", // Emergency action
            null, // Current Local Currency List Price
            "No", "Yes", "Auto" // Not all approvals are Yes/Auto
        );
        
        boolean movementResult = executePriceMovement(recordId);
        Assert.assertFalse(movementResult, "Price movement should fail when emergency action lacks proper approvals");
    }
    
    // Helper methods - Fixed method signatures
    private void navigateToPricingRecords() {
        // If we’re already on the records page, bail out quickly
        try {
            driver.findElement(By.xpath("//table[@id='pricing-records-table']"));
            return;
        } catch (Exception ignore) {}

        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(8));

        // Try menu path (Pricing -> Records)
        try {
            WebElement pricingMenu = shortWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), 'Pricing') or contains(@href, 'pricing')]")));
            pricingMenu.click();

            WebElement recordsSubmenu = shortWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), 'Records') or contains(@href, 'records')]")));
            recordsSubmenu.click();

            shortWait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//table[@id='pricing-records-table']")));
            return;
        } catch (Exception firstAttempt) {
            // fall through to direct URL
        }

        // Fallback: navigate directly (faster than burning long waits)
        try {
            driver.get(BASE_URL + "/pricing/records");
            WebDriverWait urlWait = new WebDriverWait(driver, Duration.ofSeconds(8));
            urlWait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//table[@id='pricing-records-table']")));
            return;
        } catch (Exception secondAttempt) {
            // As a final small retry, refresh once (handles transient load issues)
            driver.navigate().refresh();
            WebDriverWait lastWait = new WebDriverWait(driver, Duration.ofSeconds(6));
            lastWait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//table[@id='pricing-records-table']")));
        }
    }

    
    // Method for scenarios without approvals
    private String createTestRecordWithDefaults(Double futureUSDPrice, String futureEffectiveDate, 
                                              String chinaApproval, String currentProductGroup, 
                                              String futureProductGroup, String publishUSDPrice, 
                                              String lpOverwriteFlag, String dnAction, 
                                              String pvcAction, String chinaAction, 
                                              String currentLocalCurrencyLP) {
        return createTestRecord(futureUSDPrice, futureEffectiveDate, chinaApproval, currentProductGroup,
                              futureProductGroup, publishUSDPrice, lpOverwriteFlag, dnAction, pvcAction, 
                              chinaAction, currentLocalCurrencyLP, new String[0]);
    }
    
    // Method for scenarios with 3 approvals
    private String createTestRecordWithApprovals(Double futureUSDPrice, String futureEffectiveDate, 
                                               String chinaApproval, String currentProductGroup, 
                                               String futureProductGroup, String publishUSDPrice, 
                                               String lpOverwriteFlag, String dnAction, 
                                               String pvcAction, String chinaAction, 
                                               String currentLocalCurrencyLP, String approval1, 
                                               String approval2, String approval3) {
        return createTestRecord(futureUSDPrice, futureEffectiveDate, chinaApproval, currentProductGroup,
                              futureProductGroup, publishUSDPrice, lpOverwriteFlag, dnAction, pvcAction, 
                              chinaAction, currentLocalCurrencyLP, new String[]{approval1, approval2, approval3});
    }
    
    // Method for scenarios with all 5 approvals
    private String createTestRecordWithAllApprovals(Double futureUSDPrice, String futureEffectiveDate, 
                                                  String chinaApproval, String currentProductGroup, 
                                                  String futureProductGroup, String publishUSDPrice, 
                                                  String lpOverwriteFlag, String dnAction, 
                                                  String pvcAction, String chinaAction, 
                                                  String currentLocalCurrencyLP, String approval1, 
                                                  String approval2, String approval3, String approval4, 
                                                  String approval5) {
        return createTestRecord(futureUSDPrice, futureEffectiveDate, chinaApproval, currentProductGroup,
                              futureProductGroup, publishUSDPrice, lpOverwriteFlag, dnAction, pvcAction, 
                              chinaAction, currentLocalCurrencyLP, 
                              new String[]{approval1, approval2, approval3, approval4, approval5});
    }
    
    // Main method that handles all record creation
    private String createTestRecord(Double futureUSDPrice, String futureEffectiveDate, String chinaApproval,
                                  String currentProductGroup, String futureProductGroup, String publishUSDPrice, 
                                  String lpOverwriteFlag, String dnAction, String pvcAction, String chinaAction,
                                  String currentLocalCurrencyLP, String[] approvals) {
        
        // Click on Add New Record button
        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Add') or @id='add-record-btn']")));
        addButton.click();
        
        // Wait for form to appear
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//form[@id='pricing-form']")));
        
        // Fill form fields based on parameters
        if (futureUSDPrice != null) {
            WebElement futureUSDField = driver.findElement(By.name("future_usd_list_price"));
            futureUSDField.clear();
            futureUSDField.sendKeys(futureUSDPrice.toString());
        }
        
        if (futureEffectiveDate != null) {
            WebElement futureEffectiveDateField = driver.findElement(By.name("future_lc_lp_effective_date"));
            futureEffectiveDateField.clear();
            futureEffectiveDateField.sendKeys(futureEffectiveDate);
        }
        
        if (chinaApproval != null) {
            Select chinaApprovalSelect = new Select(driver.findElement(By.name("china_pricing_action_approval")));
            chinaApprovalSelect.selectByValue(chinaApproval);
        }
        
        if (currentProductGroup != null) {
            WebElement currentPGField = driver.findElement(By.name("current_product_group"));
            currentPGField.clear();
            currentPGField.sendKeys(currentProductGroup);
        }
        
        if (futureProductGroup != null) {
            WebElement futurePGField = driver.findElement(By.name("future_product_group"));
            futurePGField.clear();
            futurePGField.sendKeys(futureProductGroup);
        }
        
        if (publishUSDPrice != null) {
            WebElement publishUSDField = driver.findElement(By.name("publish_usd_list_price"));
            publishUSDField.clear();
            publishUSDField.sendKeys(publishUSDPrice);
        }
        
        if (lpOverwriteFlag != null) {
            Select lpOverwriteSelect = new Select(driver.findElement(By.name("lp_overwrite_flag")));
            lpOverwriteSelect.selectByValue(lpOverwriteFlag);
        }
        
        // Set pricing actions
        if (dnAction != null) {
            Select dnSelect = new Select(driver.findElement(By.name("dn_pricing_action")));
            dnSelect.selectByValue(dnAction);
        }
        
        if (pvcAction != null) {
            Select pvcSelect = new Select(driver.findElement(By.name("pvc_pricing_action")));
            pvcSelect.selectByValue(pvcAction);
        }
        
        if (chinaAction != null) {
            Select chinaActionSelect = new Select(driver.findElement(By.name("china_pricing_action")));
            chinaActionSelect.selectByValue(chinaAction);
        }
        
        if (currentLocalCurrencyLP != null) {
            WebElement currentLCLPField = driver.findElement(By.name("current_lc_list_price"));
            currentLCLPField.clear();
            currentLCLPField.sendKeys(currentLocalCurrencyLP);
        }
        
        // Set approvals
        String[] approvalFieldNames = {
            "pricing_manager_approval_status",
            "approver_1_status",
            "approver_2_status",
            "approver_3_status",
            "approver_4_status"
        };
        
        for (int i = 0; i < approvals.length && i < approvalFieldNames.length; i++) {
            if (approvals[i] != null) {
                Select approvalSelect = new Select(driver.findElement(By.name(approvalFieldNames[i])));
                approvalSelect.selectByValue(approvals[i]);
            }
        }
        
        // Submit the form
        WebElement submitButton = driver.findElement(By.xpath("//button[@type='submit' or contains(text(), 'Save')]"));
        submitButton.click();
        
        // Wait for success message and get record ID
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(@class, 'alert-success') or contains(@class, 'success-message')]")));
        
        String messageText = successMessage.getText();
        String recordId = extractRecordIdFromMessage(messageText);
        
        return recordId;
    }
    
    private boolean executePriceMovement(String recordId) {
        try {
            // Navigate to price movement section
            WebElement movementMenu = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), 'Price Movement') or contains(@href, 'movement')]")));
            movementMenu.click();
            
            // Find and select the record
            WebElement recordCheckbox = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@type='checkbox' and @value='" + recordId + "']")));
            recordCheckbox.click();
            
            // Click Execute Movement button
            WebElement executeButton = driver.findElement(
                By.xpath("//button[contains(text(), 'Execute Movement') or @id='execute-movement-btn']"));
            executeButton.click();
            
            // Wait for confirmation dialog
            WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Confirm') or @id='confirm-movement']")));
            confirmButton.click();
            
            // Wait for result message
            WebElement resultMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//div[contains(@class, 'alert') or contains(@class, 'message')]")));
            
            String messageText = resultMessage.getText().toLowerCase();
            return messageText.contains("success") || messageText.contains("completed");
            
        } catch (Exception e) {
            System.err.println("Error executing price movement: " + e.getMessage());
            return false;
        }
    }
    
    private void verifyPriceMovement(String recordId, Double expectedPublishPrice, String expectedEffectiveDate) {
        // Navigate to record details
        navigateToRecordDetails(recordId);
        
        // Verify that Future USD List Price is now in Publish USD List Price
        WebElement publishUSDField = driver.findElement(By.name("publish_usd_list_price"));
        String publishValue = publishUSDField.getAttribute("value");
        Assert.assertEquals(Double.parseDouble(publishValue), expectedPublishPrice, 
                          "Publish USD List Price should match the original Future USD List Price");
        
        // Verify that Future Local Currency LP Effective Date is now in Publish USD LP Effective Date
        WebElement publishEffectiveDateField = driver.findElement(By.name("publish_usd_lp_effective_date"));
        String publishDateValue = publishEffectiveDateField.getAttribute("value");
        Assert.assertEquals(publishDateValue, expectedEffectiveDate,
                          "Publish USD LP Effective Date should match the original Future Local Currency LP Effective Date");
        
        // Verify that Future USD List Price is now null
        WebElement futureUSDField = driver.findElement(By.name("future_usd_list_price"));
        String futureValue = futureUSDField.getAttribute("value");
        Assert.assertTrue(futureValue == null || futureValue.isEmpty(),
                         "Future USD List Price should be null after movement");
        
        // Verify that Future Local Currency LP Effective Date is now null
        WebElement futureEffectiveDateField = driver.findElement(By.name("future_lc_lp_effective_date"));
        String futureDateValue = futureEffectiveDateField.getAttribute("value");
        Assert.assertTrue(futureDateValue == null || futureDateValue.isEmpty(),
                         "Future Local Currency LP Effective Date should be null after movement");
    }
    
    private void navigateToRecordDetails(String recordId) {
        // Navigate back to records list if needed
        WebElement recordsLink = driver.findElement(By.xpath("//a[contains(text(), 'Records')]"));
        recordsLink.click();
        
        // Find and click on the specific record
        WebElement recordLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href, 'record/" + recordId + "') or @data-record-id='" + recordId + "']")));
        recordLink.click();
        
        // Wait for record details to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//form[@id='record-details-form']")));
    }
    
    private String extractRecordIdFromMessage(String message) {
        // Extract record ID from success message
        // This would depend on the actual message format from the application
        // Example: "Record created successfully with ID: 12345"
        String[] parts = message.split("ID:");
        if (parts.length > 1) {
            return parts[1].trim().replaceAll("[^0-9]", "");
        }
        return String.valueOf(System.currentTimeMillis()); // Fallback to timestamp
    }
    
    @DataProvider(name = "eligibleRecordsData")
    public Object[][] getEligibleRecordsData() {
        return new Object[][] {
            // futureUSD, futureDate, chinaApproval, currentPG, futurePG, publishUSD, lpOverwrite, dnAction, pvcAction, chinaAction, currentLCLP, approvals[], expectedResult
            {100.0, "2024-01-01", "Yes", "PG1", null, null, "No", "Normal", "Normal", "Normal", null, new String[]{}, true},
            {200.0, "2024-02-01", "Yes", "PG2", null, null, "No", "Emergency", "Normal", "Normal", null, new String[]{"Yes", "Auto", "Yes"}, true},
            {300.0, "2024-03-01", "Yes", "PG3", null, null, null, null, null, null, null, new String[]{}, true},
            {400.0, "2024-04-01", "Yes", "PG4", null, null, null, "Normal", "Emergency", "Normal", null, new String[]{"Auto", "Yes", "Auto"}, true},
            {500.0, "2024-05-01", "Yes", "PG5", null, null, "Yes", "Normal", "Normal", "Normal", null, new String[]{"Yes", "Auto", "Yes", "Auto", "Yes"}, true},
            // Negative scenarios
            {null, "2024-06-01", "Yes", "PG6", null, null, "No", "Normal", "Normal", "Normal", null, new String[]{}, false},
            {600.0, null, "Yes", "PG7", null, null, "No", "Normal", "Normal", "Normal", null, new String[]{}, false},
            {700.0, "2024-07-01", "No", "PG8", null, null, "No", "Normal", "Normal", "Normal", null, new String[]{}, false},
            {800.0, "2024-08-01", "Yes", "PG9", null, "100.0", "No", "Normal", "Normal", "Normal", null, new String[]{}, false}
        };
    }
    
    @Test(dataProvider = "eligibleRecordsData", 
          description = "Data-driven test for all price movement scenarios")
    public void testPriceMovementScenarios(Double futureUSD, String futureDate, String chinaApproval,
                                         String currentPG, String futureProductGroup, String publishUSD, 
                                         String lpOverwrite, String dnAction, String pvcAction, String chinaAction,
                                         String currentLCLP, String[] approvals, boolean expectedResult) {
        
        navigateToPricingRecords();
        
        String recordId = createTestRecord(futureUSD, futureDate, chinaApproval, currentPG,
                                         futureProductGroup, publishUSD, lpOverwrite, dnAction, 
                                         pvcAction, chinaAction, currentLCLP, approvals);
        
        boolean movementResult = executePriceMovement(recordId);
        
        if (expectedResult) {
            Assert.assertTrue(movementResult, "Price movement should be successful for valid scenario");
            if (futureUSD != null && futureDate != null) {
                verifyPriceMovement(recordId, futureUSD, futureDate);
            }
        } else {
            Assert.assertFalse(movementResult, "Price movement should fail for invalid scenario");
        }
    }
    @AfterClass
    public void afterclass() {
    	driver.quit();
    }
}