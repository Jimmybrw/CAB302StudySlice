package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.AuthService;
import com.example.cab302studyslice.View.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;
    private final AuthService authService = new AuthService();

    // Creates a new account if the inputs are valid
    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        AuthService.AuthResult result = authService.register(username, password, confirmPassword, AuthService.databaseGateway());

        if (result.isSuccess()) {
            LoginController.setRegisterMessage(result.message());
            ViewManager.switchScene("login-view.fxml");
        } else {
            messageLabel.setStyle("-fx-text-fill: #7B4141;");
            messageLabel.setText(result.message());
        }
    }

    // Returns to the login page
    @FXML
    private void handleBackToLogin() {
        ViewManager.switchScene("login-view.fxml");
    }
}
