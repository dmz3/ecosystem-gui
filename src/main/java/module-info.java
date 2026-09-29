module com.ecosystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.ecosystem to javafx.fxml;

    exports com.ecosystem;
}
