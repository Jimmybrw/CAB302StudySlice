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

/**
 * Controller for the wrapped ranking page.
 * This page shows how the current study session ranks against the user's
 * previous saved sessions based on focus score, using a rolling countdown
 * animation before revealing the final placement.
 */
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

    /**
     * Initialises the wrapped ranking page by starting the background animation
     * and loading either real wrapped ranking data or placeholder content.
     */
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

    /**
     * Loads ranking data from the wrapped session summary and updates the
     * support text for the selected session before starting the rolling
     * rank reveal animation.
     */
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

    /**
     * Loads placeholder support text for the ranking page when wrapped
     * session data is unavailable.
     */
    private void loadPlaceHolderData() {
        rankingSupportLabel.setText("Out of 10 saved sessions, this was your 2nd most focused study session.");
    }

    /**
     * Builds 5 step values for the rolling count-down animation.
     * ALL steps are plain numbers — ordinals are too long for the label at large font sizes.
     * steps[0] = total (start)
     * steps[1..3] = intermediate values counting down
     * steps[4] = finalRank (end) — the ordinal already appears in the support text below
     *
     * @param total the total number of saved sessions being compared
     * @param finalRank the final rank of the current session
     * @return an array of five numeric strings used in the rolling animation
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

    /**
     * Converts a ranking number into its ordinal string form.
     *
     * @param n the ranking number
     * @return the ordinal representation of the number, such as 1st, 2nd, or 3rd
     */
    private String ordinalSuffix(int n) {
        if (n >= 11 && n <= 13) return n + "th";
        switch (n % 10) {
            case 1:  return n + "st";
            case 2:  return n + "nd";
            case 3:  return n + "rd";
            default: return n + "th";
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
     * Plays the rolling rank animation by moving through a sequence of
     * descending numeric values before landing on the final session rank.
     *
     * @param ranks the sequence of rank values used for the rolling reveal
     */
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

    /**
     * Plays a short emphasis animation once the final rank value has landed.
     */
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

    /**
     * Reveals the support text and next button after the final ranking
     * value has been displayed.
     */
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
     * Navigates back to the wrapped comparison page.
     */
    @FXML
    private void onBackClick() {
        ViewManager.switchScene("wrapped-comparison-view.fxml");
    }

    /**
     * Navigates to the wrapped streak page.
     */
    @FXML
    private void onNextClick() {
        ViewManager.switchScene("wrapped-streak-view.fxml");
    }
}

