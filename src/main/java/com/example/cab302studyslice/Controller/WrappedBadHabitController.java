package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.AiAPI;
import com.example.cab302studyslice.Model.WrappedDataHolder;
import com.example.cab302studyslice.View.ViewManager;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.Group;

/**
 * Controller for the wrapped bad habit page.
 * This page highlights the user's biggest focus blocker or distraction
 * from the selected study session and presents it as part of the wrapped summary flow.
 */
public class WrappedBadHabitController {

    @FXML
    private Label badHabitLabel;

    @FXML
    private Label badHabitSupportLabel;

    @FXML
    private Group frownFaceGroup;

    @FXML
    private Button nextButton;

    /**
     * Initialises the wrapped bad habit page by loading wrapped session data,
     * playing the decorative face animation, and revealing the page content.
     */
    @FXML
    public void initialize(){
        loadData();
        playFrownFaceIntro();
        playRevealAnimation();
    }

    /**
     * Loads the detected bad habit or focus blocker from wrapped session data
     * and updates the page labels. Falls back to placeholder content if no
     * wrapped data is available.
     */
    private void loadData() {
        if (WrappedDataHolder.hasData()) {
            AiAPI.WrappedData data = WrappedDataHolder.getWrappedData();
            String habit = (data.badHabit != null && !data.badHabit.isBlank())
                    ? data.badHabit : "Frequent app switching";
            badHabitLabel.setText(habit);
            badHabitSupportLabel.setText(
                    "This was the biggest interruption in your session and had the strongest effect on your focus score."
            );
        } else {
            badHabitLabel.setText("Messages");
            badHabitSupportLabel.setText(
                    "This was the biggest interruption in your session and had the strongest effect on your focus score."
            );
        }
    }

    /**
     * Plays the introductory animation for the background frown face,
     * bringing it into view before the text content is shown.
     */
    private void playFrownFaceIntro() {
        frownFaceGroup.setScaleX(2.8);
        frownFaceGroup.setScaleY(2.8);

        frownFaceGroup.setOpacity(0);
        frownFaceGroup.setTranslateY(-260);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(350),  frownFaceGroup);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(0.22);

        TranslateTransition dropIn = new TranslateTransition(Duration.millis(650),  frownFaceGroup);
        dropIn.setFromY(-260);
        dropIn.setToY(0);

        RotateTransition wobble = new RotateTransition(Duration.millis(180),  frownFaceGroup);
        wobble.setFromAngle(-4);
        wobble.setToAngle(4);
        wobble.setCycleCount(2);
        wobble.setAutoReverse(true);

        SequentialTransition introSequence = new SequentialTransition(
                new ParallelTransition(fadeIn, dropIn),
                wobble
        );
        introSequence.play();
    }

    /**
     * Reveals the highlighted bad habit, support text, and next button
     * in sequence after the page content has been prepared.
     */
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

    /**
     * Plays a repeating pulse animation on the next button to encourage
     * the user to continue through the wrapped flow.
     */
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

    /**
     * Plays the exit animation for the background frown face before
     * navigating to the wrapped good habit page.
     */
    private void playFrownFaceExit() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(500), frownFaceGroup);
        slideOut.setToY(420);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(420), frownFaceGroup);
        fadeOut.setFromValue(frownFaceGroup.getOpacity());
        fadeOut.setToValue(0);

        ParallelTransition outro = new ParallelTransition(slideOut, fadeOut);
        outro.setOnFinished(event -> ViewManager.switchScene("wrapped-goodHabit-view.fxml"));
        outro.play();
    }

    /**
     * Navigates back to the wrapped focus score page.
     */
    @FXML
    private void onBackClick() {
        ViewManager.switchScene("wrapped-focusScore-view.fxml");
    }

    /**
     * Triggers the exit animation before moving to the wrapped good habit page.
     */
    @FXML
    private void onNextClick() { playFrownFaceExit();}
}
