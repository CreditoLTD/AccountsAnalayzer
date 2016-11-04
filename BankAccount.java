import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class BankAccount {
	
	// Permanent column for each hebrew type of transaction
	private static final int ENGLISHTYPECOLUMN = 1;
	
	
	ArrayList<Transaction> transactions;
	
	// A map for classifying the transactions according to their type
	Map<String, ArrayList<Transaction>> classified;

	// List for all red Flags in the account
	ArrayList<String> redFlags = new ArrayList<String>();

	// The current balance of the account
	double balance = 0;

	String name;
	
	/**
	 * Constructor
	 * 
	 * @throws IOException
	 */
	public BankAccount(String name) throws IOException {

		this.name = name;

		transactions = new ArrayList<>();
		
		classified = new HashMap<String, ArrayList<Transaction>>();

		InitializeTransactionMaps();

	}

	/**
	 * Method that initialize the mapping between a type of transaction to the
	 * transactions classified to it
	 * 
	 * @return
	 * @throws IOException 
	 */
	private void InitializeTransactionMaps() throws IOException {
		
		XSSFWorkbook wb = new XSSFWorkbook("Checking_Account_Analysis.xlsx");
		// Gets the second sheet on the xlsx of transactions where the name of
		// all types existed
		XSSFSheet sheet = wb.getSheetAt(1);

		XSSFRow row;

		classified.put("Nonclassified", new ArrayList<Transaction>());

		int rows; // Number of rows
		rows = sheet.getPhysicalNumberOfRows();

		for (int r = 1; r < rows; r++) {

			row = sheet.getRow(r);

			String type = row.getCell(ENGLISHTYPECOLUMN).toString().trim();

			// Add the keys which are the type of transactions and the start
			// value is null
			classified.put(type, new ArrayList<Transaction>());
		}
		
		wb.close();

	}

}
