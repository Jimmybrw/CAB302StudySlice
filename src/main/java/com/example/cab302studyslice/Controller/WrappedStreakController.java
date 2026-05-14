package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.View.ViewManager;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class WrappedStreakController {
    @FXML
    private Region animatedOverlay;
    @FXML
    private Label streakValueLabel;
    @FXML
    private Label streakSupportLabel;
    @FXML
    private Button exitButton;

    @FXML
    private void initialize() {
        loadPlaceHolderData();
        animateBackground();
        playRevealAnimation();
    }

    private void loadPlaceHolderData() {
        streakValueLabel.setText("3 days");
        streakSupportLabel.setText("You've studied three days in a row. Keep the streak alive!");
    }

    // -----------------------------
    // ANIMATION
    // -----------------------------

    private void animateBackground() {
        TranslateTransition move = new TranslateTransition(Duration.seconds(10), animatedOverlay);
        move.setFromX(-50);
        move.setToX(50);
        move.setCycleCount(Animation.INDEFINITE);
        move.setAutoReverse(true);

        TranslateTransition moveY = new TranslateTransition(Duration.seconds(12), animatedOverlay);
        moveY.setFromY(-30);
        moveY.setToY(30);
        moveY.setCycleCount(Animation.INDEFINITE);
        moveY.setAutoReverse(true);

        ParallelTransition animation = new ParallelTransition(move, moveY);
        animation.play();

        FadeTransition fade = new FadeTransition(Duration.seconds(6), animatedOverlay);
        fade.setFromValue(0.2);
        fade.setToValue(0.35);
        fade.setCycleCount(Animation.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();
    }

    private void playRevealAnimation() {
        streakValueLabel.setOpacity(0);
        streakValueLabel.setTranslateY(16);

        streakSupportLabel.setOpacity(0);
        streakValueLabel.setTranslateY(16);

        exitButton.setOpacity(0);
        exitButton.setTranslateY(14);

        FadeTransition valueFade = new FadeTransition(Duration.millis(450), streakValueLabel);
        valueFade.setFromValue(0);
        valueFade.setToValue(1);

        TranslateTransition valueSlide = new TranslateTransition(Duration.millis(450), streakValueLabel);
        valueSlide.setFromY(16);
        valueSlide.setToY(0);

        ParallelTransition valueReveal = new ParallelTransition(valueFade, valueSlide);

        FadeTransition supportFade = new FadeTransition(Duration.millis(450), streakSupportLabel);
        supportFade.setFromValue(0);
        supportFade.setToValue(1);

        TranslateTransition supportSlide = new TranslateTransition(Duration.millis(450), streakSupportLabel);
        supportSlide.setFromY(16);
        supportSlide.setToY(0);

        ParallelTransition supportReveal = new ParallelTransition(supportFade, supportSlide);

        FadeTransition buttonFade = new FadeTransition(Duration.millis(400), exitButton);
        buttonFade.setFromValue(0);
        buttonFade.setToValue(1);

        TranslateTransition buttonSlide = new TranslateTransition(Duration.millis(400), exitButton);
        buttonSlide.setFromY(14);
        buttonSlide.setToY(0);

        ParallelTransition buttonReveal = new ParallelTransition(buttonFade, buttonSlide);

        SequentialTransition sequence = new SequentialTransition(valueReveal, supportReveal, buttonReveal);
        sequence.setOnFinished(event -> playExitButtonPulse());
        sequence.play();
    }

    private void playExitButtonPulse() {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(900), exitButton);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    // -----------------------------
    // NAVIGATION BUTTONS
    // -----------------------------

    @FXML
    private void onBackClick() {
        ViewManager.switchScene("wrapped-ranking-view.fxml");
    }

    @FXML
    private void onExitClick() {
        ViewManager.switchScene("dashboard-view.fxml");
    }
}
