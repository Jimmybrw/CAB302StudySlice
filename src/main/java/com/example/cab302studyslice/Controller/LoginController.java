package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.AuthService;
import com.example.cab302studyslice.Model.User;
import com.example.cab302studyslice.View.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for the login view.
 * Handles user authentication, password visibility toggle, and navigation to registration.
 */
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Button eyeButton;
    @FXML private Label messageLabel;

    private boolean passwordShown = false;
    private static String registerMessage = "";
    private final AuthService authService = new AuthService();

    /**
     * Initializes the login controller.
     * Sets up password field listeners and displays any message from the registration flow.
     */
    @FXML
    public void initialize() {
        passwordVisible.textProperty().addListener((obs, old, val) -> passwordField.setText(val));
        if (!registerMessage.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: #819D93;");
            messageLabel.setText(registerMessage);
            registerMessage = "";
        }
    }

    /**
     * Toggles password visibility between masked and visible states.
     */
    @FXML
    private void togglePassword() {
        passwordShown = !passwordShown;
        if (passwordShown) {
            passwordVisible.setText(passwordField.getText());
        }
        passwordField.setVisible(!passwordShown);
        passwordVisible.setVisible(passwordShown);
        eyeButton.setText(passwordShown ? "🙈" : "👁");
    }

    /**
     * Validates login credentials and switches to the dashboard on success.
     * Displays error messages for invalid credentials or missing fields.
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        AuthService.AuthResult result = authService.login(username, password, AuthService.databaseGateway());

        if (result.status() == AuthService.Status.MISSING_FIELDS) {
            messageLabel.setStyle("-fx-text-fill: #7B4141;");
            messageLabel.setText(result.message());
            return;
        }

        if (result.isSuccess()) {
            User.setCurrentUserId(result.userId());
            User.setCurrentUsername(result.username());
            ViewManager.switchScene("dashboard-view.fxml");
        } else {
            messageLabel.setStyle("-fx-text-fill: #7B4141;");
            messageLabel.setText(result.message());
        }
    }

    /**
     * Navigates to the registration view.
     */
    @FXML
    private void handleGoToRegister() {
        ViewManager.switchScene("register-view.fxml");
    }

    /**
     * Sets a message to be displayed on the login screen (typically from registration flow).
     *
     * @param message the message to display
     */
    public static void setRegisterMessage(String message) {
        registerMessage = message;
    }
}
