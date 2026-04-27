package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.View.ViewManager;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.util.Duration;


public class WrapperController {
    @FXML
    private Label brandLabel;
    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private Label pageIndicatorLabel;
    @FXML
    private Button backButton;
    @FXML
    private Button nextButton;


    @FXML
    public void initialize(){
        animateBackground();
        playIntroAnimation();
        playNextButtonPulse();
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
        move.setAutoReverse(true);

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
        brandLabel.setOpacity(0);
        brandLabel.setTranslateY(12);

        titleLabel.setOpacity(0);
        titleLabel.setScaleX(0.92);
        titleLabel.setScaleY(0.92);

        subtitleLabel.setOpacity(0);
        subtitleLabel.setTranslateY(16);

        nextButton.setOpacity(0);
        nextButton.setTranslateY(14);

        FadeTransition brandFade = new FadeTransition(Duration.millis(400), brandLabel);
        brandFade.setFromValue(0);
        brandFade.setToValue(1);

        TranslateTransition brandSlide = new TranslateTransition(Duration.millis(400), brandLabel);
        brandSlide.setFromY(12);
        brandSlide.setToY(0);

        ParallelTransition brandAnim = new ParallelTransition(brandFade, brandSlide);

        FadeTransition titleFade = new FadeTransition(Duration.millis(600), titleLabel);
        titleFade.setFromValue(0);
        titleFade.setToValue(1);

        ScaleTransition titleScale = new ScaleTransition(Duration.millis(600), titleLabel);
        titleScale.setFromX(0.92);
        titleScale.setFromY(0.92);
        titleScale.setToX(1.0);
        titleScale.setToY(1.0);

        ParallelTransition titleAnim = new ParallelTransition(titleFade, titleScale);

        FadeTransition subtitleFade = new FadeTransition(Duration.millis(450), subtitleLabel);
        subtitleFade.setFromValue(0);
        subtitleFade.setToValue(1);

        TranslateTransition subtitleSlide = new TranslateTransition(Duration.millis(450), subtitleLabel);
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

        SequentialTransition sequence = new SequentialTransition(brandAnim, titleAnim, subtitleAnim, buttonAnim);

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
        ViewManager.switchScene("dashboard-view.fxml");
    }

    @FXML
    private void onNextClick() {
        ViewManager.switchScene("wrapped-totalTime-view.fxml");
    }
}
