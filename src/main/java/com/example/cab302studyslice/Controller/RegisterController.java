package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.AuthService;
import com.example.cab302studyslice.View.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for the registration view.
 * Handles user account creation, password visibility toggles, and validation.
 */
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

    /**
     * Initializes the registration controller.
     * Sets up bidirectional text synchronization between password fields.
     */
    @FXML
    public void initialize() {
        passwordVisible.textProperty().addListener((obs, old, val) -> passwordField.setText(val));
        confirmPasswordVisible.textProperty().addListener((obs, old, val) -> confirmPasswordField.setText(val));
    }

    /**
     * Toggles the visibility of the password field.
     */
    @FXML
    private void togglePassword() {
        passwordShown = !passwordShown;
        if (passwordShown) passwordVisible.setText(passwordField.getText());
        passwordField.setVisible(!passwordShown);
        passwordVisible.setVisible(passwordShown);
        eyeButton.setText(passwordShown ? "🙈" : "👁");
    }

    /**
     * Toggles the visibility of the confirm password field.
     */
    @FXML
    private void toggleConfirmPassword() {
        confirmPasswordShown = !confirmPasswordShown;
        if (confirmPasswordShown) confirmPasswordVisible.setText(confirmPasswordField.getText());
        confirmPasswordField.setVisible(!confirmPasswordShown);
        confirmPasswordVisible.setVisible(confirmPasswordShown);
        eyeButton2.setText(confirmPasswordShown ? "🙈" : "👁");
    }

    /**
     * Registers a new user with validation.
     * On success, navigates to login with a success message.
     * Displays error messages for validation failures.
     */
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

    /**
     * Navigates back to the login view.
     */
    @FXML
    private void handleBackToLogin() {
        ViewManager.switchScene("login-view.fxml");
    }
}
