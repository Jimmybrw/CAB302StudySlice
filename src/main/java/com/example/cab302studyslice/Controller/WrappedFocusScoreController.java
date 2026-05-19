package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.AiAPI;
import com.example.cab302studyslice.Model.WrappedDataHolder;
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

public class WrappedFocusScoreController {

    @FXML
    private Region animatedOverlay;

    @FXML
    private Label focusScoreLabel;

    @FXML
    private Label focusSupportLabel;

    @FXML
    private Button nextButton;

    @FXML
    public void initialize(){
        loadData();
        animateBackground();
        playRevealAnimation();
        playNextButtonPulse();
    }

    private void loadData() {
        if (WrappedDataHolder.hasData()) {
            AiAPI.WrappedData data = WrappedDataHolder.getWrappedData();
            focusScoreLabel.setText(data.score + "%");
            if (data.score >= 80) {
                focusSupportLabel.setText(
                        "Excellent focus! You stayed locked in for most of the session with barely a distraction.");
            } else if (data.score >= 60) {
                focusSupportLabel.setText(
                        "Solid session — you stayed on task for the most part with only a few attention dips.");
            } else if (data.score >= 40) {
                focusSupportLabel.setText(
                        "A mixed session — some good stretches, but a few distractions knocked the score back.");
            } else {
                focusSupportLabel.setText(
                        "Tough session — but every study block is progress and there is always next time.");
            }
        } else {
            focusScoreLabel.setText("82%");
            focusSupportLabel.setText("You stayed locked in for most of the session, with only a few attention dips.");
        }
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

    private void playRevealAnimation() {
        focusScoreLabel.setOpacity(0);
        focusScoreLabel.setScaleX(0.75);
        focusScoreLabel.setScaleY(0.75);

        focusSupportLabel.setOpacity(0);
        focusSupportLabel.setTranslateY(16);

        nextButton.setOpacity(0);
        nextButton.setTranslateY(14);

        FadeTransition scoreFade = new FadeTransition(Duration.millis(550), focusScoreLabel);
        scoreFade.setFromValue(0);
        scoreFade.setToValue(1);

        ScaleTransition scoreScale = new ScaleTransition(Duration.millis(550), focusScoreLabel);
        scoreScale.setFromX(0.75);
        scoreScale.setFromY(0.75);
        scoreScale.setToX(1.0);
        scoreScale.setToY(1.0);

        ParallelTransition scoreReveal = new ParallelTransition(scoreFade, scoreScale);

        FadeTransition supportFade = new FadeTransition(Duration.millis(450), focusSupportLabel);
        supportFade.setFromValue(0);
        supportFade.setToValue(1);

        TranslateTransition supportSlide = new TranslateTransition(Duration.millis(450), focusSupportLabel);
        supportSlide.setFromX(16);
        supportSlide.setFromY(0);

        ParallelTransition supportReveal = new ParallelTransition(supportFade, supportSlide);

        FadeTransition buttonFade = new FadeTransition(Duration.millis(400), nextButton);
        buttonFade.setFromValue(0);
        buttonFade.setToValue(1);

        TranslateTransition buttonSlide = new TranslateTransition(Duration.millis(400), nextButton);
        buttonSlide.setFromY(14);
        buttonSlide.setToY(0);

        ParallelTransition buttonReveal = new ParallelTransition(buttonFade, buttonSlide);

        SequentialTransition sequence = new SequentialTransition(scoreReveal, supportReveal, buttonReveal);
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

    /**
     *
     */
    @FXML
    private void onBackClick() {
        ViewManager.switchScene("wrapped-topApp-view.fxml");
    }

    @FXML
    private void onNextClick() {
        ViewManager.switchScene("wrapped-badHabit-view.fxml");
    }
}
