package nz.ara.ac.dtw0048.eyeballmaze.model;

public interface IEyeballHolder {
	public void addEyeball(int row, int column, Direction direction);
	public int getEyeballRow();
	public int getEyeballColumn();
	public Direction getEyeballDirection();
}
