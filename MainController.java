
import java.io.File;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController {
	@FXML
	private Button excelButton;
	
	@FXML
	private Button analyzeButton;
	
	@FXML
	private Label listview;
	
	@FXML
	private TextField bankName;
	
	@FXML
	private Label label;
	
	// The excel file
	File file;

	public void excelButtonOnAction(ActionEvent event){

		FileChooser fc = new FileChooser();
		file = fc.showOpenDialog(null);
		if (file != null)
			listview.setText(file.getName());
		else
			System.out.println("nono");
	}
	
	public void analyzeButtonOnAction(ActionEvent event) throws IOException{
		
		AccountAnalyzer analyzer = null;
		
		try{
			analyzer = new AccountAnalyzer(file.getAbsolutePath(), bankName.getText());
		}catch(Exception e){
			label.setText("File or bank name are invalid");
			return;
		}
		analyzer.analyze();
		((Node) event.getSource()).getScene().getWindow().hide();
		Stage stage = new Stage();
		Pane root = null;
		FXMLLoader loader = new FXMLLoader();
		root = loader.load(getClass().getResource("DataShow.fxml").openStream());
		DataShowController dsc = (DataShowController)loader.getController();
		dsc.dataShow(analyzer);
		Scene scene = new Scene(root,1024,768);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		stage.setScene(scene);
		stage.show();
	}

}
