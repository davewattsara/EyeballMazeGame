package nz.ara.ac.dtw0048.eyeballmaze.model;

public class Eyeball {
	
	private int row;
	private int column;
	private Direction direction;
	
	public Eyeball(int newRow, int newColumn, Direction newDirection) {
		this.row = newRow;
		this.column = newColumn;
		this.direction = newDirection;
	}
	
	public int getRow() {
		return this.row;
	}
	
	public int getColumn() {
		return this.column;
	}
	
	public Position getPosition() {
		return new Position(row, column);
	}
	
	public Direction getDirection() {
		return this.direction;
	}
	
	public void moveTo(int destinationRow, int destinationColumn) {
		int rowDifference = destinationRow - row;
		int columnDifference = destinationColumn - column;
		direction = Direction.getDirectionTo(rowDifference, columnDifference);
		row = destinationRow;
		column = destinationColumn;
	}

}
