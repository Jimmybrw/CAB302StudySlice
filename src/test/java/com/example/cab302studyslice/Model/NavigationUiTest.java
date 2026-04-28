package com.example.cab302studyslice.Model;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;


import static org.junit.jupiter.api.Assertions.*;

public class NavigationUiTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = javafx.fxml.FXMLLoader.load(
                getClass().getResource("/com/example/cab302studyslice/timer-view.fxml")
        );
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void testGoToDashboard() {
        // Click the sidebar button
        clickOn("#dashboardButton");

        // Assert the new scene contains something unique to dashboard
        verifyThat("#dashboardRoot", javafx.scene.Node::isVisible);
    }
}
