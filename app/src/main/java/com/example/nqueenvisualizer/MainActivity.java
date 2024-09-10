package com.example.nqueenvisualizer;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GridLayout boardLayout;
    private Button[][] boardButtons;
    private int boardSize = 8; // Default size
    private Spinner boardSizeSpinner;
    private int[] queens; // Array to store queen positions
    private boolean solutionFound = false;
    private Handler uiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boardLayout = findViewById(R.id.board);
        boardSizeSpinner = findViewById(R.id.boardSizeSpinner);
        Button solveButton = findViewById(R.id.solveButton);
        uiHandler = new Handler(Looper.getMainLooper());

        // Set up board size spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.board_sizes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        boardSizeSpinner.setAdapter(adapter);

        // Set initial board size
        updateBoardSize();

        // Listener for board size change
        boardSizeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateBoardSize();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Do nothing
            }
        });

        solveButton.setOnClickListener(v -> {
            queens = new int[boardSize];
            for (int i = 0; i < boardSize; i++) {
                queens[i] = -1;
            }
            solutionFound = false;
            clearBoard();

            // Start the solver in a new thread
            new Thread(new NQueensSolver(0)).start();
        });
    }

    private void updateBoardSize() {
        String selectedSize = boardSizeSpinner.getSelectedItem().toString();
        boardSize = Integer.parseInt(selectedSize);

        // Clear the previous board
        boardLayout.removeAllViews();
        boardButtons = new Button[boardSize][boardSize];

        // Create a new board
        boardLayout.setColumnCount(boardSize);
        boardLayout.setRowCount(boardSize);
        int size = 600 / boardSize; // Adjust size based on the boardSize
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                boardButtons[i][j] = new Button(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = size;
                params.height = size;
                params.columnSpec = GridLayout.spec(j);
                params.rowSpec = GridLayout.spec(i);
                boardButtons[i][j].setLayoutParams(params);
                boardButtons[i][j].setBackgroundColor((i + j) % 2 == 0 ? Color.WHITE : Color.LTGRAY);
                boardLayout.addView(boardButtons[i][j]);
            }
        }
    }

    private void clearBoard() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                boardButtons[i][j].setBackgroundColor((i + j) % 2 == 0 ? Color.WHITE : Color.LTGRAY);
            }
        }
    }

    private boolean isSafe(int row, int col) {
        for (int i = 0; i < row; i++) {
            if (queens[i] == col || Math.abs(queens[i] - col) == Math.abs(i - row)) {
                return false;
            }
        }
        return true;
    }

    private class NQueensSolver implements Runnable {
        private int row;

        NQueensSolver(int startRow) {
            this.row = startRow;
        }

        @Override
        public void run() {
            solveNQueens(row);
        }

        private void solveNQueens(int row) {
            if (row >= boardSize) {
                solutionFound = true;
                uiHandler.post(MainActivity.this::displaySolution);
                return;
            }

            for (int col = 0; col < boardSize; col++) {
                if (isSafe(row, col)) {
                    queens[row] = col;

                    // Update the UI with a red queen
                    int finalCol2 = col;
                    uiHandler.post(() -> displayStep(row, finalCol2, Color.RED));
                    delay();
                    // Recursively solve for the next row
                    solveNQueens(row + 1);

                    // If a solution is found, no need to backtrack
                    if (solutionFound) {
                        return;
                    }

                    // Backtrack: remove the queen and update the UI
                    queens[row] = -1;
                    int finalCol = col;
                    int finalCol1 = col;
                    uiHandler.post(() -> displayStep(row, finalCol, (row + finalCol1) % 2 == 0 ? Color.WHITE : Color.LTGRAY));
                    delay();
                }
            }
        }
    }
    private  void delay(){
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void displayStep(int row, int col, int color) {
        boardButtons[row][col].setBackgroundColor(color);
    }

    private void displaySolution() {
        Toast.makeText(this, "Solution Found", Toast.LENGTH_SHORT).show();
    }
}
