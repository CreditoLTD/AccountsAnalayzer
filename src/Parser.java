import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class Parser {

	XSSFSheet sheet;

	BankAccount account;

	Map<String, Integer> parametersCols = new HashMap<String, Integer>();
	
	Classifier transactionsClassifier;
	
	public Parser(XSSFSheet sheet, BankAccount account) throws IOException {

		this.sheet = sheet;

		this.account = account;
		
		transactionsClassifier = new Classifier();

		createParameterMap();
		if (account.name.equals("בינלאומי"))
			findFirstBalance();
	}

	/**
	 * For some banks there is a starting balance in the sheet and here we
	 * detect it
	 */
	private void findFirstBalance() {

		int rows = sheet.getPhysicalNumberOfRows();

		Pattern p;
		Matcher m;

		int firstRowIndex = parametersCols.get("row");

		for (int r = firstRowIndex; r < rows; r++) {

			XSSFRow row = sheet.getRow(r);
			if (row != null) {
				// reading the current line and initialize a boolean variable to
				// know if the starting balance is in this row
				int cols = row.getPhysicalNumberOfCells();
				boolean isInRow = false;

				for (int c = 0; c < cols; c++) {

					XSSFCell cell = row.getCell(c);
					// trying to detect the cell that hints about the existance
					// of the starting balance
					if (cell != null) {
						p = Pattern.compile("יתרת *פתיחה", Pattern.UNICODE_CASE);
						m = p.matcher(cell.toString());
						if (m.find()) {
							isInRow = true;
							break;
						}
					}
				}
				// If the starting balance is in this row try to parse each cell
				// and thus updating the balance
				if (isInRow) {
					for (int c = 0; c < cols; c++) {
						XSSFCell cell = row.getCell(c);
						try {
							account.balance = Double.parseDouble(cell.toString().trim());
							break;
						} catch (Exception e) {

						}
					}
				}
			}
		}
	}

	public void parse() {
		// Transaction creation for later use
		Transaction transaction = new Transaction();

		int rows; // Number of rows
		rows = sheet.getPhysicalNumberOfRows();

		XSSFRow row;

		int firstRowIndex = parametersCols.get("row");

		setSheetInChronologicOrder();
		// Scan the rows one by one, detect and parse the rows that represents
		// transactions
		for (int r = firstRowIndex; r < rows; r++) {
			row = sheet.getRow(r);
			if (row != null) {
				// parsing the transaction
				transaction = transctionParser(row);
				// Classify the transaction
				transactionsClassifier.classify(transaction, account);
			}
		}
	}

	/**
	 * Method that checks if the sheet is in in chronologic order from top down,
	 * and if not change the order.
	 * 
	 */
	private void setSheetInChronologicOrder() {

		int firstRowIndex = parametersCols.get("row");
		if (isSheetInChronologicOrder())
			return;

		int numberOfrowsUp = sheet.getLastRowNum() - firstRowIndex;
		int lastRow = sheet.getLastRowNum();
		int numberOfRows = sheet.getPhysicalNumberOfRows();

		// Start from one row before the last and copy after one after the last,
		// and then do the same one by one till the first relevant row
		for (int i = sheet.getLastRowNum() - 1, j = 2; i >= firstRowIndex; i--, j = j + 2) {
			sheet.shiftRows(i, i, j);
		}

		// Delete all null rows
		for (int i = 0; i < numberOfRows; i++) {
			sheet.shiftRows(i + lastRow, i + lastRow, -numberOfrowsUp);
		}

	}

	/**
	 * Method checks if the sheet is in chronologic order according to the first
	 * and last dates we know
	 * 
	 * @return if the sheet is in Chronologic order
	 */
	@SuppressWarnings("deprecation")
	private boolean isSheetInChronologicOrder() {

		int dateIndex = parametersCols.get("date");
		int firstRowIndex = parametersCols.get("row");

		Date first;
		Date last;

		XSSFCell firstValidCell = null;
		XSSFCell lastValidCell = null;
		// Looking for the first row with valid date
		XSSFRow currentRow = sheet.getRow(firstRowIndex);
		if (currentRow != null)
			firstValidCell = sheet.getRow(firstRowIndex).getCell(dateIndex);
		while (firstValidCell == null || !isValidDate(firstValidCell.toString())) {
			firstRowIndex++;
			currentRow = sheet.getRow(firstRowIndex);
			if (currentRow != null)
				firstValidCell = sheet.getRow(firstRowIndex).getCell(dateIndex);
		}

		int lastRowIndex = sheet.getLastRowNum();
		// Looking for the last row with valid date
		currentRow = sheet.getRow(firstRowIndex);
		if (currentRow != null)
			lastValidCell = sheet.getRow(lastRowIndex).getCell(dateIndex);
		while (lastValidCell == null || !isValidDate(lastValidCell.toString())) {
			lastRowIndex--;
			currentRow = sheet.getRow(firstRowIndex);
			if (currentRow != null)
				lastValidCell = sheet.getRow(lastRowIndex).getCell(dateIndex);
		}

		first = new Date(sheet.getRow(firstRowIndex).getCell(dateIndex).toString());
		last = new Date(sheet.getRow(lastRowIndex).getCell(dateIndex).toString());

		return first.before(last);
	}

	/**
	 * Method checks if string is a valid date
	 * 
	 * @param dateString
	 * @return
	 */
	private static boolean isValidDate(String dateString) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		// dateFormat.setLenient(false);
		try {
			dateFormat.parse(dateString);
		} catch (ParseException pe) {
			return false;
		}
		return true;
	}

	/**
	 * Method that detect the coloumn of each parameter of transaction and
	 * returns maping for it
	 * 
	 * @return a map contains the parameters as the key and their column numbers
	 *         as value
	 */
	private void createParameterMap() {

		int rows; // Number of rows
		rows = sheet.getPhysicalNumberOfRows();

		XSSFRow row;
		XSSFCell cell;

		Pattern p;
		Matcher m;

		for (int r = 0; r < rows; r++) {

			row = sheet.getRow(r);
			if (row != null) {

				int cols = row.getPhysicalNumberOfCells();
				for (int c = 0; c < cols; c++) {

					cell = row.getCell(c);
					if (cell != null) {
						// Extracting transaction name column using regex
						p = Pattern.compile(" ?תי?אור ה?פעולה ?| ?תי?אור ?| ?ה?פעולה ?| ?סוג התנועה ?",
								Pattern.UNICODE_CASE);
						m = p.matcher(cell.toString());
						if (m.matches()) {
							parametersCols.put("name", c);
						}
						// Extracting transaction ref column using regex
						p = Pattern.compile("אסמכתא", Pattern.UNICODE_CASE);
						m = p.matcher(cell.toString());
						if (m.find()) {
							parametersCols.put("ref", c);
						}
						// Extracting transaction date column
						p = Pattern.compile(" ?תאריך ?", Pattern.UNICODE_CASE);
						m = p.matcher(cell.toString());
						if (m.matches()) {
							parametersCols.put("date", c);
						}
						// Extracting transaction balance column
						p = Pattern.compile("יתרה", Pattern.UNICODE_CASE);
						m = p.matcher(cell.toString());
						if (m.find()) {
							parametersCols.put("balance", c);
						}
						// Extracting transaction grant column
						p = Pattern.compile("זכות", Pattern.UNICODE_CASE);
						m = p.matcher(cell.toString());
						if (m.find()) {
							parametersCols.put("grant", c);
						}
						// Extracting transaction debit column
						p = Pattern.compile(" ?חובה ?", Pattern.UNICODE_CASE);
						m = p.matcher(cell.toString());
						if (m.matches()) {
							parametersCols.put("debit", c);
						}
					}
				}
				// Checks if the map was filled as needed
				if (parametersCols.size() > 3) {
					parametersCols.put("row", r + 1);
					break;
				}

				parametersCols.clear();
			}
		}
	}

	/**
	 * Method gets a row in the account table represents a transaction and the
	 * map of the paremeters needed for a transaction and parse the value for
	 * each parmeter of this transaction
	 * 
	 * @param row
	 * @param parametersCols
	 * @param balance
	 * @return a transaction object
	 */
	@SuppressWarnings("deprecation")
	private Transaction transctionParser(XSSFRow row) {

		Transaction t = new Transaction();
		XSSFCell currentCell;
		String currentValue;

		// Insert date value of the transaction
		currentCell = row.getCell(parametersCols.get("date"));
		// checks if cell is null, then the transaction is not legal
		if (currentCell == null || currentCell.toString().trim().equals(""))
			return null;
		currentValue = currentCell.toString();
		t.date = new Date(currentValue);

		// Insert the name of the transction
		currentCell = row.getCell(parametersCols.get("name"));
		// checks if cell is null, then the transaction is not legal
		if (currentCell == null || currentCell.toString().trim().equals(""))
			return null;
		currentValue = currentCell.toString();
		t.name = currentValue;

		// Insert the ref of the transction
		currentCell = row.getCell(parametersCols.get("ref"));
		// checks if cell is null, then the transaction is not legal
		if (currentCell == null || currentCell.toString().trim().equals(""))
			return null;
		currentValue = "" + (int)Double.parseDouble(currentCell.toString().trim());
		t.ref = currentValue;

		// Insert the value of the transaction and the balance
		currentCell = row.getCell(parametersCols.get("grant"));
		// Checks if cell is null, else its a debit
		if (currentCell != null && !currentCell.toString().trim().equals("")) {
			currentValue = currentCell.toString();
			t.value = Double.parseDouble(currentValue);
		} else {
			currentCell = row.getCell(parametersCols.get("debit"));
			currentValue = currentCell.toString();
			t.value = -(Double.parseDouble(currentValue));
		}

		// Insert the new balance after the transaction
		currentCell = row.getCell(parametersCols.get("balance"));
		// Checks if cell is not null, else the balance cell not existed and
		// using the last balance
		if (currentCell != null && !currentCell.toString().trim().equals("")) {
			currentValue = currentCell.toString().trim();
			t.balance = Double.parseDouble(currentValue);
		} else
			t.balance = account.balance + t.value;

		account.balance = t.balance;

		return t;
	}

}
