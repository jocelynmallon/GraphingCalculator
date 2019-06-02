/*
    AxisFormatter.java
    This class extends StringConverter<Number>
    to only show integer values, instead of
    double; this keeps axis labels uniform
 */
package dev.StylishNerds.GraphingCalculator;

import javafx.util.StringConverter;

public class AxisFormatter extends StringConverter<Number> {

    public AxisFormatter() {
    }

    @Override
    public String toString(Number n) {
        if (n.intValue() != n.doubleValue()) {
            return "";
        } else {
            return "" + (n.intValue());
        }
    }
    @Override
    public Number fromString(String s) {
        Number val = Double.parseDouble(s);
        return val.intValue();
    }
}
