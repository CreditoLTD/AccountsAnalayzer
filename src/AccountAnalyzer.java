import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class AccountAnalyzer {

	XSSFWorkbook wb = null;

	BankAccount account = null;

	Parser accountParser = null;

	public AccountAnalyzer(String fileName, String bankName) {

		XSSFSheet sheet = null;

		try {
			wb = new XSSFWorkbook(fileName);
			sheet = wb.getSheetAt(0);

			account = new BankAccount(bankName);
			accountParser = new Parser(sheet, account);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				wb.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public void analyze() {

		accountParser.parse();
		
	}
}
