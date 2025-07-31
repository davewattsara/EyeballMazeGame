package nz.ara.ac.dtw0048.eyeballmaze.model;

import java.util.ArrayList;

public class Game implements ILevelHolder, IGoalHolder, ISquareHolder, IEyeballHolder, IMoving {

	private ArrayList<Level> allMyLevels = new ArrayList<Level>();
	private int currentLevelNumber = -1;
	
	private Level currentLevel() {
		return allMyLevels.get(currentLevelNumber);
	}
	
	// Method from IEyeballHolder
	@Override
	public void addEyeball(int row, int column, Direction direction) {
		currentLevel().addEyeball(row, column, direction);
	}
	
	// Method from IEyeballHolder
	@Override
	public int getEyeballRow() {
		return currentLevel().getEyeballRow();
	}
	
	// Method from IEyeballHolder
	@Override
	public int getEyeballColumn() {
		return currentLevel().getEyeballColumn();
	}
	
	// Method from IEyeballHolder
	@Override
	public Direction getEyeballDirection() {
		return currentLevel().getEyeballDirection();
	}
	
	// Method from ISquareHolder
	@Override
	public void addSquare(Square square, int row, int column) {
		currentLevel().addSquare(square, row, column);
	}
	
	// Method from ISquareHolder
	@Override
	public Color getColorAt(int row, int column) {
		return currentLevel().getColorAt(row, column);
	}
	
	// Method from ISquareHolder
	@Override
	public Shape getShapeAt(int row, int column) {
		return currentLevel().getShapeAt(row, column);
	}
	
	// Method from IGoalHolder
	@Override
	public void addGoal(int row, int column) {
		currentLevel().addGoal(row, column);
	}

	// Method from IGoalHolder
	@Override
	public int getGoalCount() {
		return currentLevel().getGoalCount();
	}

	// Method from IGoalHolder
	@Override
	public boolean hasGoalAt(int targetRow, int targetColumn) {
		return currentLevel().hasGoalAt(targetRow, targetColumn);
	}

	// Method from IGoalHolder
	@Override
	public int getCompletedGoalCount() {
		return currentLevel().getCompletedGoalCount();
	}

	// Method from ILevelHolder
	@Override
	public void addLevel(int height, int width) {
		this.allMyLevels.add(new Level(height, width));
		this.currentLevelNumber = this.allMyLevels.size() - 1;
	}

	// Method from ILevelHolder
	@Override
	public int getLevelWidth() {
		return this.allMyLevels.get(this.currentLevelNumber).getWidth();
	}

	// Method from ILevelHolder
	@Override
	public int getLevelHeight() {
		return this.allMyLevels.get(this.currentLevelNumber).getHeight();
	}

	// Method from ILevelHolder
	@Override
	public void setLevel(int levelNumber) {
		if (levelNumber < 0 || levelNumber >= this.allMyLevels.size()) {
			String err = String.format("Invalid level number: %d", levelNumber);
			throw new IllegalArgumentException(err);
		}
		this.currentLevelNumber = levelNumber;
	}

	// Method from ILevelHolder
	@Override
	public int getLevelCount() {
		return this.allMyLevels.size();
	}
	
	//Method from IMoving
	@Override
	public boolean canMoveTo(int destinationRow, int destinationColumn) {
		return currentLevel().canMoveTo(destinationRow, destinationColumn);
	}
	
	//Method from IMoving
	@Override
	public Message MessageIfMovingTo(int destinationRow, int destinationColumn) {
		return currentLevel().MessageIfMovingTo(destinationRow, destinationColumn);
	}

	//Method from IMoving
	@Override
	public boolean isDirectionOK(int destinationRow, int destinationColumn) {
		return currentLevel().isDirectionOK(destinationRow, destinationColumn);
	}

	//Method from IMoving
	@Override
	public Message checkDirectionMessage(int destinationRow, int destinationColumn) {
		return currentLevel().checkDirectionMessage(destinationRow, destinationColumn);
	}

	//Method from IMoving
	@Override
	public boolean hasBlankFreePathTo(int destinationRow, int destinationColumn) {
		return currentLevel().hasBlankFreePathTo(destinationRow, destinationColumn);
	}

	//Method from IMoving
	@Override
	public Message checkMessageForBlankOnPathTo(int destinationRow, int destinationColumn) {
		return currentLevel().checkMessageForBlankOnPathTo(destinationRow, destinationColumn);
	}

	//Method from IMoving
	@Override
	public void moveTo(int destinationRow, int destinationColumn) {
		currentLevel().moveTo(destinationRow, destinationColumn);
	}

	public void clearCurrentLevel() {
		currentLevel().clear();
	}
	public int getMoveCount() {
		return currentLevel().getMoveCount();
	}

	public void setMoveCount(int count) {
		currentLevel().setMoveCount(count);
	}
	public int getPossibleMovesCount() {
		return currentLevel().getPossibleMovesCount();
	}
	public int getTotalGoalCount() {
		return currentLevel().getTotalGoalCount();
	}

	public void undoMove() {
		currentLevel().undoMove();
	}

	public MovePosition[] getMoveHistory() {
		return currentLevel().getMoveHistory();
	}

	public void addCompletedGoals(int count) {
		currentLevel().addCompletedGoals(count);
	}
	public Position[] getGoalPositions() {
		return currentLevel().getGoalPositions();
	}
}
