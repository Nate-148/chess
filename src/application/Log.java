package application;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

// The log to display textual information such as past moves and potential moves.
// Nate Hunter - 05/15/2022
public class Log {
	// The text displayed in the log.
	private static Label logText;
	// The history of past moves.
	private static String gameHistory = "";
	// The potential moves to display.
	private static String gameFutures = "Future moves coming soon.";
	
	// Initializes the log in the GUI.
	public static ScrollPane initialize() {
	    logText = new Label();
	    logText.setFont(Font.font("Calibri", 13));
	    logText.setTextFill(Color.WHITE);
	    logText.setWrapText(true);
	    logText.setMaxWidth(200);
	    
	    ScrollPane log = new ScrollPane();
	    log.setContent(logText);
	    log.setStyle("-fx-background: rgb(64,64,64);");
	    log.setPadding(new Insets(5, 5, 5, 5));
	    log.setPrefViewportWidth(200);
	    log.setHbarPolicy(ScrollBarPolicy.NEVER);
	    log.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
	    return log;
	}
	
	// Appends text (typically a move) to the game history.
	public static void appendToHistory(String text) {
		gameHistory += text + ' ';
		update();
	}
	
	// Sets the future moves to display.
	public static void writeFutures(String moves) {
		gameFutures = moves;
		update();
	}
	
	// Updates the log.
	private static void update() {
		logText.setText(gameHistory + "\n\n" + gameFutures);
	}
}
