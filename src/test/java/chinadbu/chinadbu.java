package chinadbu;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;            // ✅ missing import
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import java.io.File;
import java.io.OutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

public class chinadbu {
    WebDriver driver;
    WebDriverWait wait;
    JavascriptExecutor js;
    private Process ffmpegProcess;
    private Path videoPath;
    private Path ffmpegLogPath;

    // Use -Dffmpeg="C:\\ffmpeg\\bin\\ffmpeg.exe" to override
    private final String FFMPEG_BIN = System.getProperty("ffmpeg", "ffmpeg");
    
    @BeforeClass
    public void setup() {
        // ---- start FFmpeg recording FIRST (before tests) ----
        try {
            Path outDir = Paths.get(System.getProperty("user.dir"), "target", "videos");
            Files.createDirectories(outDir);

            String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            videoPath     = outDir.resolve("chinadbu_" + stamp + ".mp4");
            ffmpegLogPath = outDir.resolve("ffmpeg_" + stamp + ".log");

            // Full desktop capture (Windows) via gdigrab
            // If you want a smaller file, lower framerate (e.g., 20) or add "-crf", "28"
            ProcessBuilder pb = new ProcessBuilder(
                FFMPEG_BIN,
                "-y",
                "-f", "gdigrab",
                "-framerate", "30",
                "-i", "desktop",
                "-vcodec", "libx264",
                "-preset", "ultrafast",
                "-pix_fmt", "yuv420p",
                videoPath.toString()
            );
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(ffmpegLogPath.toFile()));

            ffmpegProcess = pb.start();
            // tiny delay helps FFmpeg spin up before browser opens
            Thread.sleep(800);
        } catch (Exception e) {
            System.err.println("⚠ FFmpeg not started (recording disabled): " + e.getMessage());
            ffmpegProcess = null;
        }

        // ---- your existing WebDriver setup (unchanged) ----
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
        // If you must enforce non-empty, keep the assert; else comment it out.
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
    
