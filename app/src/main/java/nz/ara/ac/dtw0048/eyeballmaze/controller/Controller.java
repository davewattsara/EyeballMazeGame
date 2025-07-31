package nz.ara.ac.dtw0048.eyeballmaze.controller;


import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import nz.ara.ac.dtw0048.eyeballmaze.model.BlankSquare;
import nz.ara.ac.dtw0048.eyeballmaze.model.Color;
import nz.ara.ac.dtw0048.eyeballmaze.model.Direction;
import nz.ara.ac.dtw0048.eyeballmaze.model.Game;
import nz.ara.ac.dtw0048.eyeballmaze.model.LevelTimer;
import nz.ara.ac.dtw0048.eyeballmaze.model.Message;
import nz.ara.ac.dtw0048.eyeballmaze.model.MovePosition;
import nz.ara.ac.dtw0048.eyeballmaze.model.PlayableSquare;
import nz.ara.ac.dtw0048.eyeballmaze.model.Position;
import nz.ara.ac.dtw0048.eyeballmaze.model.Shape;
import nz.ara.ac.dtw0048.eyeballmaze.model.Square;

public class Controller implements LevelTimer.LevelTimerListener {

    public interface ControllerListener {
        void onControllerTimerUpdate(int seconds);
    }

    private static class LevelData {
        public String name = "";
        public int startRow = 0,
                startColumn = 0,
                rows = 0,
                columns = 0,
                move_count = 0,
                seconds = 0,
                completed_goals = 0;
        public Direction startDirection = Direction.UP;
        public ArrayList<Square> squares = new ArrayList<>();
        public ArrayList<Position> goals = new ArrayList<>();
    }

    private final ControllerListener listener;
    private Game game;
    private final LevelTimer levelTimer;
    private int levelIndex;
    ArrayList<LevelData> levels;
    public Controller(ControllerListener listener, InputStream levelFile) {
        levelTimer = new LevelTimer(this);
        this.listener = listener;
        this.levels = new ArrayList<>();
        try {
            loadLevelsFromJson(levelFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Direction getDirection(String directionData) {
        return switch (directionData) {
            case "u" -> Direction.UP;
            case "d" -> Direction.DOWN;
            case "l" -> Direction.LEFT;
            default -> Direction.RIGHT;
        };
    }

    private String getDirectionString(Direction direction) {
        return switch (direction) {
            case UP -> "u";
            case DOWN -> "d";
            case LEFT -> "l";
            default -> "r";
        };
    }

    private Square getSquare(String squareData) {
        Color color;
        switch (squareData.charAt(0)) {
            case 'b' -> color = Color.BLUE;
            case 'r' -> color = Color.RED;
            case 'y' -> color = Color.YELLOW;
            case 'g' -> color = Color.GREEN;
            case 'p' -> color = Color.PURPLE;
            default -> {
                return new BlankSquare();
            }
        }

        Shape shape;
        switch (squareData.charAt(1)) {
            case 'd' -> shape = Shape.DIAMOND;
            case 'c' -> shape = Shape.CROSS;
            case 's' -> shape = Shape.STAR;
            case 'f' -> shape = Shape.FLOWER;
            case 'l' -> shape = Shape.LIGHTNING;
            default -> {
                return new BlankSquare();
            }
        }

        return new PlayableSquare(color, shape);
    }

    private String getSquareString(Square square) {
        String result = "";
        switch (square.getColor()) {
            case BLUE -> result += "b";
            case RED -> result += "r";
            case YELLOW -> result += "y";
            case GREEN -> result += "g";
            case PURPLE -> result += "p";
            default -> {
                return "xx";
            }
        }

        switch (square.getShape()) {
            case DIAMOND -> result += "d";
            case CROSS -> result += "c";
            case STAR -> result += "s";
            case FLOWER -> result += "f";
            case LIGHTNING -> result += "l";
            default -> {
                return "xx";
            }
        }
        return result;
    }

    private void loadLevelFromJson(JsonReader reader) throws IOException {
        LevelData levelData = new LevelData();

        // Parse the json data for this level
        while (reader.hasNext()) {
            String keyName = reader.nextName();
            switch (keyName) {
                case "name" -> levelData.name = reader.nextString();
                case "rows" -> levelData.rows = reader.nextInt();
                case "columns" -> levelData.columns = reader.nextInt();
                case "eyeball_row" -> levelData.startRow = reader.nextInt();
                case "eyeball_column" -> levelData.startColumn = reader.nextInt();
                case "eyeball_direction" -> levelData.startDirection = getDirection(reader.nextString());
                case "squares" -> {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        levelData.squares.add(getSquare(reader.nextString()));
                    }
                    reader.endArray();
                }
                case "goals" -> {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        int row = 0;
                        int column = 0;

                        reader.beginObject();
                        while (reader.hasNext()) {
                            String goalKeyName = reader.nextName();
                            switch (goalKeyName) {
                                case "row" -> row = reader.nextInt();
                                case "column" -> column = reader.nextInt();
                            }
                        }
                        reader.endObject();

                        levelData.goals.add(new Position(row, column));
                    }

                    reader.endArray();
                }
                case "seconds" -> levelData.seconds = reader.nextInt();
                case "move_count" -> levelData.move_count = reader.nextInt();
                case "completed_goals" -> levelData.completed_goals = reader.nextInt();
            }
        }
        levels.add(levelData);
    }

    public void loadLevelsFromJson(InputStream inputStream) throws IOException {
        levels.clear();
        levelIndex = 0;
        InputStreamReader inputStreamReader
                = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        JsonReader reader = new JsonReader(inputStreamReader);
        reader.beginObject();
        while (reader.hasNext()) {
            String keyName = reader.nextName();
            switch (keyName) {
                case "levels" -> {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        reader.beginObject();
                        loadLevelFromJson(reader);
                        reader.endObject();
                    }
                    reader.endArray();
                }
                case "level_index" -> levelIndex = reader.nextInt();
            }
        }
        reader.endObject();
        reader.close();
        inputStreamReader.close();
        setUpLevels();
    }

