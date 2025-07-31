package nz.ara.ac.dtw0048.eyeballmaze.model;

public class MovePosition {
    public final int row;
    public final int column;
    public final Direction direction;

    public MovePosition(int row, int column, Direction direction) {
        this.row = row;
        this.column = column;
        this.direction = direction;
    }
}