 // EDITED: Rule 5 — Publish USD List Price must be NULL before movement
    @Test(priority = 5)
    public void test5_publishUsdListPriceIsNull() {
        selectEntity("003 List Pricing");

        // Close overlays & expand all panels so the field is present in DOM
        try { new Actions(driver).sendKeys(Keys.ESCAPE).perform(); } catch (Exception ignored) {}
        try { wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.cdk-overlay-pane"))); } catch (Exception ignored) {}
        try {
            for (WebElement hdr : driver.findElements(By.cssSelector("mat-expansion-panel .mat-expansion-panel-header"))) {
                if (!"true".equals(hdr.getAttribute("aria-expanded"))) {
                    hdr.click();
                    wait.until(ExpectedConditions.attributeToBe(hdr, "aria-expanded", "true"));
                }
            }
        } catch (Exception ignored) {}

        // --- 1) Primary label-anchored input tries ---
        By[] inputTries = new By[] {
            // sp-numeric with Publish + USD + (List Price|LP)
            By.xpath("//sp-numeric[.//*[contains(normalize-space(),'Publish') and contains(normalize-space(),'USD') and (contains(normalize-space(),'List Price') or contains(normalize-space(),'LP'))]]//input"),
            // sp-input variant
            By.xpath("//sp-input[.//*[contains(normalize-space(),'Publish') and contains(normalize-space(),'USD') and contains(normalize-space(),'List')]]//input"),
            // mat-form-field anchored by label
            By.xpath("//mat-form-field[.//*[contains(normalize-space(),'Publish') and contains(normalize-space(),'USD') and (contains(normalize-space(),'List Price') or contains(normalize-space(),'LP'))]]//input"),
            // first input after any matching label text
            By.xpath("(//*[contains(normalize-space(),'Publish') and contains(normalize-space(),'USD') and (contains(normalize-space(),'List') or contains(normalize-space(),'LP'))]/following::input)[1]")
        };

        WebElement field = null;
        for (By by : inputTries) {
            try {
                field = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
                break;
            } catch (Exception ignored) {}
        }

        // --- 2) If not found, scan nearby sp-numeric blocks (10..20) by their CONTAINER text ---
        if (field == null) {
            for (int i = 10; i <= 20; i++) {
                try {
                    WebElement container = driver.findElement(By.xpath("//sp-dynamic-form//sp-numeric[" + i + "]"));
                    String text = container.getText();
                    if (text != null) {
                        String t = text.toLowerCase();
                        if (t.contains("publish") && t.contains("usd") && (t.contains("list") || t.contains("lp"))) {
                            // found the container; try to grab its input (or value span)
                            try {
                                field = container.findElement(By.xpath(".//mat-form-field//input"));
                            } catch (Exception ignoredInner) {
                                // readonly/value fallback inside the same container
                                try {
                                    field = container.findElement(By.xpath(".//*[self::span or self::div][contains(@class,'value') or contains(@class,'mat-mdc-form-field-infix')][1]"));
                                } catch (Exception ignoredInner2) {}
                            }
                            if (field != null) break;
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        // --- 3) Last-resort non-input fallback near the label (readonly displays) ---
        if (field == null) {
            By readonlyTry = By.xpath(
                "(//*[contains(normalize-space(),'Publish') and contains(normalize-space(),'USD') and (contains(normalize-space(),'List') or contains(normalize-space(),'LP'))]" +
                "/following::*[self::span or self::div][contains(@class,'value') or contains(@class,'mat-mdc-form-field-infix')][1])"
            );
            try {
                field = wait.until(ExpectedConditions.visibilityOfElementLocated(readonlyTry));
            } catch (Exception ignored) {}
        }

        Assert.assertNotNull(field, "\"Publish USD List Price is null ");

        // Read current value (input vs. non-input)
        String value;
        if ("input".equalsIgnoreCase(field.getTagName())) {
            value = field.getAttribute("value");
            if (value == null || value.trim().isEmpty()) value = String.valueOf(field.getDomProperty("value"));
        } else {
            value = field.getText();
        }
        value = value == null ? "" : value.trim();

        System.out.println("Publish USD List Price (expected null) = '" + value + "'");
        Assert.assertTrue(value.isEmpty(), "❌ Expected 'Publish USD List Price' to be null/empty.");
    }

 // -------- minimal helpers used by the test --------
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

    // Read a mat-select's displayed value by label (kept simple & robust)
    private String readSelectByLabel(String label) {
        // value visible in trigger
        By valueInTrigger = By.xpath(
            "(//*[contains(normalize-space(),'" + label + "')]" +
            "/following::div[contains(@class,'mat-mdc-select-value')][1]//span)[1]"
        );
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(valueInTrigger));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        return N(el.getText());
    }

    // Read an input's value by label (text/number/datepicker)
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


    @Test(priority = 5)
    public void test5_rule6_any_condition_satisfied() {
        // Ensure right view & panel open (same pattern as your tests)
        selectEntity("003 List Pricing");
        try {
            WebElement header = driver.findElement(By.cssSelector("mat-expansion-panel .mat-expansion-panel-header"));
            if (header != null && !"true".equals(header.getAttribute("aria-expanded"))) {
                header.click();
                wait.until(ExpectedConditions.attributeToBe(header, "aria-expanded", "true"));
            }
        } catch (Exception ignored) {}

        // ---- READ using stable, index-based relatives (from your absolute XPaths) ----
        // LP Overwrite Flag  => sp-lookup-select[3]
        String lpOverwriteFlag    = readSelectByIndex(3);

        // DN Pricing Action  => sp-input[11]
        String dnAction           = readInputByIndex(11);

        // PVC Pricing Action => sp-input[13]
        String pvcAction          = readInputByIndex(13);

        // China Pricing Action => sp-lookup-select[12]
        String chinaAction        = readSelectByIndex(12);

        // Pricing Manager Approval Status => sp-lookup-select[4]
        String pricingMgrApproval = readSelectByIndex(4);

        // Approver 1..4 => sp-lookup-select[5..8]
        String approver1          = readSelectByIndex(5);
        String approver2          = readSelectByIndex(6);
        String approver3          = readSelectByIndex(7);
        String approver4          = readSelectByIndex(8);

        // Current Local Currency List Price — keep label-based (no index provided)
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
            currentLocalLP = ""; // if not present, treat as null/empty
        }

        // ---- NORMALIZE/HELPERS ----
        java.util.function.Function<String,String> N = s -> s == null ? "" : s.trim();
        java.util.function.Predicate<String> isNo   = s -> N.apply(s).equalsIgnoreCase("no");
        java.util.function.Predicate<String> isNullOrNone = s -> {
            String x = N.apply(s);
            return x.isEmpty() || x.equalsIgnoreCase("null") || x.equalsIgnoreCase("none");
        };
        java.util.function.Predicate<String> isEmergency = s -> N.apply(s).equalsIgnoreCase("emergency");
        java.util.function.Predicate<String> isYesAuto = s -> {
            String x = N.apply(s).toLowerCase();
            return x.equals("yes") || x.equals("auto");
        };
        java.util.function.Function<String[],Boolean> anyEmergency = arr -> {
            for (String x : arr) if (isEmergency.test(x)) return true;
            return false;
        };
        java.util.function.Function<String[],Boolean> allYesAuto = arr -> {
            for (String x : arr) if (!isYesAuto.test(x)) return false;
            return true;
        };
        java.util.function.Function<String[],Boolean> allNullOrNone = arr -> {
            for (String x : arr) if (!isNullOrNone.test(x)) return false;
            return true;
        };

        // ---- EVALUATE RULE 6 (i–iv) ----
        boolean noneEmergency     = !anyEmergency.apply(new String[]{dnAction, pvcAction, chinaAction});
        boolean anyOneEmergency   =  anyEmergency.apply(new String[]{dnAction, pvcAction, chinaAction});
        boolean approvals3        =  allYesAuto.apply(new String[]{pricingMgrApproval, approver1, approver2});
        boolean approvals5        =  allYesAuto.apply(new String[]{pricingMgrApproval, approver1, approver2, approver3, approver4});

        // i) LP Overwrite Flag = No AND all three actions NOT Emergency
        boolean cond_i   = isNo.test(lpOverwriteFlag) && noneEmergency;

        // ii) LP Overwrite Flag = No AND any one action Emergency AND 3 approvals Yes/Auto
        boolean cond_ii  = isNo.test(lpOverwriteFlag) && anyOneEmergency && approvals3;

        // iii) LP Overwrite Flag = Null AND (a OR b)
        boolean cond_iiia = isNullOrNone.test(lpOverwriteFlag)
                && allNullOrNone.apply(new String[]{dnAction, pvcAction, chinaAction})
                && isNullOrNone.test(currentLocalLP);

        boolean cond_iiib = isNullOrNone.test(lpOverwriteFlag)
                && anyOneEmergency
                && approvals3;

        boolean cond_iii = cond_iiia || cond_iiib;

        // iv) LP Overwrite Flag = Yes AND all 5 approvals Yes/Auto
        boolean cond_iv  = N.apply(lpOverwriteFlag).equalsIgnoreCase("yes") && approvals5;

        boolean allowed = cond_i || cond_ii || cond_iii || cond_iv;

        System.out.println("Rule 6 → i:" + cond_i + ", ii:" + cond_ii + ", iii:" + cond_iii + " (a:" + cond_iiia + ", b:" + cond_iiib + "), iv:" + cond_iv);
        Assert.assertTrue(allowed, "❌ Rule 6 not satisfied: none of (i–iv) hold for current form values.");
    }

 
 // Read a mat-select displayed value using sp-lookup-select[index]
    private String readSelectByIndex(int idx) {
        By boxBy = By.xpath("//sp-dynamic-form//sp-lookup-select[" + idx + "]");
        WebElement box = wait.until(ExpectedConditions.presenceOfElementLocated(boxBy));
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", box);

        // Try common Angular Material value containers
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

        // Fallback: open overlay and read selected option, then close
        try {
            WebElement trigger = box.findElement(By.xpath(".//div[contains(@class,'mat-mdc-select-trigger') or @role='combobox']"));
            trigger.click();
            WebElement selected = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'cdk-overlay-pane')]//mat-option[@aria-selected='true' or contains(@class,'mat-selected') or contains(@class,'mdc-list-item--selected')]//span[1]")));
            String txt = selected.getText().trim();
            new org.openqa.selenium.interactions.Actions(driver).sendKeys(Keys.ESCAPE).perform();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.cdk-overlay-pane")));
            if (!txt.isEmpty()) return txt;
        } catch (Exception ignored) {}

        return "";
    }

    // Read an input value using sp-input[index]
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
        try {
            if (driver != null) driver.quit();
        } finally {
            // ---- stop FFmpeg recording LAST ----
            if (ffmpegProcess != null) {
                try {
                    // FFmpeg stops cleanly when it receives 'q' on stdin
                    OutputStream os = ffmpegProcess.getOutputStream();
                    os.write('q');
                    os.flush();
                } catch (IOException ignored) {}

                try {
                    if (!ffmpegProcess.waitFor(5, TimeUnit.SECONDS)) {
                        ffmpegProcess.destroy();
                        if (!ffmpegProcess.waitFor(3, TimeUnit.SECONDS)) {
                            ffmpegProcess.destroyForcibly();
                        }
                    }
                } catch (InterruptedException ie) {
                    ffmpegProcess.destroyForcibly();
                    Thread.currentThread().interrupt();
                }

                System.out.println("🎥 Test run video: " + (videoPath == null ? "<unknown>" : videoPath.toAbsolutePath()));
                if (ffmpegLogPath != null && Files.exists(ffmpegLogPath)) {
                    System.out.println("📝 FFmpeg log: " + ffmpegLogPath.toAbsolutePath());
                }
            }
        }
    }

}
