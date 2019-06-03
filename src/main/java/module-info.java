module GraphingCalculator {
    requires javafx.base;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.controls;

    exports dev.StylishNerds.GraphingCalculator;
    opens dev.StylishNerds.GraphingCalculator to javafx.fxml;
}