package nz.ara.ac.dtw0048.eyeballmaze.model;

public final class PlayableSquare extends Square {

	private Color color;
	private Shape shape;
	
	public PlayableSquare(Color newColor, Shape newShape) {
		this.color = newColor;
		this.shape = newShape;
	}

	@Override
	public Color getColor() {
		return this.color;
	}

	@Override
	public Shape getShape() {
		return this.shape;
	}
}
