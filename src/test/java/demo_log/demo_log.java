
//selenium test page
package demo_log;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;


public class demo_log {
	

	

	    private WebDriver driver;

	    @BeforeClass
	    public void setup() {
	    	WebDriverManager.chromedriver().setup();
	        driver = new ChromeDriver();
	        driver.manage().window().maximize();
	    
	      
	    }

	    @Test
	    public void testFetchFromTextBox() {
	        // Step 1: Navigate to a simple webpage with a textbox.
	        // We will use the Selenium Dev website's web form for this example.
	        driver.get("https://www.selenium.dev/selenium/web/web-form.html");
	        System.out.println("Navigated to the web form page.");

	        // Step 2: Locate the text input field.
	        // We are using 'By.name' to find the element, but you can also use other locators
	        // like By.id, By.xpath, or By.cssSelector.
	        WebElement myTextBox = driver.findElement(By.name("my-text"));

	        // Step 3: Define the text to enter into the textbox.
	        String textToEnter = "Hello, Selenium!";
	        
	        // Step 4: Clear the text field (good practice) and send the text.
	        myTextBox.clear();
	        myTextBox.sendKeys(textToEnter);
	        System.out.println("Entered text: '" + textToEnter + "' into the textbox.");

	        // Step 5: Fetch the text from the same textbox using the 'value' attribute.
	        // The 'value' attribute holds the current text content of an input field.
	        String fetchedText = myTextBox.getAttribute("value");
	        System.out.println("Fetched text from the textbox: '" + fetchedText + "'.");

	        // Step 6: Verify that the fetched text is what we entered.
	        Assert.assertEquals(fetchedText, textToEnter, "The fetched text does not match the entered text.");
	        System.out.println("Test passed: Fetched text matches the entered text.");
	    }

	    @Test
	    public void testFetchFromDropdown() {
	        // Step 1: Navigate to the web page.
	        driver.get("https://www.selenium.dev/selenium/web/web-form.html");
	        System.out.println("Navigated to the web form page.");

	        // Step 2: Locate the dropdown element. The dropdown is a <select> tag.
	        WebElement dropdownElement = driver.findElement(By.name("my-select"));

	        // Step 3: Create a Select object to interact with the dropdown.
	        Select dropdown = new Select(dropdownElement);

	       

	        // Step 5: Get the selected option and fetch its text.
	        // getFirstSelectedOption() returns the WebElement of the currently selected option.
	        WebElement selectedOption = dropdown.getFirstSelectedOption();
	        String fetchedText = selectedOption.getText();
	        System.out.println("Fetched text from the dropdown: '" + fetchedText + "'.");

	       }

	    @AfterClass
	    public void teardown() {
	        // Close the browser after the test is complete.
	       if (driver != null) {
	            driver.quit();
	    
	       }
	       }
}


