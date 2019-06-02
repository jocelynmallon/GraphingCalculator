/*
    GraphableFunc.java
    This class is the model for a user input
    'graphable' function.
 */
package dev.StylishNerds.GraphingCalculator;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.chart.XYChart;

public class GraphableFunc {

    private final SimpleStringProperty varName;
    private final SimpleBooleanProperty checked;
    private Expression expression;
    private final SimpleStringProperty rawInput;
    private final SimpleIntegerProperty index;
    private final XYChart.Series<Double, Double> data;

    /**
     * Overloaded Constructor:
     * @param index the table index for the function
     * @param var   the variable the function is in
     */
    public GraphableFunc(int index, String var) {
        this(index);
        this.varName.set(var);
    }

    /**
     * Default constructor:
     * @param index the table index for the function
     */
    public GraphableFunc(int index) {
        this.index = new SimpleIntegerProperty(index);
        this.varName = new SimpleStringProperty("x");
        this.rawInput = new SimpleStringProperty();
        this.checked = new SimpleBooleanProperty(true);
        this.data = new XYChart.Series<>();
    }

    public SimpleIntegerProperty indexProperty() {
        return this.index;
    }

    public int getIndex() {
        return this.indexProperty().get();
    }

    public String getVarName() {
        return varName.get();
    }

    public SimpleBooleanProperty checkedProperty() {
        return checked;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public String getRawInput() {
        return rawInput.get();
    }

    public SimpleStringProperty rawInputProperty() {
        return rawInput;
    }

    public void setRawInput(String rawInput) {
        this.rawInput.set(rawInput);
    }

    public XYChart.Series<Double, Double> getData() {
        return data;
    }
}
