package nz.ara.ac.dtw0048.eyeballmaze.model;

public interface ISquareHolder {
	public void addSquare(Square square, int row, int column);
	public Color getColorAt(int row, int column);
	public Shape getShapeAt(int row, int column);
}
