package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.View.ViewManager;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class WrappedTopAppController {
    @FXML
    private Region animatedOverlay;
    @FXML
    private Label topAppLabel;
    @FXML
    private Label topAppSupportLabel;
    @FXML
    private Button nextButton;

    @FXML
    private void initialize() {
        loadPlaceHolderData();
        animateBackground();
        playRevealAnimation();
    }

    private void loadPlaceHolderData() {
        topAppLabel.setText("Word Document");
        topAppSupportLabel.setText("This was your main study space for this session. Looks like the assignment grind was real.");
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
        topAppLabel.setOpacity(0);
        topAppLabel.setScaleX(0.9);
        topAppLabel.setScaleY(0.9);

        topAppSupportLabel.setOpacity(0);
        topAppSupportLabel.setTranslateY(16);

        nextButton.setOpacity(0);
        nextButton.setTranslateY(14);

        FadeTransition appFade = new FadeTransition(Duration.millis(500), topAppLabel);
        appFade.setFromValue(0);
        appFade.setToValue(1);

        ScaleTransition appScale = new ScaleTransition(Duration.millis(500), topAppLabel);
        appScale.setFromX(0.9);
        appScale.setFromY(0.9);
        appScale.setToX(1.0);
        appScale.setToY(1.0);

        ParallelTransition appReveal = new ParallelTransition(appFade, appScale);

        FadeTransition supportFade = new FadeTransition(Duration.millis(450), topAppSupportLabel);
        supportFade.setFromValue(0);
        supportFade.setToValue(1);

        TranslateTransition supportSlide = new TranslateTransition(Duration.millis(450), topAppSupportLabel);
        supportSlide.setFromY(16);
        supportSlide.setFromY(0);

        ParallelTransition supportReveal = new ParallelTransition(supportFade, supportSlide);

        FadeTransition buttonFade = new FadeTransition(Duration.millis(400), nextButton);
        buttonFade.setFromValue(0);
        buttonFade.setToValue(1);

        TranslateTransition buttonSlide = new TranslateTransition(Duration.millis(400), nextButton);
        buttonSlide.setFromY(14);
        buttonSlide.setToY(0);

        ParallelTransition buttonReveal = new ParallelTransition(buttonFade, buttonSlide);

        SequentialTransition sequence = new SequentialTransition(appReveal, supportReveal, buttonReveal);
        sequence.setOnFinished(event -> playNextButtonPulse());
        sequence.play();
    }

    private void playNextButtonPulse() {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(900), nextButton);
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
        ViewManager.switchScene("wrapped-totalTime-view.fxml");
    }

    @FXML
    private void onNextClick() {
        ViewManager.switchScene("wrapped-focusScore-view.fxml");
    }
}
