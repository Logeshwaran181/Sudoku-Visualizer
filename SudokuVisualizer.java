import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SudokuVisualizer extends JFrame implements Visualizer {
    private static final int SIZE = 9;
    private static final int GRID_SIZE = 50;
    private static final int TOTAL_TIME_MS = 2 * 60 * 1000; // 2 minutes
    private int[][] board;
    private JPanel panel;
    private JButton solveButton;
    private JTextField[][] fields;

    public SudokuVisualizer(int[][] board) {
        this.board = board;
        this.fields = new JTextField[SIZE][SIZE];
        panel = new JPanel(new GridLayout(SIZE, SIZE));
        setUpBoard();

        solveButton = new JButton("Solve");
        solveButton.setBackground(Color.GRAY);
        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    SudokuSolver solver = new SudokuSolver(SudokuVisualizer.this, TOTAL_TIME_MS);
                    solver.solveSudoku(board);
                }).start();
            }
        });

        this.setLayout(new BorderLayout());
        this.add(panel, BorderLayout.CENTER);
        this.add(solveButton, BorderLayout.SOUTH);
        this.setSize(SIZE * GRID_SIZE, SIZE * GRID_SIZE + 50);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    private void setUpBoard() {
        panel.removeAll();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                JTextField field = new JTextField();
                field.setHorizontalAlignment(JTextField.CENTER);
                field.setFont(new Font("Arial", Font.BOLD, 20));
                if (board[row][col] != 0) {
                    field.setText(String.valueOf(board[row][col]));
                    field.setEditable(false);
                    field.setBackground(Color.LIGHT_GRAY);
                } else {
                    field.setEditable(true);
                    field.setBackground(Color.WHITE);
                }
                fields[row][col] = field;
                panel.add(field);
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    @Override
    public void visualizeBoard(int[][] newBoard) {
        SwingUtilities.invokeLater(() -> {
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    JTextField field = fields[row][col];
                    if (newBoard[row][col] != 0) {
                        field.setText(String.valueOf(newBoard[row][col]));
                        field.setBackground(Color.GRAY);
                    } else {
                        field.setText("");
                        field.setBackground(Color.WHITE);
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        int[][] board = {
            {5, 3, 0, 0, 7, 0, 0, 0, 0},
            {6, 0, 0, 1, 9, 5, 0, 0, 0},
            {0, 9, 8, 0, 0, 0, 0, 6, 0},
            {8, 0, 0, 0, 6, 0, 0, 0, 3},
            {4, 0, 0, 8, 0, 3, 0, 0, 1},
            {7, 0, 0, 0, 2, 0, 0, 0, 6},
            {0, 6, 0, 0, 0, 0, 2, 8, 0},
            {0, 0, 0, 4, 1, 9, 0, 0, 5},
            {0, 0, 0, 0, 8, 0, 0, 7, 9}
        };

        new SudokuVisualizer(board);
    }
}

interface Visualizer {
    void visualizeBoard(int[][] newBoard);
}

class SudokuSolver {
    private Visualizer visualizer;
    private long startTime;
    private long endTime;
    private long duration;
    private static final int MINIMUM_DELAY = 10;

    public SudokuSolver(Visualizer visualizer, long durationMillis) {
        this.visualizer = visualizer;
        this.duration = durationMillis;
    }

    public void solveSudoku(int[][] board) {
        startTime = System.currentTimeMillis();
        endTime = startTime + duration;
        solve(board);
    }

    private boolean solve(int[][] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    for (int num = 1; num <= 9; num++) {
                        if (isSafe(board, row, col, num)) {
                            board[row][col] = num;
                            visualizer.visualizeBoard(board);
                            try {
                                long currentTime = System.currentTimeMillis();
                                long timeRemaining = endTime - currentTime;
                                long delay = 5;
                                Thread.sleep(delay);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (solve(board)) {
                                return true;
                            } else {
                                board[row][col] = 0; // backtrack
                                visualizer.visualizeBoard(board);
                            }
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isSafe(int[][] board, int row, int col, int num) {
        for (int d = 0; d < 9; d++) {
            if (board[row][d] == num || board[d][col] == num) {
                return false;
            }
        }

        int sqrt = (int) Math.sqrt(9);
        int boxRowStart = row - row % sqrt;
        int boxColStart = col - col % sqrt;

        for (int r = boxRowStart; r < boxRowStart + sqrt; r++) {
            for (int d = boxColStart; d < boxColStart + sqrt; d++) {
                if (board[r][d] == num) {
                    return false;
                }
            }
        }

        return true;
    }
}
