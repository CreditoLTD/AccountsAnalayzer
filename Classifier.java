import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Classifier {

	// Permanent column for each name of transaction
	private static final int NAMECOLUMN = 0;

	// Permanent column for each name of transaction
	private static final int REFCOLUMN = 1;

	// Permanent column for each type of transaction
	private static final int TYPECOLUMN = 3;

	// Permanent column for each english type of transaction
	private static final int SECONDSHEETENGLISHTYPECOLUMN = 1;

	// Permanent column for each hebrew type of transaction
	private static final int SECONDSHEETHEBREWTYPECOLUMN = 0;

	// Mapping from a name of transaction to its type
	Map<String, String> nameToType;

	// Mapping from a name of transaction to its type
	Map<String, String> refToType;

	public Classifier() throws IOException {

		XSSFWorkbook wb = new XSSFWorkbook("Checking_Account_Analysis.xlsx");
		// Gets first sheet
		XSSFSheet firstSheet = wb.getSheetAt(0);
		// Gets the second sheet on the xlsx of transactions where the name of
		// all types existed
		XSSFSheet secondSheet = wb.getSheetAt(1);

		nameToType = new HashMap<String, String>();

		refToType = new HashMap<String, String>();

		Map<String, String> hebrewToEnglish = generateTransactionsClassifier(secondSheet);

		createNameAndRefToTypeMap(firstSheet, hebrewToEnglish);

		wb.close();

	}

	/**
	 * Classify a transaction according to its name if existed in database, if
	 * not trying to classify it by its ref if existed in data base, else
	 * classify it as non classified
	 * 
	 * @param transaction
	 * @param account
	 */
	public void classify(Transaction transaction, BankAccount account) {
		if (transaction != null) {
			// Trying to classify the transaction by its name
			String transactionType = nameToType.get(transaction.name);
			if (transactionType == null) {
				// Trying to classify the transaction by its ref
				transactionType = refToType.get(transaction.ref);
				if (transactionType == null)
					transactionType = "Nonclassified";
			}
			
			// Enter transaction to a list of transactions and to its type
			account.transactions.add(transaction);
			account.classified.get(transactionType).add(new Transaction(transaction));
		}
	}

	/**
	 * Method that creating the mapping between a name of transaction to its
	 * type
	 * 
	 * @param hebrewToEnglish
	 */
	private void createNameAndRefToTypeMap(XSSFSheet firstSheet, Map<String, String> hebrewToEnglish) {

		XSSFRow row;

		int rows; // Number of rows
		rows = firstSheet.getPhysicalNumberOfRows();

		for (int r = 2; r < rows; r++) {

			row = firstSheet.getRow(r);

			String name = row.getCell(NAMECOLUMN).toString();
			String ref = row.getCell(REFCOLUMN).toString().trim();
			String type = row.getCell(TYPECOLUMN).toString();
			// Adding a map from a specific name to its type
			nameToType.put(name, hebrewToEnglish.get(type));
			if (!ref.equals("משתנה"))
				refToType.put(ref, hebrewToEnglish.get(type));
		}

	}

	/**
	 * Method that generates the classifier map and the mapping between hebrew
	 * type to english type
	 * 
	 * @return
	 */
	private Map<String, String> generateTransactionsClassifier(XSSFSheet sheet) {

		XSSFRow row;

		Map<String, String> hebrewToEnglish = new HashMap<String, String>();

		int rows; // Number of rows
		rows = sheet.getPhysicalNumberOfRows();

		for (int r = 1; r < rows; r++) {

			row = sheet.getRow(r);

			String hebrewType = row.getCell(SECONDSHEETHEBREWTYPECOLUMN).toString().trim();
			String englishType = row.getCell(SECONDSHEETENGLISHTYPECOLUMN).toString().trim();

			// Add the keys and values for mapping from a type in hebrew to type
			// in english
			hebrewToEnglish.put(hebrewType, englishType);
		}

		return hebrewToEnglish;

	}

}
