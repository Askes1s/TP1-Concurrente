package util;

public class seatMatrix {
    private final int rows;
    private final int cols;
    private final Seat[][] matrix;

    public seatMatrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        matrix = new Seat[rows][cols];
        setSeatNumbers();
    }

    public void setSeatNumbers() {
        int k = 1;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = new Seat(k);
                k++;
            }
        }
    }

    public Seat getSeat(int row, int col) {
        return matrix[row][col];
    }

    public int getSeatsAmount() {
        return rows*cols;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }
}


