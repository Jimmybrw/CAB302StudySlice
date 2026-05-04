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

public class WrappedGoodHabitController {
    @FXML
    private Label goodHabitLabel;

    @FXML
    private Label goodHabitSupportLabel;

    @FXML
    private Button nextButton;

    @FXML
    public void initialize(){
        loadPlaceHolderData();
        playRevealAnimation();
        playNextButtonPulse();
    }

    private void loadPlaceHolderData() {
        goodHabitLabel.setText("Staying in one task");
        goodHabitSupportLabel.setText(
                "Your strongest habit was staying focused in one work stream for a longer stretch."
        );
    }

    // -----------------------------
    // ANIMATION
    // -----------------------------

    private void playRevealAnimation() {
        goodHabitLabel.setOpacity(0);
        goodHabitLabel.setScaleX(0.75);
        goodHabitLabel.setScaleY(0.75);

        goodHabitSupportLabel.setOpacity(0);
        goodHabitSupportLabel.setTranslateY(16);

        nextButton.setOpacity(0);
        nextButton.setTranslateY(14);

        FadeTransition scoreFade = new FadeTransition(Duration.millis(550), goodHabitLabel);
        scoreFade.setFromValue(0);
        scoreFade.setToValue(1);

        ScaleTransition scoreScale = new ScaleTransition(Duration.millis(550), goodHabitLabel);
        scoreScale.setFromX(0.75);
        scoreScale.setFromY(0.75);
        scoreScale.setToX(1.0);
        scoreScale.setToY(1.0);

        ParallelTransition scoreReveal = new ParallelTransition(scoreFade, scoreScale);

        FadeTransition supportFade = new FadeTransition(Duration.millis(450), goodHabitSupportLabel);
        supportFade.setFromValue(0);
        supportFade.setToValue(1);

        TranslateTransition supportSlide = new TranslateTransition(Duration.millis(450), goodHabitSupportLabel);
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
        ViewManager.switchScene("wrapped-badHabit-view.fxml");
    }

    @FXML
    private void onNextClick() {
        ViewManager.switchScene("wrapped-sessionRanking-view.fxml");
    }
}
