package login;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.testng.annotations.DataProvider;

public class dataprovider {
	@DataProvider
	public String[][] getdata()throws Exception {
	File file = new File("C:\\Users\\aleena.j_simadvisory\\Documents\\selenium\\test1.xls");
	System.out.println(file.exists());
	FileInputStream fis = new FileInputStream(file);
	HSSFWorkbook workbook =new HSSFWorkbook(fis);
	HSSFSheet sheet = workbook.getSheetAt(0);
	int rowcount = sheet.getPhysicalNumberOfRows();
	int columnnumber =sheet.getRow(0).getLastCellNum();
	String[][] data =new String[rowcount-1][columnnumber];
	for(int i=0;i<rowcount-1;i++) {
		for(int j =0;j<columnnumber;j++) {
			DataFormatter df =new DataFormatter();		
			data[i][j] = df.formatCellValue(sheet.getRow(i+1).getCell(j));
			
			
			
		}
	
			
		}
	workbook.close();
	fis.close();
	
	return data;
	}
	
	
}
