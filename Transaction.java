import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Transaction {

	// value of the transaction
	double value;

	// the date of the transction
	Date date;

	// The name of the transction
	String name;

	// balance after transaction
	double balance;
	
	// The ref of the transaction
	String ref;
	
	public Transaction() {
		value = 0;
		date = null;
		name = "";
		balance = 0;
		ref = "";
	}
	
	public Transaction(Transaction t) {
		
		this.value = t.value;
		this.date = (Date)t.date.clone();
		this.name = t.name;
		this.balance = t.balance;
		this.ref = t.ref;
		
	}
	
	public String getDate(){
		
		SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
		
		return dt1.format(date);
	}
	
	public int getValue(){
		return (int) value;
	}
	
	public int getBalance(){
		return (int) balance;
	}
	
	public String getName(){
		return name;
	}
	
	public String getRef(){
		return ref;
	}
	
	@Override
	public String toString() {
		
		@SuppressWarnings("deprecation")
		String dateString = date.getDate() + "-" + (date.getMonth() + 1) + "-" + (date.getYear() + 1900);
//		return name + "\t" + dateString + "\t" + ref + "\t" + value + "\t" + balance;
		
		return dateString + "\t" + ref + "\t" + String.format("\t%.3f", value) + String.format("\t%.3f", balance) + "\t" + name;
	}
	

}
