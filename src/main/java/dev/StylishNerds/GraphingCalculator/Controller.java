/*
    Controller.java

    The main view controller for our calculator;
 */
package dev.StylishNerds.GraphingCalculator;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableColumn.CellEditEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    // instance fields/variables
    private final SimpleStringProperty output;
    private final Parser parser;
    private final ObservableList<GraphableFunc> userFunctions;

    /**
     * Constructor for our controller
     */
    public Controller() {
        this.output = new SimpleStringProperty("");
        this.parser = new Parser();
        this.userFunctions = FXCollections.observableArrayList();
    }

    // FXML created objects, only import the objects
    // we actually need to control/modify
    @FXML private Label display;        // handles the actual display/output for our calculator
    @FXML private TableView<GraphableFunc> userFuncTable;
    @FXML private TableColumn<GraphableFunc, String> indexCol;
    @FXML private TableColumn<GraphableFunc, String> functionCol;
    @FXML private TableColumn<GraphableFunc, Boolean> checkBoxCol;
    @FXML private ToggleButton graphToggleButton, normToggleButton;
    @FXML private ToggleGroup modeControl;
    @FXML private VBox normalModePane;
    @FXML private SplitPane graphModePane;
    @FXML private LineChart<Double, Double> graphChart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Slider negXSlider, negYSlider, posXSlider, posYSlider;

    /**
     * Event Handler for number key presses
     * @param e     the ActionEvent, from a button
     *              'on action' to respond to
     */
    @FXML void buttonHandler(ActionEvent e) {
        String key = ((Button) e.getSource()).getText();
        processInput(key);
    }

    /**
     * Parses the actual input now for scientific mode
     * @param key   the last input, as a string
     *              either the keycode from a keyevent
     *              or the button .label text from
     *              an actionevent
     */
    private void processInput(String key) {
        String curr = output.get(); // grab existing output for efficiency
        // check for error string and clear it.
        if (curr.matches("Error|Undefined")) {
            output.set("");
        }
        if (key.matches("[=]")) {               // match equals/enter first
            computeNow();
        } else if (key.matches("C|CE")) {       // catch clear before generic alpha catching for functions
            output.set("");
        } else if (key.matches("⇍")) {
            if (noEntry()) {
                return;
            } else {
                output.set(curr.substring(0, curr.length() - 1));
            }
        } else if (key.matches("\\+/-")) {      // handle 'invert' sign: i.e. "+/-" key
            if (noEntry()) {
                return;
            } else if (curr.charAt(0) == '-') {
                output.set(curr.substring(1)); // remove the leading '-'
            } else {
                output.set("-(" + curr + ")"); // negate the current input & wrap in parens to be safe
            }
        } else if (key.matches("1/x")) {        // handle reciprocal key
            output.set("1/(" + curr + ")" );
            computeNow();
        } else {
            output.set(curr + key);
        }
    }

    /**
     * Make sure the output string/display isn't empty
     * before trying to either delete a character, or
     * clear the entire output
     * @return  true if output.get() is empty/blank
     */
    private boolean noEntry() {
        return output.get().isEmpty() || output.get().isBlank();
    }

    /**
     * Hands off computation to the parser class
     * and catches errors accordingly
     */
    private void computeNow() {
        try {
            Expression x = parser.eval(output.get());
            Double result = x.eval();
            if (result.isNaN()) {   // make sure we actually have a number
                output.set("Undefined");
            } else {
                output.set(String.valueOf(result));
            }
        } catch (Exception e) {
            output.set("Error");
        }
    }

    /**
     * Event handler for keyboard input instead of
     * our GUI/buttons
     * @param e     the keyEvent to process
     */
    private void keyHandler(KeyEvent e) {
        KeyCode key = e.getCode();
        switch (key) {
            case ENTER:
                processInput("="); break;
            case DELETE:    // intentionally fall through
            case BACK_SPACE:
                processInput("⇍"); break;
            default:
                processInput(e.getText()); break;
        }
    }

    /**
     * parseFuncInput: creates a GraphableFunc object
     * from a given user input string, to later be
     * plotted/graphed
     * @param e the tableCell edit event to process, used
     *          to get the raw input string
     */
    private void parseFuncInput(CellEditEvent<GraphableFunc, String> e) {
        GraphableFunc func = userFuncTable.getSelectionModel().getSelectedItem();
        String raw = e.getNewValue();
        func.setRawInput(raw);
        if (userFunctions.size() == func.getIndex() + 1) {
            // add a new row/blank entry
            this.addFunctionRow();
        }
        this.graphNow(func);
    }

    /**
     * plots a user function as a data series, in a
     * javafx line chart, with resolution equal to
     * min tick values
     * @param func  the graphablefunction object to
     *              parse and graph
     */
    private void graphNow(GraphableFunc func) {
        Series<Double, Double> data = func.getData();  // get the data series from the function object
        if (!graphChart.getData().contains(data)) {     // new function, add the data series to the chart
            graphChart.getData().add(data);     // add the data series to the chart
            data.getNode().visibleProperty().bindBidirectional(func.checkedProperty());
        } else {
            data.getData().clear(); // clear the data series and recompute
        }
        HashMap<String, Double> vars = new HashMap<>();
        Expression exp = parser.eval(func.getRawInput(), vars);
        func.setExpression(exp);    // store the compiled expression for reuse
	    double incr;
	    if (isTrigFunc(func.getRawInput())) {
	    	incr = Math.PI/18;  // use incrememts of PI for trig functions
	    } else {
	    	incr = 0.1; // else use increments of 1/10th
	    }
        for (double i = -100; i <= 100; i+=incr) {   // make sure we have decent 'resolution'
            vars.put(func.getVarName(), i);
            double yVal = exp.eval();
            //System.out.println("Evaluating for " + func.getVarName() + "=" + i + " => " + yVal);
            data.getData().add(new Data<>(i, exp.eval()));
        }
    }

    private boolean isTrigFunc(String in) {
    	return (in.toLowerCase().contains("sin") ||
		    in.toLowerCase().contains("cos")) ||
		    in.toLowerCase().contains("tan");
    }

    /**
     * Add a new, blank/empty row in our user function/
     * graphing input table
     */
    private void addFunctionRow() {
        userFunctions.add(new GraphableFunc(userFunctions.size()));
    }

    /**
     * Initialize the tableview for users to input
     * functions to be evaluated by the graphing
     * mode of our calculator.
     */
    private void initTable() {
        checkBoxCol.setCellValueFactory(cellData -> cellData.getValue().checkedProperty());
        checkBoxCol.setCellFactory(tc -> new CheckBoxTableCell<>());
        indexCol.setCellValueFactory(cellData -> Bindings.format("y%d=", cellData.getValue().indexProperty()));
        functionCol.setCellValueFactory(cellData -> cellData.getValue().rawInputProperty());
        functionCol.setCellFactory(TextFieldTableCell.forTableColumn());
        functionCol.setOnEditCommit(this::parseFuncInput);
        userFunctions.add(new GraphableFunc(userFunctions.size())); // add the first entry row
        userFuncTable.setItems(userFunctions);
    }

    /**
     * Initialize the chart/graph view:
     * turn of legends, turn off symbols, etc.
     */
    private void initGraph() {
        // bind the slider positions to relevant axis upper/lower bounds
        xAxis.lowerBoundProperty().bind(negXSlider.valueProperty());
        xAxis.upperBoundProperty().bind(posXSlider.valueProperty());
        yAxis.lowerBoundProperty().bind(negYSlider.valueProperty());
        yAxis.upperBoundProperty().bind(posYSlider.valueProperty());
        // set the label formatter for our axis to only show ints
        xAxis.setTickLabelFormatter(new AxisFormatter());
        yAxis.setTickLabelFormatter(new AxisFormatter());
    }

    /**
     * The initialize method for our fxml controller
     * @param url
     * @param resourceBundle
     */
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // bind our output display/label
        this.display.textProperty().bind(this.output);
        // hook the pane to register our keyHandler
        this.normalModePane.setOnKeyPressed(this::keyHandler);
        // bind our mode toggle buttons
        this.normToggleButton.selectedProperty().bindBidirectional(normalModePane.visibleProperty());
        this.graphToggleButton.selectedProperty().bindBidirectional(graphModePane.visibleProperty());
        // initialize the graph input table
        this.initTable();
        // initialize the actual graph/chart
        this.initGraph();
        // select normal mode by default on launch
        this.modeControl.selectToggle(normToggleButton);
        // ensure we never end up without a mode selected
        modeControl.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                modeControl.selectToggle(oldVal);
            }
        });
    }
}
