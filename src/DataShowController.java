import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.formula.functions.Days360;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;

public class DataShowController {

	@FXML
	TableView<Transaction> tIncomes;

	@FXML
	TableView<Transaction> tLoans;

	@FXML
	TableView<Transaction> tMortgage;

	@FXML
	LineChart<String, Number> balanceChart;

	public void dataShow(AccountAnalyzer analyzer) {

		ObservableList<Transaction> incomesData = FXCollections.observableArrayList();
		ObservableList<Transaction> loansData = FXCollections.observableArrayList();
		ObservableList<Transaction> mortgageData = FXCollections.observableArrayList();
		ArrayList<Transaction> currentTypeList = null;
		// tIncomes.setEditable(true);
		//
		// TableColumn date = new TableColumn("Date");
		// TableColumn ref = new TableColumn("Ref");
		// TableColumn value = new TableColumn("Value");
		// TableColumn balance = new TableColumn("Balance");
		// TableColumn name = new TableColumn("Name");
		//
		//
		// date.setCellValueFactory(new PropertyValueFactory<Transaction,
		// Date>("date"));
		// ref.setCellValueFactory(new PropertyValueFactory<Transaction,
		// String>("ref"));
		// value.setCellValueFactory(new PropertyValueFactory<Transaction,
		// Integer>("value"));
		// balance.setCellValueFactory(new PropertyValueFactory<Transaction,
		// Integer>("balance"));
		// name.setCellValueFactory(new PropertyValueFactory<Transaction,
		// String>("name"));
		//
		// tIncomes.getColumns().addAll(date, ref, value, balance, name);

		setTable(tIncomes);
		setTable(tLoans);
		setTable(tMortgage);

		for (String key : analyzer.account.classified.keySet()) {

			currentTypeList = analyzer.account.classified.get(key);

			if (!currentTypeList.isEmpty()) {
				for (Transaction t : currentTypeList) {
					if (t.value > 0)
						incomesData.add(t);
				}
			}
		}

		currentTypeList = analyzer.account.classified.get("Loan");

		if (!currentTypeList.isEmpty()) {
			for (Transaction t : currentTypeList) {
				loansData.add(t);
			}
		}

		currentTypeList = analyzer.account.classified.get("Mortgage");
		if (!currentTypeList.isEmpty()) {
			for (Transaction t : currentTypeList) {
				mortgageData.add(t);
			}
		}

		tIncomes.setItems(incomesData);
		tLoans.setItems(loansData);
		tMortgage.setItems(mortgageData);

		createBalanceChart(analyzer);

	}

	private static long daysBetween(Date one, Date two) {
		long difference = (one.getTime() - two.getTime()) / 86400000;
		return Math.abs(difference);
	}


	private void createBalanceChart(AccountAnalyzer analyzer) {

		XYChart.Series series = new XYChart.Series();
		
		XYChart.Series series2 = new XYChart.Series();
		
		ArrayList<Transaction> lastThirtyDays = new ArrayList<Transaction>();
		
		Date firstDate = analyzer.account.transactions.get(0).date;
		
		Date secondDate = analyzer.account.transactions.get(1).date;
		
		int i = 1;
		
//		while(daysBetween(firstDate, secondDate) < 30){
//			lastThirtyDays.add(e)
//			
//		}
		
		
		
		int counter = 1;
		
		double balance = 0;

		for (Transaction t : analyzer.account.transactions) {

			series.getData().add(new XYChart.Data(new SimpleDateFormat("MM-dd").format(t.date), t.balance));
			
			if(counter % 30 != 0)
				balance +=t.balance;
			else{
				series2.getData().add(new XYChart.Data(new SimpleDateFormat("MM-dd").format(t.date), balance / 30));
				balance = 0;
			}
			
			counter++;
		}
		
		balanceChart.getData().addAll(series, series2);
		
		balanceChart.setCreateSymbols(false);
		
		// for (String key : analyzer.account.outcomesClassified.keySet()) {
		//
		// ArrayList<Transaction> currentTypeList =
		// analyzer.account.outcomesClassified.get(key);
		//
		// if (!currentTypeList.isEmpty()) {
		// for (Transaction t : currentTypeList) {
		// series.getData().add(new XYChart.Data(t.getDate(), t.balance));
		// }
		// }
		// }
	}

	private static void setTable(TableView<Transaction> t) {
		t.setEditable(true);

		TableColumn date = new TableColumn("Date");
		TableColumn ref = new TableColumn("Ref");
		TableColumn value = new TableColumn("Value");
		TableColumn balance = new TableColumn("Balance");
		TableColumn name = new TableColumn("Name");

		date.setCellValueFactory(new PropertyValueFactory<Transaction, Date>("date"));
		ref.setCellValueFactory(new PropertyValueFactory<Transaction, String>("ref"));
		value.setCellValueFactory(new PropertyValueFactory<Transaction, Integer>("value"));
		balance.setCellValueFactory(new PropertyValueFactory<Transaction, Integer>("balance"));
		name.setCellValueFactory(new PropertyValueFactory<Transaction, String>("name"));

		t.getColumns().addAll(date, ref, value, balance, name);
	}
}
