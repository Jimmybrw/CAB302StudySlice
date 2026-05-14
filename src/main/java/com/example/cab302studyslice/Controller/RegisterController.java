package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.AuthService;
import com.example.cab302studyslice.View.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Button eyeButton;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordVisible;
    @FXML private Button eyeButton2;
    @FXML private Label messageLabel;

    private boolean passwordShown = false;
    private boolean confirmPasswordShown = false;
    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        passwordVisible.textProperty().addListener((obs, old, val) -> passwordField.setText(val));
        confirmPasswordVisible.textProperty().addListener((obs, old, val) -> confirmPasswordField.setText(val));
    }

    @FXML
    private void togglePassword() {
        passwordShown = !passwordShown;
        if (passwordShown) passwordVisible.setText(passwordField.getText());
        passwordField.setVisible(!passwordShown);
        passwordVisible.setVisible(passwordShown);
        eyeButton.setText(passwordShown ? "🙈" : "👁");
    }

    @FXML
    private void toggleConfirmPassword() {
        confirmPasswordShown = !confirmPasswordShown;
        if (confirmPasswordShown) confirmPasswordVisible.setText(confirmPasswordField.getText());
        confirmPasswordField.setVisible(!confirmPasswordShown);
        confirmPasswordVisible.setVisible(confirmPasswordShown);
        eyeButton2.setText(confirmPasswordShown ? "🙈" : "👁");
    }

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
