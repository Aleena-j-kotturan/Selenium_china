package china_Dbu_test;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;            // keep
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;


	


	public class china_Dbu_test {
	    WebDriver driver;
	    WebDriverWait wait;
	    JavascriptExecutor js;

	    @BeforeClass
	    public void setup() {
	        WebDriverManager.chromedriver().setup();
	        driver = new ChromeDriver();
	        driver.manage().window().maximize();
	        wait = new WebDriverWait(driver, Duration.ofSeconds(25));
	        js = (JavascriptExecutor) driver;

	        // Login
	        driver.get("https://auth.dev.simadvisory.com/auth/realms/D_SPRICED/protocol/openid-connect/auth?client_id=CHN_D_SPRICED_Client&redirect_uri=https%3A%2F%2Fcdbu-dev.alpha.simadvisory.com%2Fspriced-data&state=919d809b-1a17-4fff-abba-1b5411944dcd&response_mode=fragment&response_type=code&scope=openid&nonce=1dbd7be4-eb87-43fa-8c5e3cf56531");
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys("gopika");
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password"))).sendKeys("Rainy$Nights2Read");
	        wait.until(ExpectedConditions.elementToBeClickable(By.id("kc-login"))).click();
	        wait.until(ExpectedConditions.urlContains("spriced-data"));
	    }

	    // helper: select an Entity from the dropdown and confirm it
	    private void selectEntity(String name) {
	        By trigger   = By.xpath("//*[normalize-space()='Entity']/following::div[contains(@class,'mat-mdc-select-trigger')][1]");
	        By option    = By.xpath("//div[contains(@class,'cdk-overlay-pane')]//mat-option//span[normalize-space()='" + name + "']");
	        By valueSpan = By.xpath("(//*[normalize-space()='Entity']/following::div[contains(@class,'mat-mdc-select-value')][1]//span)[1]");

	        wait.until(ExpectedConditions.elementToBeClickable(trigger)).click();
	        wait.until(ExpectedConditions.elementToBeClickable(option)).click();
	        wait.until(ExpectedConditions.textToBePresentInElementLocated(valueSpan, name));
	        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.cdk-overlay-pane")));
	    }

	    // ─────────────────────────────────────────────────────────────────────────────
	    // KEEPING YOUR EXISTING TESTS (1–4) UNCHANGED
	    // ─────────────────────────────────────────────────────────────────────────────

	    @Test(priority = 1)
	    public void test1() {
	        // Ensure we're in List Pricing
	        selectEntity("003 List Pricing");

	        // 1) Make sure the expansion panel is open (keeps it simple & robust)
	        try {
	            WebElement header = driver.findElement(By.cssSelector("mat-expansion-panel .mat-expansion-panel-header"));
	            if (header != null && !"true".equals(header.getAttribute("aria-expanded"))) {
	                header.click();
	                wait.until(ExpectedConditions.attributeToBe(header, "aria-expanded", "true"));
	            }
	        } catch (Exception ignored) { /* panel may already be open */ }

	        // 2) Future USD List Price (your confirmed position: 14th sp-numeric)
	        By futureUsdListPrice = By.xpath("//sp-dynamic-form//sp-numeric[14]//mat-form-field//input");

	        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(futureUsdListPrice));
	        js.executeScript("arguments[0].scrollIntoView({block:'center'});", input);

	        // 3) Read value (no long waits for non-empty)
	        String value = input.getAttribute("value");
	        if (value == null || value.trim().isEmpty()) {
	            value = String.valueOf(input.getDomProperty("value"));
	        }
	        value = value == null ? "" : value.trim();

	        System.out.println("Future USD List Price = " + value);
	        Assert.assertFalse(value.isEmpty(), "❌ Future USD List Price is empty.");
	    }

	    @Test(priority = 2)
	    public void test2() {
	        // Stay in the same entity as test1 (safe to call again)
	        selectEntity("003 List Pricing");

	        // Ensure panel visible (same simple open check)
	        try {
	            WebElement header = driver.findElement(By.cssSelector("mat-expansion-panel .mat-expansion-panel-header"));
	            if (header != null && !"true".equals(header.getAttribute("aria-expanded"))) {
	                header.click();
	                wait.until(ExpectedConditions.attributeToBe(header, "aria-expanded", "true"));
	            }
	        } catch (Exception ignored) { }

	        // LP Effective Date (your confirmed position: 5th sp-date-picker)
	        By lpEffDate = By.xpath("//sp-dynamic-form//sp-date-picker[5]//input[contains(@class,'mat-datepicker-input')]");

	        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(lpEffDate));
	        js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);

	        String value = el.getAttribute("value");
	        if (value == null || value.trim().isEmpty()) {
	            value = String.valueOf(el.getDomProperty("value"));
	        }
	        value = value == null ? "" : value.trim();

	        System.out.println("LP Effective Date = '" + value + "'");
	        Assert.assertFalse(value.isEmpty(), "❌ LP Effective Date is empty!");
	    }

	    // New: precise Rule 2 (Future Local Currency LP Effective Date ≠ null) — does not replace your test2
	    @Test(priority = 2)
	    public void test2b_futureLocalCurrencyLPEffectiveDateNotNull() {
	        selectEntity("003 List Pricing");
	        expandAllPanels();

	        String value = readValueByLabel("Future Local Currency LP Effective Date");
	        if (value.isEmpty()) {
	            value = readValueByLabel("Future Local Currency LP Eff Date"); // common short label
	        }
	        System.out.println("Future Local Currency LP Effective Date = '" + value + "'");
	        Assert.assertFalse(value.isEmpty(), "❌ Future Local Currency LP Effective Date is empty.");
	    }

	    @Test(priority = 3)
	    public void test3() {
	        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(20));

	        // Open the Entity dropdown -> 007 Pricing Action
	        shortWait.until(ExpectedConditions.elementToBeClickable(
	            By.xpath("//*[normalize-space()='Entity']/following::div[contains(@class,'mat-mdc-select-trigger')][1]")))
	            .click();

	        shortWait.until(ExpectedConditions.elementToBeClickable(
	            By.xpath("//div[contains(@class,'cdk-overlay-pane')]//mat-option//span[normalize-space()='007 Pricing Action']")))
	            .click();

	        // Open Approval dropdown
	        By approvalTrigger = By.xpath("//*[normalize-space()='Approval']/following::div[contains(@class,'mat-mdc-select-trigger')][1]");
	        shortWait.until(ExpectedConditions.elementToBeClickable(approvalTrigger)).click();

	        // Overlay appears
	        shortWait.until(ExpectedConditions.visibilityOfElementLocated(
	            By.xpath("//div[contains(@class,'cdk-overlay-pane')]//mat-option")));

	        // Try read selected option from overlay; fallback to trigger text
	        By selectedOpt = By.xpath("//div[contains(@class,'cdk-overlay-pane')]//mat-option[@aria-selected='true' or contains(@class,'mdc-list-item--selected') or contains(@class,'mat-selected')]//span");

	        String approval;
	        try {
	            approval = shortWait.until(ExpectedConditions.visibilityOfElementLocated(selectedOpt)).getText().trim();
	        } catch (org.openqa.selenium.TimeoutException e) {
	            approval = shortWait.until(ExpectedConditions.visibilityOfElementLocated(
	                By.xpath("(//*[normalize-space()='Approval']/following::div[contains(@class,'mat-mdc-select-value')][1]//span)[1]")))
	                .getText().trim();
	        }

	        System.out.println("Approval = " + approval);
	        Assert.assertEquals(approval, "Yes", "Expected Approval = 'Yes'.");

	        // Close overlay
	        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
	        shortWait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.cdk-overlay-pane")));
	    }

	    @Test(priority = 4)
	    public void test4() {
	        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(20));

	        // Entity -> 003 List Pricing
	        selectEntity("003 List Pricing");

	        // Your working locators (kept as requested)
	        By currentPG = By.xpath("(//*[normalize-space()='Product Group Manager']/following::input[@type='text'])[6]");
	        By futurePG  = By.xpath("(//*[normalize-space()='Note for PG overwrite']/following::input[@type='text'])[8]");

	        WebElement curEl = shortWait.until(ExpectedConditions.visibilityOfElementLocated(currentPG));
	        WebElement futEl = shortWait.until(ExpectedConditions.visibilityOfElementLocated(futurePG));

	        // read values (then poll briefly if either loads a moment later)
	        shortWait.until(d ->
	            !String.valueOf(curEl.getDomProperty("value")).trim().isEmpty() ||
	            !String.valueOf(futEl.getDomProperty("value")).trim().isEmpty()
	        );

	        String current = String.valueOf(curEl.getDomProperty("value")).trim();
	        String future  = String.valueOf(futEl.getDomProperty("value")).trim();

	        System.out.println("Current Product Group = '" + current + "', Future Product Group = '" + future + "'");
	        Assert.assertTrue(!current.isEmpty() || !future.isEmpty(),
	                "❌ Both Current Product Group and Future Product Group are empty.");
	    }

	    // ─────────────────────────────────────────────────────────────────────────────
	    // RULE 5: Publish USD List Price is null
	    // ─────────────────────────────────────────────────────────────────────────────
	    @Test(priority = 5)
	    public void test5_publishUsdListPriceIsNull() {
	        // Ensure correct entity
	        selectEntity("003 List Pricing");

	        // Close any open overlay
	        try {
	            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
	            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.cdk-overlay-pane")));
	        } catch (Exception ignored) {}

	        // Expand all panels
	        expandAllPanels();

	        // Robust, label-anchored attempts
	        By[] locators = new By[] {
	            By.xpath("//sp-numeric[.//*[contains(normalize-space(),'Publish') and contains(normalize-space(),'USD') and contains(normalize-space(),'List')]]//mat-form-field//input"),
	            By.xpath("//sp-numeric[.//*[contains(normalize-space(),'Publish') and contains(normalize-space(),'USD') and (contains(normalize-space(),'List Price') or contains(normalize-space(),'LP'))]]//mat-form-field//input"),
	            By.xpath("//sp-input[.//*[contains(normalize-space(),'Publish') and contains(normalize-space(),'USD') and contains(normalize-space(),'List')]]//input"),
	            By.xpath("//mat-form-field[.//*[contains(normalize-space(),'Publish') and contains(normalize-space(),'USD') and contains(normalize-space(),'List') and (contains(normalize-space(),'Price') or contains(normalize-space(),'LP'))]]//input"),
	            By.xpath("(//*[contains(normalize-space(),'Publish USD List Price') or contains(normalize-space(),'Publish USD LP')]/following::input)[1]")
	        };

	        WebElement input = null;
	        for (By by : locators) {
	            try {
	                input = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
	                break;
	            } catch (Exception ignored) {}
	        }

	        Assert.assertNotNull(input, "❌ Could not locate 'Publish USD List Price' input.");
	        js.executeScript("arguments[0].scrollIntoView({block:'center'});", input);

	        String value = input.getAttribute("value");
	        if (value == null || value.trim().isEmpty()) {
	            value = String.valueOf(input.getDomProperty("value"));
	        }
	        value = value == null ? "" : value.trim();

	        System.out.println("Publish USD List Price (should be null) = '" + value + "'");
	        Assert.assertTrue(value.isEmpty(), "❌ Expected 'Publish USD List Price' to be null/empty.");
	    }

	    // ─────────────────────────────────────────────────────────────────────────────
	    // RULE 6 (i–iv) using your component indices
	    // ─────────────────────────────────────────────────────────────────────────────
	    @Test(priority = 5)
	    public void test5_rule6_any_condition_satisfied() {
	        selectEntity("003 List Pricing");
	        expandAllPanels();

	        // From your absolutes: indices mapped to stable relatives
	        String lpOverwriteFlag    = readSelectByIndex(3);   // sp-lookup-select[3]
	        String dnAction           = readInputByIndex(11);   // sp-input[11]
	        String pvcAction          = readInputByIndex(13);   // sp-input[13]
	        String chinaAction        = readSelectByIndex(12);  // sp-lookup-select[12]

	        String pricingMgrApproval = readSelectByIndex(4);   // sp-lookup-select[4]
	        String approver1          = readSelectByIndex(5);   // sp-lookup-select[5]
	        String approver2          = readSelectByIndex(6);   // sp-lookup-select[6]
	        String approver3          = readSelectByIndex(7);   // sp-lookup-select[7]
	        String approver4          = readSelectByIndex(8);   // sp-lookup-select[8]

	        String currentLocalLP;
	        try {
	            currentLocalLP = driver.findElement(
	                By.xpath("//sp-input[.//*[contains(normalize-space(),'Current Local Currency') and (contains(normalize-space(),'List Price') or contains(normalize-space(),'LP'))]]//input")
	            ).getAttribute("value");
	            if (currentLocalLP == null || currentLocalLP.trim().isEmpty()) {
	                currentLocalLP = String.valueOf(driver.findElement(
	                    By.xpath("//sp-input[.//*[contains(normalize-space(),'Current Local Currency') and (contains(normalize-space(),'List Price') or contains(normalize-space(),'LP'))]]//input")
	                ).getDomProperty("value"));
	            }
	            currentLocalLP = currentLocalLP == null ? "" : currentLocalLP.trim();
	        } catch (Exception e) {
	            currentLocalLP = "";
	        }

	        boolean noneEmergency     = !anyEmergency(dnAction, pvcAction, chinaAction);
	        boolean anyOneEmergency   =  anyEmergency(dnAction, pvcAction, chinaAction);
	        boolean approvals3        =  allYesAuto(pricingMgrApproval, approver1, approver2);
	        boolean approvals5        =  allYesAuto(pricingMgrApproval, approver1, approver2, approver3, approver4);

	        boolean cond_i   = isNo(lpOverwriteFlag) && noneEmergency;
	        boolean cond_ii  = isNo(lpOverwriteFlag) && anyOneEmergency && approvals3;
	        boolean cond_iiia = isNullOrNone(lpOverwriteFlag)
	                && allNullOrNone(dnAction, pvcAction, chinaAction)
	                && isNullOrNone(currentLocalLP);
	        boolean cond_iiib = isNullOrNone(lpOverwriteFlag)
	                && anyOneEmergency
	                && approvals3;
	        boolean cond_iii = cond_iiia || cond_iiib;
	        boolean cond_iv  = "yes".equalsIgnoreCase(N(lpOverwriteFlag)) && approvals5;

	        boolean allowed = cond_i || cond_ii || cond_iii || cond_iv;

	        System.out.println("Rule 6 → i:" + cond_i + ", ii:" + cond_ii + ", iii:" + cond_iii + " (a:" + cond_iiia + ", b:" + cond_iiib + "), iv:" + cond_iv);
	        Assert.assertTrue(allowed, "❌ Rule 6 not satisfied: none of (i–iv) hold for current form values.");
	    }

	    // ─────────────────────────────────────────────────────────────────────────────
	    // FULL ELIGIBILITY (Rules 1–6) + MOVEMENT (Future → Publish, then clear Future)
	    // ─────────────────────────────────────────────────────────────────────────────
	    @Test(priority = 6)
	    public void test6_eligibilityAndMovement() {
	        // 003 List Pricing — prerequisites
	        selectEntity("003 List Pricing");
	        expandAllPanels();

	        // Rule 1: Future USD LP not null (label-first; fallback to your index)
	        String futureUsdLP = readValueByLabel("Future USD List Price");
	        if (futureUsdLP.isEmpty()) {
	            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(
	                By.xpath("//sp-dynamic-form//sp-numeric[14]//mat-form-field//input")));
	            futureUsdLP = el.getAttribute("value");
	            if (futureUsdLP == null || futureUsdLP.trim().isEmpty())
	                futureUsdLP = String.valueOf(el.getDomProperty("value"));
	            futureUsdLP = futureUsdLP == null ? "" : futureUsdLP.trim();
	        }
	        Assert.assertFalse(futureUsdLP.isEmpty(), "❌ Rule 1 failed: Future USD List Price is null.");

	        // Rule 2: Future Local Currency LP Effective Date not null
	        String futureLCLPEffDate = readValueByLabel("Future Local Currency LP Effective Date");
	        if (futureLCLPEffDate.isEmpty()) futureLCLPEffDate = readValueByLabel("Future Local Currency LP Eff Date");
	        Assert.assertFalse(futureLCLPEffDate.isEmpty(), "❌ Rule 2 failed: Future Local Currency LP Effective Date is null.");

	        // Rule 4: any of current/future product group not null (your locators)
	        WebElement curEl = wait.until(ExpectedConditions.visibilityOfElementLocated(
	            By.xpath("(//*[normalize-space()='Product Group Manager']/following::input[@type='text'])[6]")));
	        WebElement futEl = wait.until(ExpectedConditions.visibilityOfElementLocated(
	            By.xpath("(//*[normalize-space()='Note for PG overwrite']/following::input[@type='text'])[8]")));
	        String currentPG = String.valueOf(curEl.getDomProperty("value")).trim();
	        String futurePG  = String.valueOf(futEl.getDomProperty("value")).trim();
	        Assert.assertTrue(!currentPG.isEmpty() || !futurePG.isEmpty(),
	                "❌ Rule 4 failed: Both Current Product Group and Future Product Group are empty.");

	        // Rule 5: Publish USD LP is null
	        String publishUsdLP = readValueByLabel("Publish USD List Price");
	        if (publishUsdLP.isEmpty()) publishUsdLP = readValueByLabel("Publish USD LP");
	        Assert.assertTrue(publishUsdLP.isEmpty(), "❌ Rule 5 failed: Publish USD List Price should be null before movement.");

	        // Rule 3: Approval = Yes in 007 Pricing Action
	        selectEntity("007 Pricing Action");
	        String approval;
	        try {
	            WebElement triggerVal = wait.until(ExpectedConditions.visibilityOfElementLocated(
	                By.xpath("(//*[normalize-space()='Approval']/following::div[contains(@class,'mat-mdc-select-value')][1]//span)[1]")));
	            approval = triggerVal.getText().trim();
	            if (approval.isEmpty()) throw new RuntimeException("empty");
	        } catch (Exception e) {
	            WebElement trigger = wait.until(ExpectedConditions.elementToBeClickable(
	                By.xpath("//*[normalize-space()='Approval']/following::div[contains(@class,'mat-mdc-select-trigger')][1]")));
	            trigger.click();
	            WebElement selected = wait.until(ExpectedConditions.visibilityOfElementLocated(
	                By.xpath("//div[contains(@class,'cdk-overlay-pane')]//mat-option[@aria-selected='true' or contains(@class,'mat-selected') or contains(@class,'mdc-list-item--selected')]//span")));
	            approval = selected.getText().trim();
	            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
	            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.cdk-overlay-pane")));
	        }
	        Assert.assertEquals(approval, "Yes", "❌ Rule 3 failed: Approval in 007 Pricing Action is not 'Yes'.");

	        // Back to 003 List Pricing for Rule 6 + movement
	        selectEntity("003 List Pricing");
	        expandAllPanels();

	        String lpOverwriteFlag    = readSelectByIndex(3);
	        String dnAction           = readInputByIndex(11);
	        String pvcAction          = readInputByIndex(13);
	        String chinaAction        = readSelectByIndex(12);
	        String pricingMgrApproval = readSelectByIndex(4);
	        String approver1          = readSelectByIndex(5);
	        String approver2          = readSelectByIndex(6);
	        String approver3          = readSelectByIndex(7);
	        String approver4          = readSelectByIndex(8);

	        boolean noneEmergency   = !anyEmergency(dnAction, pvcAction, chinaAction);
	        boolean anyOneEmergency =  anyEmergency(dnAction, pvcAction, chinaAction);
	        boolean approvals3      =  allYesAuto(pricingMgrApproval, approver1, approver2);
	        boolean approvals5      =  allYesAuto(pricingMgrApproval, approver1, approver2, approver3, approver4);

	        boolean cond_i   = isNo(lpOverwriteFlag) && noneEmergency;
	        boolean cond_ii  = isNo(lpOverwriteFlag) && anyOneEmergency && approvals3;
	        boolean cond_iiia = isNullOrNone(lpOverwriteFlag)
	                && allNullOrNone(dnAction, pvcAction, chinaAction)
	                && isNullOrNone(readValueByLabel("Current Local Currency List Price"));
	        boolean cond_iiib = isNullOrNone(lpOverwriteFlag) && anyOneEmergency && approvals3;
	        boolean cond_iii  = cond_iiia || cond_iiib;
	        boolean cond_iv   = "yes".equalsIgnoreCase(N(lpOverwriteFlag)) && approvals5;

	        boolean allowedRule6 = cond_i || cond_ii || cond_iii || cond_iv;
	        Assert.assertTrue(allowedRule6,
	            "❌ Rule 6 failed: none of (i–iv) satisfied. i=" + cond_i + ", ii=" + cond_ii + ", iii=" + cond_iii + " (a=" + cond_iiia + ", b=" + cond_iiib + "), iv=" + cond_iv);

	        // Movement: Future → Publish, then clear Future
	        setValueByLabel("Publish USD List Price", futureUsdLP);
	        setValueByLabel("Publish USD LP Effective Date", futureLCLPEffDate);
	        clearByLabel("Future USD List Price");
	        clearByLabel("Future Local Currency LP Effective Date");

	        // Verify
	        String publishUsdLP_new      = readValueByLabel("Publish USD List Price");
	        String publishUsdLPEff_new   = readValueByLabel("Publish USD LP Effective Date");
	        String futureUsdLP_after     = readValueByLabel("Future USD List Price");
	        String futureLCLPEff_after   = readValueByLabel("Future Local Currency LP Effective Date");

	        Assert.assertEquals(publishUsdLP_new, futureUsdLP, "❌ Publish USD List Price not moved from Future.");
	        Assert.assertEquals(publishUsdLPEff_new, futureLCLPEffDate, "❌ Publish USD LP Effective Date not moved from Future.");
	        Assert.assertTrue(isNullOrNone(futureUsdLP_after), "❌ Future USD List Price not cleared.");
	        Assert.assertTrue(isNullOrNone(futureLCLPEff_after), "❌ Future Local Currency LP Effective Date not cleared.");
	    }

	    // ─────────────────────────────────────────────────────────────────────────────
	    // Helpers (string norms + rule utils)
	    // ─────────────────────────────────────────────────────────────────────────────
	    private String N(String v){ return v == null ? "" : v.trim(); }
	    private boolean isNullOrNone(String v){
	        String x = N(v);
	        return x.isEmpty() || x.equalsIgnoreCase("null") || x.equalsIgnoreCase("none");
	    }
	    private boolean isYesAuto(String v){
	        String x = N(v).toLowerCase();
	        return x.equals("yes") || x.equals("auto");
	    }
	    private boolean isNo(String v){ return N(v).equalsIgnoreCase("no"); }
	    private boolean isEmergency(String v){ return N(v).equalsIgnoreCase("emergency"); }
	    private boolean anyEmergency(String... xs){
	        for (String x: xs) if (isEmergency(x)) return true;
	        return false;
	    }
	    private boolean allYesAuto(String... xs){
	        for (String x: xs) if (!isYesAuto(x)) return false;
	        return true;
	    }
	    private boolean allNullOrNone(String... xs){
	        for (String x: xs) if (!isNullOrNone(x)) return false;
	        return true;
	    }

	    // ─────────────────────────────────────────────────────────────────────────────
	    // Generic, label-anchored helpers (inputs & panels)
	    // ─────────────────────────────────────────────────────────────────────────────
	    private void expandAllPanels() {
	        for (WebElement hdr : driver.findElements(By.cssSelector("mat-expansion-panel .mat-expansion-panel-header"))) {
	            try {
	                if (!"true".equals(hdr.getAttribute("aria-expanded"))) {
	                    hdr.click();
	                    wait.until(ExpectedConditions.attributeToBe(hdr, "aria-expanded", "true"));
	                }
	            } catch (Exception ignored) {}
	        }
	    }

	    private WebElement findInputByLabel(String label) {
	        By[] tries = new By[] {
	            By.xpath("//sp-numeric[.//*[contains(normalize-space(),'" + label + "')]]//input"),
	            By.xpath("//sp-input[.//*[contains(normalize-space(),'" + label + "')]]//input"),
	            By.xpath("//sp-date-picker[.//*[contains(normalize-space(),'" + label + "')]]//input"),
	            By.xpath("//mat-form-field[.//*[contains(normalize-space(),'" + label + "')]]//input"),
	            By.xpath("(//*[contains(normalize-space(),'" + label + "')]/following::input)[1]")
	        };
	        for (By by : tries) {
	            try {
	                WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
	                js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
	                return el;
	            } catch (Exception ignored) {}
	        }
	        throw new org.openqa.selenium.NoSuchElementException("Input not found for label: " + label);
	    }

	    private String readValueByLabel(String label) {
	        WebElement el = findInputByLabel(label);
	        String v = el.getAttribute("value");
	        if (v == null || v.trim().isEmpty()) v = String.valueOf(el.getDomProperty("value"));
	        return v == null ? "" : v.trim();
	    }

	    private void setValueByLabel(String label, String value) {
	        WebElement el = findInputByLabel(label);
	        el.click();
	        el.sendKeys(Keys.chord(Keys.CONTROL, "a"));
	        el.sendKeys(value == null ? "" : value);
	        el.sendKeys(Keys.TAB);
	    }

	    private void clearByLabel(String label) {
	        WebElement el = findInputByLabel(label);
	        el.click();
	        el.sendKeys(Keys.chord(Keys.CONTROL, "a"));
	        el.sendKeys(Keys.DELETE);
	        el.sendKeys(Keys.TAB);
	    }

	    // ─────────────────────────────────────────────────────────────────────────────
	    // Your earlier label-readers (kept, used in some tests)
	    // ─────────────────────────────────────────────────────────────────────────────
	    private String readSelectByLabel(String label) {
	        By valueInTrigger = By.xpath(
	            "(//*[contains(normalize-space(),'" + label + "')]" +
	            "/following::div[contains(@class,'mat-mdc-select-value')][1]//span)[1]"
	        );
	        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(valueInTrigger));
	        js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
	        return N(el.getText());
	    }

	    private String readInputByLabel(String label) {
	        By[] tries = new By[] {
	            By.xpath("//mat-form-field[.//*[contains(normalize-space(),'" + label + "')]]//input"),
	            By.xpath("//sp-input[.//*[contains(normalize-space(),'" + label + "')]]//input"),
	            By.xpath("//sp-numeric[.//*[contains(normalize-space(),'" + label + "')]]//input"),
	            By.xpath("//sp-date-picker[.//*[contains(normalize-space(),'" + label + "')]]//input"),
	            By.xpath("(//*[contains(normalize-space(),'" + label + "')]/following::input)[1]")
	        };
	        WebElement el = null;
	        for (By by : tries) {
	            try { el = wait.until(ExpectedConditions.visibilityOfElementLocated(by)); break; }
	            catch (Exception ignored) {}
	        }
	        if (el == null) throw new org.openqa.selenium.NoSuchElementException("Input not found for label: " + label);
	        js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
	        String v = el.getAttribute("value");
	        if (v == null || v.trim().isEmpty()) v = String.valueOf(el.getDomProperty("value"));
	        return N(v);
	    }

	    // ─────────────────────────────────────────────────────────────────────────────
	    // Index-based readers built from your absolute XPaths (stable relative forms)
	    // ─────────────────────────────────────────────────────────────────────────────
	    private String readSelectByIndex(int idx) {
	        By boxBy = By.xpath("//sp-dynamic-form//sp-lookup-select[" + idx + "]");
	        WebElement box = wait.until(ExpectedConditions.presenceOfElementLocated(boxBy));
	        js.executeScript("arguments[0].scrollIntoView({block:'center'});", box);

	        // Common Angular Material value containers
	        By[] valueTries = new By[] {
	            By.xpath(".//div[contains(@class,'mat-mdc-select-value')]"),
	            By.xpath(".//span[contains(@class,'mat-mdc-select-min-line')]"),
	            By.xpath(".//span[contains(@class,'mat-mdc-select-value-text')]")
	        };
	        for (By by : valueTries) {
	            try {
	                WebElement vEl = box.findElement(by);
	                String txt = vEl.getText().trim();
	                if (!txt.isEmpty()) return txt;
	            } catch (Exception ignored) {}
	        }

	        // Fallback: open and read selected, then close
	        try {
	            WebElement trigger = box.findElement(By.xpath(".//div[contains(@class,'mat-mdc-select-trigger') or @role='combobox']"));
	            trigger.click();
	            WebElement selected = wait.until(ExpectedConditions.visibilityOfElementLocated(
	                By.xpath("//div[contains(@class,'cdk-overlay-pane')]//mat-option[@aria-selected='true' or contains(@class,'mat-selected') or contains(@class,'mdc-list-item--selected')]//span[1]")));
	            String txt = selected.getText().trim();
	            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
	            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.cdk-overlay-pane")));
	            if (!txt.isEmpty()) return txt;
	        } catch (Exception ignored) {}

	        return "";
	    }

	    private String readInputByIndex(int idx) {
	        By by = By.xpath("//sp-dynamic-form//sp-input[" + idx + "]//input");
	        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
	        js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
	        String v = el.getAttribute("value");
	        if (v == null || v.trim().isEmpty()) v = String.valueOf(el.getDomProperty("value"));
	        return v == null ? "" : v.trim();
	    }

	    @AfterClass
	    public void afterclass() {
	        if (driver != null) driver.quit();
	    }
	}



