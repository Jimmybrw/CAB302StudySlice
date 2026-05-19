package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.WrappedDataHolder;
import com.example.cab302studyslice.View.ViewManager;
import javafx.animation.*;
import javafx.scene.Group;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class WrappedGoodHabitController {
    @FXML
    private Label goodHabitLabel;

    @FXML
    private Label goodHabitSupportLabel;

    @FXML
    private Group smileyFaceGroup;

    @FXML
    private Button nextButton;

    @FXML
    public void initialize(){
        loadData();
        playSmileFaceIntro();
        playRevealAnimation();
    }

    private void loadData() {
        if (WrappedDataHolder.hasData()) {
            goodHabitLabel.setText(WrappedDataHolder.getGoodHabit());
            goodHabitSupportLabel.setText(
                    "Your strongest habit was staying focused in one work stream for a longer stretch."
            );
        } else {
            goodHabitLabel.setText("Staying in one task");
            goodHabitSupportLabel.setText(
                    "Your strongest habit was staying focused in one work stream for a longer stretch."
            );
        }
    }

    // -----------------------------
    // ANIMATION
    // -----------------------------

    private void playSmileFaceIntro() {
        smileyFaceGroup.setScaleX(2.8);
        smileyFaceGroup.setScaleY(2.8);

        smileyFaceGroup.setOpacity(0);
        smileyFaceGroup.setTranslateY(260);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(350),  smileyFaceGroup);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(0.22);

        TranslateTransition riseIn = new TranslateTransition(Duration.millis(650),  smileyFaceGroup);
        riseIn.setFromY(260);
        riseIn.setToY(0);

        RotateTransition wobble = new RotateTransition(Duration.millis(180),  smileyFaceGroup);
        wobble.setFromAngle(-4);
        wobble.setToAngle(4);
        wobble.setCycleCount(2);
        wobble.setAutoReverse(true);

        // ParallelTransition intro = new ParallelTransition(fadeIn, riseIn);
        // intro.play();
        SequentialTransition introSequence = new SequentialTransition(
                new ParallelTransition(fadeIn, riseIn),
                wobble
        );
        introSequence.play();
    }

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
        supportSlide.setFromY(16);
        supportSlide.setToY(0);

        ParallelTransition supportReveal = new ParallelTransition(supportFade, supportSlide);

        FadeTransition buttonFade = new FadeTransition(Duration.millis(400), nextButton);
        buttonFade.setFromValue(0);
        buttonFade.setToValue(1);

        TranslateTransition buttonSlide = new TranslateTransition(Duration.millis(400), nextButton);
        buttonSlide.setFromY(14);
        buttonSlide.setToY(0);

        ParallelTransition buttonReveal = new ParallelTransition(buttonFade, buttonSlide);

        SequentialTransition sequence = new SequentialTransition(scoreReveal, supportReveal, buttonReveal);
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

    private void playSmileyFaceExit() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(500), smileyFaceGroup);
        slideOut.setToY(-220);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(420), smileyFaceGroup);
        fadeOut.setFromValue(smileyFaceGroup.getOpacity());
        fadeOut.setToValue(0);

        ParallelTransition outro = new ParallelTransition(slideOut, fadeOut);
        outro.setOnFinished(event -> ViewManager.switchScene("wrapped-comparison-view.fxml"));
        outro.play();
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
        playSmileyFaceExit();
    }
}
