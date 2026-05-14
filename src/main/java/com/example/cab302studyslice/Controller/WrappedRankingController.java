package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.View.ViewManager;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class WrappedRankingController {
    @FXML
    private Region animatedOverlay;
    @FXML
    private Label rankingLabelCurrent;
    @FXML
    private Label rankingLabelNext;
    @FXML
    private Label rankingSupportLabel;
    @FXML
    private Button nextButton;

    @FXML
    private void initialize() {
        loadPlaceHolderData();
        animateBackground();
        playRankingRevealAnimation("2nd");
        //playNextButtonPulse();
    }

    private void loadPlaceHolderData() {
        rankingLabelCurrent.setText("2nd");
        rankingLabelNext.setText("Out of the 10 saved sessions, this was your second most focused study session.");
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

    private void playRankingRevealAnimation(String finalRank) {
        String[] ranks = {"10", "7", "5", "3", finalRank};

        rankingSupportLabel.setOpacity(0);
        rankingSupportLabel.setTranslateY(16);

        nextButton.setOpacity(0);
        nextButton.setTranslateY(14);

        rankingLabelCurrent.setText(ranks[1]);
        rankingLabelCurrent.setOpacity(1);
        rankingLabelCurrent.setTranslateY(0);

        rankingLabelNext.setOpacity(0);
        rankingLabelNext.setTranslateY(0);

        SequentialTransition fullSequence = new SequentialTransition();

        for (int i = 0; i < ranks.length; i++) {
            String nextRank = ranks[i];

            PauseTransition step = new PauseTransition(Duration.ZERO);
            step.setOnFinished(event -> rankingLabelNext.setText(nextRank));

            rankingLabelNext.setTranslateY(-70);
            rankingLabelNext.setOpacity(0);

            TranslateTransition currentOut = new TranslateTransition(Duration.millis(140), rankingLabelCurrent);
            currentOut.setFromY(0);
            currentOut.setToY(70);

            FadeTransition currentFade = new FadeTransition(Duration.millis(140), rankingLabelCurrent);
            currentFade.setFromValue(1);
            currentFade.setToValue(0);

            ParallelTransition currentExit = new ParallelTransition(currentOut, currentFade);

            TranslateTransition nextIn = new TranslateTransition(Duration.millis(140), rankingLabelNext);
            nextIn.setFromY(-70);
            nextIn.setToY(0);

            FadeTransition nextFade = new FadeTransition(Duration.millis(140), rankingLabelNext);
            nextFade.setFromValue(0);
            nextFade.setToValue(1);

            ParallelTransition nextEnter = new ParallelTransition(nextIn, nextFade);

            ParallelTransition rollStep = new ParallelTransition(currentExit, nextEnter);

            PauseTransition commitStep = new PauseTransition(Duration.ZERO);
            commitStep.setOnFinished(event -> {
                rankingLabelCurrent.setText(nextRank);
                rankingLabelCurrent.setTranslateY(0);
                rankingLabelCurrent.setOpacity(1);

                rankingLabelNext.setOpacity(0);
                rankingLabelNext.setTranslateY(-70);
            });

            fullSequence.getChildren().addAll(step, rollStep, commitStep);
        }

        fullSequence.setOnFinished(e -> playFinalRankPop());
        fullSequence.play();
    }

    private void playFinalRankPop() {
        ScaleTransition popUp = new ScaleTransition(Duration.millis(160), rankingLabelCurrent);
        popUp.setFromX(1.0);
        popUp.setFromY(1.0);
        popUp.setToX(1.08);
        popUp.setToY(1.08);

        ScaleTransition settle = new ScaleTransition(Duration.millis(140), rankingLabelCurrent);
        settle.setFromX(1.08);
        settle.setFromY(1.08);
        settle.setToX(1.0);
        settle.setToY(1.0);

        SequentialTransition pop = new SequentialTransition(popUp, settle);
        pop.setOnFinished(event -> playRevealAnimation());
        pop.play();
    }

    private void playRevealAnimation() {
        rankingSupportLabel.setOpacity(0);
        rankingSupportLabel.setTranslateY(16);

        nextButton.setOpacity(0);
        nextButton.setTranslateY(14);

        FadeTransition supportFade = new FadeTransition(Duration.millis(450), rankingSupportLabel);
        supportFade.setFromValue(0);
        supportFade.setToValue(1);

        TranslateTransition supportSlide = new TranslateTransition(Duration.millis(450), rankingSupportLabel);
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

        SequentialTransition sequence = new SequentialTransition(supportReveal, buttonReveal);
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
        ViewManager.switchScene("wrapped-comparison-view.fxml");
    }

    @FXML
    private void onNextClick() {
        ViewManager.switchScene("wrapped-streak-view.fxml");
    }
}

