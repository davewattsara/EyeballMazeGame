package nz.ara.ac.dtw0048.eyeballmaze.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Level implements IGoalHolder, ISquareHolder, IEyeballHolder, IMoving {

	private int height;
	private int width;
	private HashSet<Position> allMyGoalPositions = new HashSet<Position>();
	private HashMap<Position, Square> allMySquares = new HashMap<Position, Square>();
	private Eyeball myEyeball;
	private boolean isEyeballOnGoal = false;
	private int completedGoalCount = 0;
	private int moveCount;
	private int totalGoalCount;
	private final ArrayList<MovePosition> moveHistory;

	public Level(int newHeight, int newWidth) {
		this.height = newHeight;
		this.width = newWidth;
		moveCount = 0;
		moveHistory = new ArrayList<MovePosition>();
	}
	
	private void positionCheck(int row, int column) {
		if (row < 0 || row >= height || column < 0 || column >= width) {
			StringBuilder sb = new StringBuilder("Out of level bounds. row: ");
			sb.append(row).append(", column: ").append(column);
			sb.append(", rows: ").append(height);
			sb.append(", columns: ").append(width);
			throw new IllegalArgumentException(sb.toString());
		}
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getWidth() {
		return width;
	}

	// ------------ IEyeballHolder --------------------------
	@Override
	public void addEyeball(int row, int column, Direction direction) {
		positionCheck(row, column);
		this.myEyeball = new Eyeball(row, column, direction);
		this.moveHistory.add(new MovePosition(row, column, direction));
	}

	@Override
	public int getEyeballRow() {
		return this.myEyeball.getRow();
	}

	@Override
	public int getEyeballColumn() {
		return this.myEyeball.getColumn();
	}

	@Override
	public Direction getEyeballDirection() {
		return this.myEyeball.getDirection();
	}
	// -------------------------------------------------------
	
	
	// ------------ ISquareHolder ---------------------------
	@Override
	public void addSquare(Square square, int row, int column) {
		positionCheck(row, column);
		this.allMySquares.put(new Position(row, column), square);
	}

	@Override
	public Color getColorAt(int row, int column) {
		positionCheck(row, column);
		Square square = this.allMySquares.get(new Position(row, column));
		if (square == null)
			return Color.BLANK;
		return square.getColor();
	}

	@Override
	public Shape getShapeAt(int row, int column) {
		positionCheck(row, column);
		Square square = this.allMySquares.get(new Position(row, column));
		if (square == null)
			return Shape.BLANK;
		return square.getShape();
	}
	// -----------------------------------------------------------
	
	
	// --------------- IGoalHolder -------------------------------
	@Override
	public void addGoal(int row, int column) {
		positionCheck(row, column);
		allMyGoalPositions.add(new Position(row, column));
		totalGoalCount++;
	}

	@Override
	public int getGoalCount() {
		return allMyGoalPositions.size();
	}

	@Override
	public boolean hasGoalAt(int targetRow, int targetColumn) {
		positionCheck(targetRow, targetColumn);
		return allMyGoalPositions.contains(new Position(targetRow, targetColumn));
	}

	@Override
	public int getCompletedGoalCount() {
		return completedGoalCount;
	}
	// ----------------------------------------------------------------
	
	
	// --------------------- IMoving ------------------------------------
	@Override
	public boolean canMoveTo(int destinationRow, int destinationColumn) {
		return MessageIfMovingTo(destinationRow, destinationColumn) == Message.OK;
	}

	@Override
	public Message MessageIfMovingTo(int destinationRow, int destinationColumn) {
		positionCheck(destinationRow, destinationColumn);
		if (getEyeballColumn() == destinationColumn && getEyeballRow() == destinationRow)
			return Message.SAME_SQUARE;
		Message directionMessage = checkDirectionMessage(destinationRow, destinationColumn);
		if (directionMessage != Message.OK)
			return directionMessage;
		if (!hasBlankFreePathTo(destinationRow, destinationColumn))
			return Message.MOVING_OVER_BLANK;
		if (getColorAt(getEyeballRow(), getEyeballColumn()) == getColorAt(destinationRow, destinationColumn))
			return Message.OK;
		if (getShapeAt(getEyeballRow(), getEyeballColumn()) == getShapeAt(destinationRow, destinationColumn))
			return Message.OK;
		return Message.DIFFERENT_SHAPE_OR_COLOR;
	}

	@Override
	public boolean isDirectionOK(int destinationRow, int destinationColumn) {
		return checkDirectionMessage(destinationRow, destinationColumn) == Message.OK;
	}

	@Override
	public Message checkDirectionMessage(int destinationRow, int destinationColumn) {
		final int rowDiff = destinationRow - getEyeballRow();
		final int columnDiff = destinationColumn - getEyeballColumn();
		
		if (rowDiff != 0 && columnDiff != 0)
			return Message.MOVING_DIAGONALLY;
		
		if (Direction.getDirectionTo(rowDiff, columnDiff) == getEyeballDirection().opposite())
			return Message.BACKWARDS_MOVE;
		
		return Message.OK;
	}

	@Override
	public boolean hasBlankFreePathTo(int destinationRow, int destinationColumn) {
		final int minRow = Math.min(destinationRow, getEyeballRow());
		final int maxRow = Math.max(destinationRow, getEyeballRow());
		final int minColumn = Math.min(destinationColumn, getEyeballColumn());
		final int maxColumn = Math.max(destinationColumn, getEyeballColumn());
		for (int row = minRow; row <= maxRow; row++) {
			for (int column = minColumn; column <= maxColumn; column++) {
				if (getColorAt(row, column) == Color.BLANK || getShapeAt(row, column) == Shape.BLANK)
					return false;
			}
		}
		return true;
	}

	@Override
	public Message checkMessageForBlankOnPathTo(int destinationRow, int destinationColumn) {
		if (hasBlankFreePathTo(destinationRow, destinationColumn))
			return Message.OK;
		return Message.MOVING_OVER_BLANK;
	}

	@Override
	public void moveTo(int destinationRow, int destinationColumn) {
		//Check for invalid moves
		positionCheck(destinationRow, destinationColumn);
		if (checkDirectionMessage(destinationRow, destinationColumn) != Message.OK) {
			throw new IllegalArgumentException(
					String.format("Invalid Move: %s", checkDirectionMessage(destinationRow, destinationColumn)));
		}
		
		//Remove the square if the last move landed on a goal
		if (isEyeballOnGoal) {
			allMySquares.remove(myEyeball.getPosition());
			isEyeballOnGoal = false;
		}
		
		//move the eyeball
		myEyeball.moveTo(destinationRow, destinationColumn);
		moveCount += 1;
		moveHistory.add(new MovePosition(destinationRow, destinationColumn, getEyeballDirection()));
		
		//Check for landing on a goal
		if (hasGoalAt(destinationRow, destinationColumn)) {
			allMyGoalPositions.remove(myEyeball.getPosition());
			isEyeballOnGoal = true;
			completedGoalCount += 1;
		}
	}
	// -----------------------------------------------------------------------


	// --------------------- Methods introduced for Ass# 3-------------------
	public void clear() {
		allMyGoalPositions = new HashSet<Position>();
		allMySquares = new HashMap<Position, Square>();
		myEyeball = null;
		isEyeballOnGoal = false;
		completedGoalCount = 0;
		moveCount = 0;
		moveHistory.clear();
	}

	public int getMoveCount() {
		return moveCount;
	}

	public void setMoveCount(int count) {
		moveCount = count;
	}

	public int getPossibleMovesCount() {
		int result = 0;
		for (int row = 0; row < getHeight(); row++) {
			if (row == getEyeballRow())
				continue;
			if (canMoveTo(row, getEyeballColumn()))
				result += 1;
		}
		for (int column = 0; column < getWidth(); column++) {
			if (column == getEyeballColumn())
				continue;
			if (canMoveTo(getEyeballRow(), column))
				result += 1;
		}
		return result;
	}

	public int getTotalGoalCount() {
		return totalGoalCount;
	}

	public void undoMove() {
		if (moveCount > 0) {
			--moveCount;
			moveHistory.remove(moveHistory.size() - 1);
			MovePosition lastPos = moveHistory.get(moveHistory.size() - 1);
			myEyeball = new Eyeball(lastPos.row, lastPos.column, lastPos.direction);
			Log.i("Undo Move", "undoMove: ");
		}
	}

	public MovePosition[] getMoveHistory() {
		MovePosition[] result = new MovePosition[moveHistory.size()];
		return moveHistory.toArray(result);
	}

	public void addCompletedGoals(int count) {
		completedGoalCount += count;
		totalGoalCount += count;
	}

	public Position[] getGoalPositions() {
		Position[] result = new Position[allMyGoalPositions.size()];
		return allMyGoalPositions.toArray(result);
	}
	// -------------------------------------------------------------------------
}
