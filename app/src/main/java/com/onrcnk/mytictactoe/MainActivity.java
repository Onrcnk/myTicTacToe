package com.onrcnk.mytictactoe;

import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean playerOneActive;
    private TextView playerOneScore, playerTwoScore, playerStatus, textWinner;
    private Button[] buttons = new Button[9];
    private Button reset, playAgain;
    private static final int PLAYER_ONE = 0;
    private static final int PLAYER_TWO = 1;
    private static final int EMPTY = 2;

    private int[] gameState = {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY};
    private final int[][] winningPositions = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
            {0, 4, 8}, {2, 4, 6}
    };

    private int rounds;
    private int playerOneScoreCount, playerTwoScoreCount;
    private final List<Integer> playerOneMoves = new ArrayList<>();
    private final List<Integer> playerTwoMoves = new ArrayList<>();

    private SoundPool soundPool;
    private int soundX, soundO;

    private boolean isGameActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button infoButtonGame = findViewById(R.id.btnInfoGame);
        infoButtonGame.setBackgroundTintList(
                ContextCompat.getColorStateList(this, R.color.info_color));

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build();
        soundX = soundPool.load(this, R.raw.soundx, 1);
        soundO = soundPool.load(this, R.raw.soundo, 1);

        playerOneScore = findViewById(R.id.score_Player1);
        playerTwoScore = findViewById(R.id.score_Player2);
        playerStatus = findViewById(R.id.textStatus);
        reset = findViewById(R.id.btn_reset);
        playAgain = findViewById(R.id.btn_play_again);
        textWinner = findViewById(R.id.textWinner);

        buttons[0] = findViewById(R.id.btn0);
        buttons[1] = findViewById(R.id.btn1);
        buttons[2] = findViewById(R.id.btn2);
        buttons[3] = findViewById(R.id.btn3);
        buttons[4] = findViewById(R.id.btn4);
        buttons[5] = findViewById(R.id.btn5);
        buttons[6] = findViewById(R.id.btn6);
        buttons[7] = findViewById(R.id.btn7);
        buttons[8] = findViewById(R.id.btn8);

        for (Button button : buttons) {
            button.setOnClickListener(this);
        }

        playerOneScoreCount = 0;
        playerTwoScoreCount = 0;
        playerOneActive = true;
        rounds = 0;

        reset.setOnClickListener(view -> resetGame());
        playAgain.setOnClickListener(view -> playAgain());

        playAgain.setVisibility(View.INVISIBLE);
        updateGameStatus();
    }

    @Override
    public void onClick(View view) {
        Button clickedButton = (Button) view;
        String buttonID = view.getResources().getResourceEntryName(view.getId());
        int gameStatePointer = Integer.parseInt(buttonID.substring(3));

        if (!clickedButton.getText().toString().equals("")) {
            return;
        }

        playBounceAnimation(clickedButton);

        if (playerOneActive) {
            playSoundEffect(soundX);
            makeMove(clickedButton, gameStatePointer, PLAYER_ONE, playerOneMoves, R.color.player_one_color);
        } else {
            playSoundEffect(soundO);
            makeMove(clickedButton, gameStatePointer, PLAYER_TWO, playerTwoMoves, R.color.player_two_color);
        }

        boolean isWinner = checkWinner();

        if (isWinner) {
            isGameActive = false; // Animasyonları devre dışı bırak
            if (playerOneActive) {
                playerOneScoreCount++;
                updatePlayerScore();
                handleWinningCombination(playerOneMoves);

                if (playerOneScoreCount == 2) {
                    textWinner.setText("Player X Win!");
                    textWinner.setVisibility(View.VISIBLE);
                    playAgain.setVisibility(View.VISIBLE);
                    reset.setVisibility(View.INVISIBLE);
                    updateGameStatus();
                    disableAllButtons();
                    animateWinnerText();
                    return;
                }
            } else {
                playerTwoScoreCount++;
                updatePlayerScore();
                handleWinningCombination(playerTwoMoves);

                if (playerTwoScoreCount == 2) {
                    textWinner.setText("Player O Win!");
                    textWinner.setVisibility(View.VISIBLE);
                    playAgain.setVisibility(View.VISIBLE);
                    reset.setVisibility(View.INVISIBLE);
                    updateGameStatus();
                    disableAllButtons();
                    animateWinnerText();
                    return;
                }
            }

        }

        if (!isWinner) {
            playerOneActive = !playerOneActive;
        }
        updateGameStatus();
    }

    private void playSoundEffect(int soundId) {
        soundPool.play(soundId, 1, 1, 0, 0, 1);
    }

    private void playBounceAnimation(Button button) {
        if (isGameActive) { // Sadece oyun aktifse animasyon oynat
            Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce_animation);
            button.startAnimation(bounce);
        }
    }

    private void makeMove(Button button, int position, int player, List<Integer> moves, int color) {
        button.setText(player == PLAYER_ONE ? "X" : "O");
        button.setTextColor(getResources().getColor(color));
        gameState[position] = player;
        moves.add(position);

        if (moves.size() > 3) {
            int oldestMove = moves.remove(0);
            gameState[oldestMove] = EMPTY;
            buttons[oldestMove].setText("");
        }

        rounds++;
    }

    private boolean checkWinner() {
        for (int[] position : winningPositions) {
            if (gameState[position[0]] == gameState[position[1]] &&
                    gameState[position[1]] == gameState[position[2]] &&
                    gameState[position[0]] != EMPTY) {
                return true;
            }
        }
        return false;
    }

    private void handleWinningCombination(List<Integer> winningPlayerMoves) {
        for (int[] position : winningPositions) {
            if (gameState[position[0]] == gameState[position[1]] &&
                    gameState[position[1]] == gameState[position[2]] &&
                    gameState[position[0]] != EMPTY) {

                for (int index : position) {
                    gameState[index] = EMPTY;
                    buttons[index].setText("");
                    buttons[index].setEnabled(true);
                    winningPlayerMoves.remove(Integer.valueOf(index));
                }
            }
        }
    }

    private void disableAllButtons() {
        for (Button button : buttons) {
            button.setEnabled(false);
            button.setText("");
            button.setVisibility(View.INVISIBLE);
        }
    }

    private void animateWinnerText() {
        Animation winnerAnimation = AnimationUtils.loadAnimation(this, R.anim.winner_animation);
        textWinner.startAnimation(winnerAnimation);
    }

    private void playAgain() {
        isGameActive = true;
        rounds = 0;
        playerOneActive = true;
        playerOneScoreCount = 0;
        playerTwoScoreCount = 0;
        playerOneMoves.clear();
        playerTwoMoves.clear();
        textWinner.clearAnimation();
        textWinner.setVisibility(View.GONE);

        for (int i = 0; i < buttons.length; i++) {
            gameState[i] = EMPTY;
            buttons[i].setText("");
            buttons[i].setEnabled(true);
            buttons[i].setVisibility(View.VISIBLE);
        }

        playAgain.setVisibility(View.INVISIBLE);
        reset.setVisibility(View.VISIBLE);
        updateGameStatus();
        updatePlayerScore();
    }
    private void resetGame() {
        playAgain();
        playerOneScoreCount = 0;
        playerTwoScoreCount = 0;
        updatePlayerScore();
    }

    private void updatePlayerScore() {
        playerOneScore.setText(String.valueOf(playerOneScoreCount));
        playerTwoScore.setText(String.valueOf(playerTwoScoreCount));
    }

    private void updateGameStatus() {
        playerStatus.setText(playerOneScoreCount + "   -   " + playerTwoScoreCount);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release(); // SoundPool kaynaklarını serbest bırak
    }
}
