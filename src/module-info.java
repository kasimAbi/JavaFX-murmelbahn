module sample {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens sample to javafx.fxml;
    exports sample;
}