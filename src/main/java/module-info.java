module com.example.cab302studyslice {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires java.sql;

    // Export and Open packages to allow JavaFX to run the UI
    opens com.example.cab302studyslice.Core to javafx.fxml;
    exports com.example.cab302studyslice.Core;

    opens com.example.cab302studyslice.Controller to javafx.fxml;
    exports com.example.cab302studyslice.Controller;

    opens com.example.cab302studyslice.View to javafx.fxml;
    exports com.example.cab302studyslice.View;

    exports com.example.cab302studyslice.Model;
}