package test_login;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

public class test_log {
	WebDriver driver;
	WebDriverWait Wait;
	
	
	
@Test(dataProvider="logdata",dataProviderClass = dataprov.class)

public void test1(String name,String mail,String currentaddress,String permanentadress) {
	driver.get("https://demoqa.com/text-box");
	JavascriptExecutor js = (JavascriptExecutor) driver;
	js.executeScript(
	  "let ad = document.querySelector('iframe, .ad, .ads, .google_ads_iframe, .ad-container');" +
	  "if(ad) { ad.style.display = 'none'; }"
	);
	Wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='userName']"))).sendKeys(name);

	Wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='userEmail']"))).sendKeys(mail);
	Wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("currentAddress"))).sendKeys(currentaddress);
	Wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("permanentAddress"))).sendKeys(permanentadress);
	Wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("submit"))).click();
	WebElement nameField = driver.findElement(By.id("currentAddress"));
	String value = nameField.getAttribute("value");
	System.out.println("Textbox value: " + value);
	
}

@Test
public void test2() {
	
	driver.get("https://demoqa.com/select-menu");
	Wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='withOptGroup']"))).click();
	Wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='react-select-2-option-0-0']e"))).click();
	
	
}
@BeforeClass()

public void beforeclass() {
	WebDriverManager.chromedriver().setup();
	driver =new ChromeDriver();
	driver.manage().window().maximize();
	driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0)); 
	Wait = new WebDriverWait(driver, Duration.ofSeconds(15));

	//Wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys("gopika");
	//Wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password"))).sendKeys("Rainy$Nights2Read");
	//Wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='kc-login']"))).click();
	//Wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='text-white mt-1']")));
	//System.out.println("my current title is " + driver.getTitle());
	
	
}

@AfterClass()

public void afterclass() {
	
	//driver.quit();
	
}
}
