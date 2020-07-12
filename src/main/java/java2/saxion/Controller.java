package java2.saxion;

import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class Controller implements Initializable {

    public AnchorPane root;
    public GridPane field;
    public Button button_errCheck;
    public Button button_checkAll;
    public Button button_newPuzzle;
    public Button button_exit;
    public Button button_solve;

    private final int maxLines = 9;
    private final int digitsToTakeOff = 2;

    private Button lastClickedButton;
    private Sudoku sudoku;
    private boolean errCheckEnabled = false;
    private Button[][] sudokuButtons;

    //buttons 1-9
    public void handleOnButtonAction(MouseEvent mouseEvent) {
        int userChoice = Integer.parseInt(((Button)mouseEvent.getSource()).getText());

        if(!lastClickedButton.getId().isEmpty()){
            System.out.println("Last clicked button: " + lastClickedButton.getId());
            int xPos = Integer.parseInt(lastClickedButton.getId().split(":")[0]);
            int yPos = Integer.parseInt(lastClickedButton.getId().split(":")[1]);
            boolean isSuitable = sudoku.isValueSuitableToPutThere(xPos, yPos, userChoice);

            sudoku.setValue(xPos, yPos, userChoice);
            lastClickedButton.setText("" + sudoku.getValue(xPos, yPos));
            sudokuButtons[xPos][yPos] = lastClickedButton;

            if(!isSuitable && errCheckEnabled){
                signalizeError(lastClickedButton);
            } else if(isSuitable && errCheckEnabled){
                signalizeSuccess(lastClickedButton);
            }

            if(sudoku.checkIfFinished()){
                int errors = findErrors();
                if(errors == 0){
                    showMessageWindow("Congratulations! \nYou solved the sudoku!");
                } else {
                    showMessageWindow("You solved the sudoku, \nbut made " + errors + " errors.");
                }
            }
        }
    }

    //sudoku buttons
    public void handleOnGridAction(MouseEvent mouseEvent) {
        lastClickedButton = (Button)mouseEvent.getSource();
    }

    //button "New Puzzle"
    public void handleNewPuzzleAction(MouseEvent mouseEvent) {
        sudoku = new Sudoku(maxLines, digitsToTakeOff);
        setField();
    }

    //button "Solve puzzle"
    public void handleSolveAction(MouseEvent mouseEvent) {
        sudoku.solve();
        //update buttons
        for(int xPos = 0; xPos < 9; xPos++) {
            for(int yPos = 0; yPos < 9; yPos++){
                sudokuButtons[xPos][yPos].setText("" + sudoku.getValue(xPos, yPos));
            }
        }
        showMessageWindow("Sudoku successfully solved!");
    }

    //button "Exit"
    public void handleExitAction(MouseEvent mouseEvent) {
        System.exit(0);
    }

    //button "Check puzzle"
    public void handleCheckAllAction(MouseEvent mouseEvent) {
        for(int xPos = 0; xPos < 9; xPos++){
            for(int yPos = 0; yPos < 9; yPos++){

                Button currentButton = sudokuButtons[xPos][yPos];
                int currentValue = Integer.parseInt(currentButton.getText());

                //setting zero, because otherwise the number to check and number that already exists will repeat
                sudoku.setValue(xPos, yPos, 0);
                if(sudoku.isValueSuitableToPutThere(xPos, yPos, currentValue)){
                    currentButton.setStyle("-fx-background-color: forestgreen");
                } else {
                    currentButton.setStyle("-fx-background-color: darksalmon");
                }
                //returning value back
                sudoku.setValue(xPos, yPos, currentValue);
            }
        }
        //returning basic color after 0.5 sec
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for(Button[] buttonsRow : sudokuButtons) {
                    for(Button currentButton : buttonsRow){
                        currentButton.setStyle("");
                    }
                }
            }
        }, 500);
    }

    //button "Enable error check"
    public void handleErrCheckAction(MouseEvent mouseEvent) {
        if(!errCheckEnabled){
            errCheckEnabled = true;
            ((Button)mouseEvent.getSource()).setText("Disable error check");
        } else {
            errCheckEnabled = false;
            ((Button)mouseEvent.getSource()).setText("Enable error check");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        sudoku = new Sudoku(maxLines, digitsToTakeOff);
        root = new AnchorPane();
        sudokuButtons = new Button[maxLines][maxLines];
        setField();
    }

    //function to setup the sudoku field
    public void setField() {

        for (int index = 0; index < 2; index++) {
            for (int index2 = 0; index2 < 3; index2++) {
                field.getColumnConstraints().add(new ColumnConstraints(50));
                field.getRowConstraints().add(new RowConstraints(50));
            }
            field.getColumnConstraints().add(new ColumnConstraints(10));
            field.getRowConstraints().add(new RowConstraints(10));
        }
        for (int index = 0; index < 3; index++) {
            field.getColumnConstraints().add(new ColumnConstraints(50));
            field.getRowConstraints().add(new RowConstraints(50));
        }

        for (int xPos = 0; xPos < 12; xPos++) {
            for (int yPos = 0; yPos < 12; yPos++) {
                if (!(xPos == 3 || xPos == 7 || xPos == 11 || yPos == 3 || yPos == 7 || yPos == 11)) {

                    int xPosRight = toNormalMatrix(xPos);
                    int yPosRight = toNormalMatrix(yPos);
                    String buttonIndex = (xPosRight + ":" + yPosRight);

                    Button button = new Button("" + sudoku.getValue(xPosRight, yPosRight));

                    button.setId(buttonIndex);
                    button.setOnMouseClicked(this::handleOnGridAction);
                    button.setMaxWidth(Double.MAX_VALUE);
                    button.setMaxHeight(Double.MAX_VALUE);
                    GridPane.setHgrow(button, Priority.ALWAYS);
                    GridPane.setVgrow(button, Priority.ALWAYS);

                    sudokuButtons[xPosRight][yPosRight] = button;
                    field.add(button, xPos, yPos);
                }
            }
        }
    }

    //function to set normal coordinates (avoid blank fields in sudoku gridPane)
    private int toNormalMatrix(int i){
        if(i >= 3 && i < 7){
            return i-1;
        } else if(i >= 7){
            return i-2;
        } else {
            return i;
        }
    }

    //function to signalize that the choice of digit is incorrect by blinking with red color
    private void signalizeError(Button button){
        if(button.getStyle().equals("")) {
            button.setStyle("-fx-background-color: darksalmon");
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            button.setStyle("");
                        }
                    }, 500);
        }
    }

    //function to signalize that the choice of digit is correct by blinking with green color
    private void signalizeSuccess(Button button){
        if(button.getStyle().equals("")){
            button.setStyle("-fx-background-color : forestgreen");
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            button.setStyle("");
                        }
                    }, 500);
        }
    }

    //function to open new window with given phrase
    private void showMessageWindow(String text){

        Text message = new Text(text);

        message.setFont(new Font(18));

        StackPane messageRoot = new StackPane();
        messageRoot.getChildren().add(message);

        Stage successWindow = new Stage();
        successWindow.setTitle("Sudoku message");
        successWindow.setScene(new Scene(messageRoot, message.getLayoutBounds().getWidth()+50, message.getLayoutBounds().getHeight()+50, Color.WHITE));
        successWindow.show();
    }

    //function to find all errors in the whole sudoku (used to determine whether the field is filled correctly)
    private int findErrors(){
        int errors = 0;
        for(int xPos = 0; xPos < 9; xPos++){
            for(int yPos = 0; yPos < 9; yPos++){
                int sudokuValue = sudoku.getValue(xPos, yPos);
                sudoku.setValue(xPos,yPos, 0);
                if(!sudoku.isValueSuitableToPutThere(xPos, yPos, sudokuValue)){
                    errors++;
                }
                sudoku.setValue(xPos, yPos, sudokuValue);
            }
        }
        return errors;
    }
}
