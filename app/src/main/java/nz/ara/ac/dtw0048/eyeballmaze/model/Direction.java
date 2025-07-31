package nz.ara.ac.dtw0048.eyeballmaze.model;

import java.util.Locale;

public enum Direction {
	UP {
		@Override
		public Direction opposite() {
			return Direction.DOWN;
		}
	},
	DOWN {
		@Override
		public Direction opposite() {
			return Direction.UP;
		}
	},
	LEFT {
		@Override
		public Direction opposite() {
			return Direction.RIGHT;
		}
	},
	RIGHT {
		@Override
		public Direction opposite() {
			return Direction.LEFT;
		}
	};
	
	public abstract Direction opposite();
	
	public static Direction getDirectionTo(int rowDifference, int columnDifference) {
		if (rowDifference * columnDifference != 0 || rowDifference + columnDifference == 0) {
			String err = String.format(Locale.UK,"Invalid direction! rowDifference: %d, columnDifference: %d", rowDifference, columnDifference);
			throw new IllegalArgumentException(err);
		}
		if (rowDifference > 0)
			return Direction.DOWN;
		if (rowDifference < 0)
			return Direction.UP;
		if (columnDifference > 0)
			return Direction.RIGHT;
		return Direction.LEFT;
	}
}
