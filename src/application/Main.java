package application;
	
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

// Runs the main GUI application.
// Nate Hunter - 01/29/2022
public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		// Create an 8x8 chess board.
		StackPane board = Board.initialize();
	    
	    // Create a text log.
	    ScrollPane log = Log.initialize();
	    
	    // Combine the chess board and text log horizontally.
	    HBox windowContents = new HBox(board, log);
	    windowContents.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(0), new Insets(0))));
	    windowContents.setPadding(new Insets(10, 10, 10, 10));
	    windowContents.setSpacing(10);
		
		// Add the contents to the window and display the window.
		Stage window = primaryStage;
		window.setTitle("Chess");
		window.setScene(new Scene(windowContents));
		window.show();
		
		// If white is a bot, the first move must be triggered.
		Arbiter.playBotMoveIfAppropriate();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
