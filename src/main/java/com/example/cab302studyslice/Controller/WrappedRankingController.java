package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.AiAPI;
import com.example.cab302studyslice.Model.WrappedDataHolder;
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
        animateBackground();
        if (WrappedDataHolder.hasData()) {
            loadRealData();
        } else {
            loadPlaceHolderData();
            playRankingRevealAnimation(buildRankSteps(10, 2));
        }
    }

    private void loadRealData() {
        AiAPI.WrappedData data = WrappedDataHolder.getWrappedData();
        int rank  = Math.max(1, data.ranking);
        int total = Math.max(rank, data.totalSessions > 0 ? data.totalSessions : 1);
        String rankStr = ordinalSuffix(rank);
        rankingSupportLabel.setText(
                "Out of " + total + " saved sessions, this was your " + rankStr + " most focused study session."
        );
        playRankingRevealAnimation(buildRankSteps(total, rank));
    }

    private void loadPlaceHolderData() {
        rankingSupportLabel.setText("Out of 10 saved sessions, this was your 2nd most focused study session.");
    }

    /**
     * Builds 5 step values for the rolling count-down animation.
     * ALL steps are plain numbers — ordinals are too long for the label at large font sizes.
     * steps[0] = total (start)
     * steps[1..3] = intermediate values counting down
     * steps[4] = finalRank (end) — the ordinal already appears in the support text below
     */
    private String[] buildRankSteps(int total, int finalRank) {
        String[] steps = new String[5];
        steps[0] = String.valueOf(total);

        if (total <= 1 || total == finalRank) {
            // Edge case: ranked last or only one session — nothing to count down
            for (int i = 1; i <= 4; i++) steps[i] = String.valueOf(finalRank);
            return steps;
        }

        int range = total - finalRank;
        for (int i = 1; i <= 3; i++) {
            int value = total - (int) Math.round((double) range * i / 4.0);
            value = Math.max(value, finalRank + 1);
            value = Math.min(value, total - 1);
            steps[i] = String.valueOf(value);
        }
        steps[4] = String.valueOf(finalRank);  // plain number — ordinal is in the support label
        return steps;
    }

    private String ordinalSuffix(int n) {
        if (n >= 11 && n <= 13) return n + "th";
        switch (n % 10) {
            case 1:  return n + "st";
            case 2:  return n + "nd";
            case 3:  return n + "rd";
            default: return n + "th";
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

    private void playRankingRevealAnimation(String[] ranks) {

        rankingSupportLabel.setOpacity(0);
        rankingSupportLabel.setTranslateY(16);

        nextButton.setOpacity(0);
        nextButton.setTranslateY(14);

        // Start at ranks[0] (the total sessions count) and roll DOWN to the final rank
        rankingLabelCurrent.setText(ranks[0]);
        rankingLabelCurrent.setOpacity(1);
        rankingLabelCurrent.setTranslateY(0);

        rankingLabelNext.setOpacity(0);
        rankingLabelNext.setTranslateY(-70);

        SequentialTransition fullSequence = new SequentialTransition();

        for (int i = 1; i < ranks.length; i++) {
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

