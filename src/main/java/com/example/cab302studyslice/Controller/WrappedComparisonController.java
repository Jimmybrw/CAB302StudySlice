package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.AiAPI;
import com.example.cab302studyslice.Model.WrappedDataHolder;
import com.example.cab302studyslice.View.ViewManager;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.util.Duration;

/**
 * Controller for the wrapped comparison page.
 * This page compares the current study session to the user's previous saved sessions
 * and presents a short performance summary as part of the wrapped flow.
 */
public class WrappedComparisonController {
    @FXML
    private Label comparisonLabel;
    @FXML
    private Label comparisonSupportLabel;
    @FXML
    private Button nextButton;
    @FXML
    private Region animatedOverlay;

    /**
     * Initialises the wrapped comparison page by loading wrapped session data,
     * starting the animated background, and revealing the page content.
     */
    @FXML
    public void initialize(){
        loadData();
        animateBackground();
        playIntroAnimation();
    }

    /**
     * Loads comparison data from the wrapped session summary and updates the
     * page labels to reflect whether the session performed above or below
     * the user's average. Falls back to placeholder content if wrapped data
     * is unavailable.
     */
    private void loadData() {
        if (WrappedDataHolder.hasData()) {
            AiAPI.WrappedData data = WrappedDataHolder.getWrappedData();
            boolean isGood = "good".equalsIgnoreCase(data.comparedToSessions);
            comparisonLabel.setText(isGood ? "Better than average" : "Below average");
            comparisonSupportLabel.setText(isGood
                    ? "This session outperformed most of your saved sessions. Consistency like this adds up fast."
                    : "This session was a bit below your usual standard — every session is a chance to improve.");
        } else {
            comparisonLabel.setText("Better than average");
            comparisonSupportLabel.setText("This session outperformed most of your saved sessions.");
        }
    }

    /**
     * Animates the decorative background overlay to create subtle movement
     * and depth behind the wrapped page content.
     */
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

    /**
     * Reveals the comparison heading, support text, and next button
     * in sequence after the page content has been prepared.
     */
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
     * Navigates back to the wrapped good habit page.
     */
    @FXML
    private void onBackClick() {
        ViewManager.switchScene("wrapped-goodHabit-view.fxml");
    }

    /**
     * Navigates to the wrapped ranking page.
     */
    @FXML
    private void onNextClick() {
        ViewManager.switchScene("wrapped-ranking-view.fxml");
    }
}
