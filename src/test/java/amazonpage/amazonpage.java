package amazonpage;

import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

public class amazonpage {

		

		    WebDriver driver;
		    WebDriverWait wait;
		    @BeforeClass
		    public void setup() {
		    	 WebDriverManager.chromedriver().setup();
		         driver = new ChromeDriver();                                      // init driver first
		         driver.manage().window().maximize();
		         wait = new WebDriverWait(driver, Duration.ofSeconds(20));         // then init wait
		         driver.get("https://www.amazon.in/");
		         System.out.println("Navigated to the web form page.");
		      
		    }

		    @Test
		    public void login() {
		    	
		    	
		    	driver.findElement(By.cssSelector("span[class='nav-line-2 ']")).click();
		    	wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='ap_email_login']"))).sendKeys("8606630007");
		    	driver.findElement(By.xpath("//input[@type='submit']")).click();
		    	wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#ap_password"))).sendKeys("Celine2211");
		    	
		    	driver.findElement(By.cssSelector("#signInSubmit")).click();
		    	
		    	
	
		    	driver.findElement(By.xpath("//a[normalize-space()='About Amazon']")).click();
		    	
		    	//a[normalize-space()='About Amazon']
		      }

/**
		    @Test
		    public void categories() {
		    	
		    	
		    	
		    	/**wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='ap_email_login']"))).sendKeys("8606630007");
		    	driver.findElement(By.xpath("//input[@type='submit']")).click();
		    	wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#ap_password"))).sendKeys("Celine2211");
		        
		    }
		   

		    @AfterClass
		    public void teardown() {
		        
		       if (driver != null) {
		          driver.quit();
		    
		       }**/
		       
	}




