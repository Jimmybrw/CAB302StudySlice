package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.View.ViewManager;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class WrappedComparisonController {
    @FXML
    private Label comparisonLabel;
    @FXML
    private Label comparisonSupportLabel;
    @FXML
    private Button nextButton;


    @FXML
    public void initialize(){
        animateBackground();
        playIntroAnimation();
    }

    @FXML private Region animatedOverlay;

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

    private void playIntroAnimation() {

        comparisonLabel.setOpacity(0);
        comparisonLabel.setScaleX(0.92);
        comparisonLabel.setScaleY(0.92);

        comparisonSupportLabel.setOpacity(0);
        comparisonSupportLabel.setTranslateY(16);

        nextButton.setOpacity(0);
        nextButton.setTranslateY(14);

        FadeTransition titleFade = new FadeTransition(Duration.millis(600), comparisonLabel);
        titleFade.setFromValue(0);
        titleFade.setToValue(1);

        ScaleTransition titleScale = new ScaleTransition(Duration.millis(600), comparisonLabel);
        titleScale.setFromX(0.92);
        titleScale.setFromY(0.92);
        titleScale.setToX(1.0);
        titleScale.setToY(1.0);

        ParallelTransition titleAnim = new ParallelTransition(titleFade, titleScale);

        FadeTransition subtitleFade = new FadeTransition(Duration.millis(450), comparisonSupportLabel);
        subtitleFade.setFromValue(0);
        subtitleFade.setToValue(1);

        TranslateTransition subtitleSlide = new TranslateTransition(Duration.millis(450), comparisonSupportLabel);
        subtitleSlide.setFromY(16);
        subtitleSlide.setToY(0);

        ParallelTransition subtitleAnim = new ParallelTransition(subtitleFade, subtitleSlide);

        FadeTransition buttonFade = new FadeTransition(Duration.millis(400), nextButton);
        buttonFade.setFromValue(0);
        buttonFade.setToValue(1);

        TranslateTransition buttonSlide = new TranslateTransition(Duration.millis(400), nextButton);
        buttonSlide.setFromY(14);
        buttonSlide.setToY(0);

        ParallelTransition buttonAnim = new ParallelTransition(buttonFade, buttonSlide);

        SequentialTransition sequence = new SequentialTransition(titleAnim, subtitleAnim, buttonAnim);
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
        ViewManager.switchScene("wrapped-goodHabit-view.fxml");
    }

    @FXML
    private void onNextClick() {
        ViewManager.switchScene("wrapped-ranking-view.fxml");
    }
}
