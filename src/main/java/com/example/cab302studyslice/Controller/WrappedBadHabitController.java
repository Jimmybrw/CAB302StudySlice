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

public class WrappedBadHabitController {

    @FXML
    private Label badHabitLabel;

    @FXML
    private Label badHabitSupportLabel;

    @FXML
    private Button nextButton;

    @FXML
    public void initialize(){
        loadPlaceHolderData();
        playRevealAnimation();
        playNextButtonPulse();
    }

    private void loadPlaceHolderData() {
        badHabitLabel.setText("Messages");
        badHabitSupportLabel.setText(
                "This was the biggest interruption in your session and and had the strongest effect on your focus score."
        );
    }

    // -----------------------------
    // ANIMATION
    // -----------------------------

    private void playRevealAnimation() {
        badHabitLabel.setOpacity(0);
        badHabitLabel.setScaleX(0.75);
        badHabitLabel.setScaleY(0.75);

        badHabitSupportLabel.setOpacity(0);
        badHabitSupportLabel.setTranslateY(16);

        nextButton.setOpacity(0);
        nextButton.setTranslateY(14);

        FadeTransition scoreFade = new FadeTransition(Duration.millis(550), badHabitLabel);
        scoreFade.setFromValue(0);
        scoreFade.setToValue(1);

        ScaleTransition scoreScale = new ScaleTransition(Duration.millis(550), badHabitLabel);
        scoreScale.setFromX(0.75);
        scoreScale.setFromY(0.75);
        scoreScale.setToX(1.0);
        scoreScale.setToY(1.0);

        ParallelTransition scoreReveal = new ParallelTransition(scoreFade, scoreScale);

        FadeTransition supportFade = new FadeTransition(Duration.millis(450), badHabitSupportLabel);
        supportFade.setFromValue(0);
        supportFade.setToValue(1);

        TranslateTransition supportSlide = new TranslateTransition(Duration.millis(450), badHabitSupportLabel);
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

    @FXML
    private void onBackClick() {
        ViewManager.switchScene("wrapped-focusScore-view.fxml");
    }

    @FXML
    private void onNextClick() {
        ViewManager.switchScene("wrapped-goodHabit-view.fxml");
    }
}