    private void setUpLevel(int index) {
        LevelData levelData = levels.get(index);

        // Add the level squares
        for (int row = 0; row < levelData.rows; row++) {
            for (int column = 0; column < levelData.columns; column++) {
                game.addSquare(levelData.squares.get(column + row * levelData.columns), row, column);
            }
        }

        // Set the level goals
        for(Position goalPosition: levelData.goals) {
            game.addGoal(goalPosition.getRow(), goalPosition.getColumn());
        }

        // Set the player start position
        game.addEyeball(levelData.startRow, levelData.startColumn, levelData.startDirection);

        // Set the completed goals
        game.addCompletedGoals(levelData.completed_goals);

        // Set the move count
        game.setMoveCount(levelData.move_count);
    }

    private void setUpLevels() {
        game = new Game();
        for (int i = 0; i < levels.size(); i++) {
            game.addLevel(levels.get(i).rows, levels.get(i).columns);
            setUpLevel(i);
        }

        // Set the current level to 0
        game.setLevel(levelIndex);
        levelTimer.start(levels.get(levelIndex).seconds);
    }

    public String getLevelName() {
        return levels.get(levelIndex).name;
    }

    public int getLevelWidth () {
        return game.getLevelWidth();
    }

    public int getLevelHeight () {
        return game.getLevelHeight();
    }

    public Color getColorAt(int row, int column) {
        return game.getColorAt(row, column);
    }

    public Shape getShapeAt(int row, int column) {
        return game.getShapeAt(row, column);
    }

    public int getEyeballRow() {
        return game.getEyeballRow();
    }

    public int getEyeballColumn() {
        return game.getEyeballColumn();
    }

    public Direction getEyeballDirection() {
        return game.getEyeballDirection();
    }

    public boolean canMoveTo(int destinationRow, int destinationColumn) {
        return game.canMoveTo(destinationRow, destinationColumn);
    }

    public Message messageIfMovingTo(int destinationRow, int destinationColumn) {
        return game.MessageIfMovingTo(destinationRow, destinationColumn);
    }

    public void moveTo(int destinationRow, int destinationColumn) {
        assert(canMoveTo(destinationRow, destinationColumn));
        game.moveTo(destinationRow, destinationColumn);
    }

    public boolean hasWonLevel() {
        return game.getGoalCount() == 0;
    }

    public boolean hasGoalAt(int row, int column) {
        return game.hasGoalAt(row, column);
    }

    public int getGoalCount() {
        return game.getGoalCount();
    }
    public int getMoveCount() {
        return game.getMoveCount();
    }

    public void restartCurrentLevel() {
        game.clearCurrentLevel();
        setUpLevel(levelIndex);
    }

    public void nextLevel() {
        if (game.getLevelCount() > levelIndex + 1) {
            levelIndex += 1;
            game.setLevel(levelIndex);
            levelTimer.start(levels.get(levelIndex).seconds);
        }
    }

    @Override
    public void onLevelTimerUpdate(int seconds) {
        listener.onControllerTimerUpdate(seconds);
        if (levels.size() > levelIndex)
            levels.get(levelIndex).seconds = seconds;
    }

    public void pauseTimer() {
        levelTimer.pause();
    }

    public void resumeTimer() {
        levelTimer.resume();
    }

    public int getTimerSeconds () {
        return levelTimer.seconds();
    }

    public boolean hasLostLevel() {
        return game.getPossibleMovesCount() == 0;
    }

    public int getTotalGoalCount() {
        return game.getTotalGoalCount();
    }

    public void undoMove() {
        game.undoMove();
    }

    public MovePosition[] getMoveHistory() {
        return game.getMoveHistory();
    }

    public void saveLevel(JsonWriter writer, int index) throws IOException {
        LevelData data = levels.get(index);
        game.setLevel(index);
        writer.beginObject();

        writer.name("name").value(data.name);
        writer.name("rows").value(data.rows);
        writer.name("columns").value(data.columns);

        writer.name("squares").beginArray();
        for (int i = 0; i < data.squares.size(); i++) {
            writer.value(getSquareString(data.squares.get(i)));
        }
        writer.endArray();

        writer.name("eyeball_row").value(game.getEyeballRow());
        writer.name("eyeball_column").value(game.getEyeballColumn());
        writer.name("eyeball_direction").value(getDirectionString(game.getEyeballDirection()));

        writer.name("goals").beginArray();
        for (Position goalPosition: game.getGoalPositions()) {
            writer.beginObject();
            writer.name("row").value(goalPosition.getRow());
            writer.name("column").value(goalPosition.getColumn());
            writer.endObject();
        }
        writer.endArray();

        writer.name("seconds").value(data.seconds);
        writer.name("move_count").value(game.getMoveCount());
        writer.name("completed_goals").value(game.getCompletedGoalCount());

        writer.endObject();
    }

    public void saveGame(OutputStream outputStream) throws IOException {
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        JsonWriter writer = new JsonWriter(outputStreamWriter);
        writer.beginObject();
        writer.name("levels").beginArray();
        for (int i = 0; i < levels.size(); i++) {
            saveLevel(writer, i);
        }
        writer.endArray();
        writer.name("level_index").value(levelIndex);
        writer.endObject();
        game.setLevel(levelIndex);
        writer.close();
        outputStreamWriter.close();
    }
}
