package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.View.ViewManager;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.Pane;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class WrapperTotalTimeController {

    private Rotate hourHandRotate;
    private Rotate minuteHandRotate;

    @FXML
    private Label totalTimeLabel;

    @FXML
    private Label supportTextLabel;

    @FXML
    private Button nextButton;

    @FXML
    private javafx.scene.layout.Region animatedOverlay;

    @FXML
    private Pane clockContainer;

    @FXML
    private Group hourHandGroup;

    @FXML
    private Group minuteHandGroup;

    @FXML
    private void initialize(){
        loadPlaceHolderData();
        setUpClockHands();
        animateBackground();
        playClockThenRevealAnimation();
        playNextButtonPulse();
    }

    // Placeholder data, replace later with actual data
    private void loadPlaceHolderData(){
        totalTimeLabel.setText("5 hours and 15 minutes");
        supportTextLabel.setText("That is a serious study stretch.Your future self probably owes you a snack.");
    }

    // set angle and rotation for clock hands
    private void setUpClockHands(){
        hourHandRotate = new Rotate(0, 115, 115);
        minuteHandRotate = new Rotate(0, 115, 115);

        hourHandGroup.getTransforms().add(hourHandRotate);
        minuteHandGroup.getTransforms().add(minuteHandRotate);

        hourHandRotate.setAngle(10);
        minuteHandRotate.setAngle(0);
    }

    // -----------------------------
    // ANIMATION
    // -----------------------------

    // get background colors to subtly swirl
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

    // animate text appearance
    private void playRevealAnimation() {
        totalTimeLabel.setOpacity(0);
        totalTimeLabel.setScaleX(0.85);
        totalTimeLabel.setScaleY(0.85);

        supportTextLabel.setOpacity(0);
        supportTextLabel.setTranslateY(16);
        supportTextLabel.setScaleX(1.0);
        supportTextLabel.setScaleY(1.0);

        nextButton.setOpacity(0);
        nextButton.setTranslateY(14);

        FadeTransition timeFade = new FadeTransition(Duration.millis(550), totalTimeLabel);
        timeFade.setFromValue(0);
        timeFade.setToValue(1);

        ScaleTransition timeScale = new ScaleTransition(Duration.millis(550), totalTimeLabel);
        timeScale.setFromX(0.85);
        timeScale.setFromY(0.85);
        timeScale.setToX(1.0);
        timeScale.setToY(1.0);

        ParallelTransition timeReveal = new ParallelTransition(timeFade, timeScale);

        FadeTransition supportFade = new FadeTransition(Duration.millis(450), supportTextLabel);
        supportFade.setFromValue(0);
        supportFade.setToValue(1);

        TranslateTransition supportSlide = new TranslateTransition(Duration.millis(450), supportTextLabel);
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

        SequentialTransition sequence = new SequentialTransition(timeReveal, supportReveal, buttonReveal);

        sequence.play();
    }

    // display animation of clock before revealing total time
    private void playClockThenRevealAnimation() {
        totalTimeLabel.setOpacity(0);
        totalTimeLabel.setVisible(false);
        totalTimeLabel.setManaged(false);

        supportTextLabel.setOpacity(0);
        supportTextLabel.setTranslateY(16);

        nextButton.setOpacity(0);
        nextButton.setTranslateY(14);

        clockContainer.setOpacity(0);
        clockContainer.setScaleX(0.8);
        clockContainer.setScaleY(0.8);

        FadeTransition clockFadeIn = new FadeTransition(Duration.millis(100), clockContainer);
        clockFadeIn.setFromValue(0);
        clockFadeIn.setToValue(1);

        ScaleTransition clockScaleIn = new ScaleTransition(Duration.millis(100), clockContainer);
        clockScaleIn.setFromX(0.8);
        clockScaleIn.setFromY(0.8);
        clockScaleIn.setToX(1.0);
        clockScaleIn.setToY(1.0);

        ParallelTransition clockIntro = new ParallelTransition(clockFadeIn, clockScaleIn);

        Timeline tickingAnimation = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(minuteHandRotate.angleProperty(), 0),
                        new KeyValue(hourHandRotate.angleProperty(), 10)
                ),
                new KeyFrame(
                        Duration.seconds(0.7),
                        new KeyValue(minuteHandRotate.angleProperty(), 40),
                        new KeyValue(hourHandRotate.angleProperty(), 5)
                )
        );

        PauseTransition pause = new PauseTransition(Duration.millis(50));

        FadeTransition clockFadeOut = new FadeTransition(Duration.millis(70), clockContainer);
        clockFadeOut.setFromValue(1);
        clockFadeOut.setToValue(0);

        ScaleTransition clockScaleOut = new ScaleTransition(Duration.millis(70), clockContainer);
        clockScaleOut.setFromX(1.0);
        clockScaleOut.setFromY(1.0);
        clockScaleOut.setToX(0.75);
        clockScaleOut.setToY(0.75);

        ParallelTransition clockOutro = new ParallelTransition(clockFadeOut, clockScaleOut);

        clockOutro.setOnFinished(event -> {
            clockContainer.setVisible(false);
            clockContainer.setManaged(false);

            totalTimeLabel.setVisible(true);
            totalTimeLabel.setManaged(true);

            playRevealAnimation();
        });

        SequentialTransition sequence = new SequentialTransition(
                clockIntro, tickingAnimation, pause, clockOutro
        );

        sequence.play();
    }

    // pulse the next button to prompt user to the next page
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

    // go back to first wrapped page
    @FXML
    private void onBackClick() {
        ViewManager.switchScene("wrapped-intro-view.fxml");
    }

    // go to next wrapped page
    @FXML
    private void onNextClick() {
        ViewManager.switchScene("wrapped-TopApp-view.fxml");
    }

}
