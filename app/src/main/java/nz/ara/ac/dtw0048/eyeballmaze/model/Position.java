package nz.ara.ac.dtw0048.eyeballmaze.model;

import java.util.Objects;

// Class had to be converted from a record to a class to be compatible with Android Studio?
public final class Position {
	private final int row;
	private final int column;

	public Position(int row, int column) {
		this.row = row;
		this.column = column;
	}

	public int row() {
		return row;
	}

	public int column() {
		return column;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		Position that = (Position) obj;
		return this.row == that.row &&
				this.column == that.column;
	}

	@Override
	public int hashCode() {
		return Objects.hash(row, column);
	}

	@Override
	public String toString() {
		return "Position[" +
				"row=" + row + ", " +
				"column=" + column + ']';
	}

	public int getRow() {
		return row;
	}

	public int getColumn() {
		return column;
	}
}
