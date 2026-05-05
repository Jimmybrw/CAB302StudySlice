package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.View.ViewManager;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.Group;

public class WrappedBadHabitController {

    @FXML
    private Label badHabitLabel;

    @FXML
    private Label badHabitSupportLabel;

    @FXML
    private Group frownFaceGroup;

    @FXML
    private Button nextButton;

    @FXML
    public void initialize(){
        loadPlaceHolderData();
        playFrownFaceIntro();
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

    private void playFrownFaceIntro() {
        frownFaceGroup.setScaleX(2.8);
        frownFaceGroup.setScaleY(2.8);

        frownFaceGroup.setOpacity(0.22);
        frownFaceGroup.setTranslateY(-260);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(350),  frownFaceGroup);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition dropIn = new TranslateTransition(Duration.millis(360),  frownFaceGroup);
        dropIn.setFromY(-260);
        dropIn.setToY(0);

        RotateTransition wobble = new RotateTransition(Duration.millis(180),  frownFaceGroup);
        wobble.setFromAngle(-4);
        wobble.setToAngle(4);
        wobble.setCycleCount(2);
        wobble.setAutoReverse(true);

        ParallelTransition intro = new ParallelTransition(fadeIn, dropIn);
        intro.play();
    }

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

    private void playFrownFaceExit() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(500), frownFaceGroup);
        slideOut.setToY(420);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(420), frownFaceGroup);

        ParallelTransition outro = new ParallelTransition(slideOut, fadeOut);
        outro.setOnFinished(event -> ViewManager.switchScene("wrapped-goodHabit-view.fxml"));
        outro.play();
    }

    // -----------------------------
    // NAVIGATION BUTTONS
    // -----------------------------

    @FXML
    private void onBackClick() {
        ViewManager.switchScene("wrapped-focusScore-view.fxml");
    }

    @FXML
    private void onNextClick() { playFrownFaceExit();}
}
