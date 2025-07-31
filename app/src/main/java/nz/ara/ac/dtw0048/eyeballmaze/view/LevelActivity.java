package nz.ara.ac.dtw0048.eyeballmaze.view;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import nz.ara.ac.dtw0048.eyeballmaze.R;
import nz.ara.ac.dtw0048.eyeballmaze.controller.Controller;
import nz.ara.ac.dtw0048.eyeballmaze.model.Color;
import nz.ara.ac.dtw0048.eyeballmaze.model.Message;
import nz.ara.ac.dtw0048.eyeballmaze.model.Shape;

public class LevelActivity extends AppCompatActivity
        implements FinishLevelDialogFragment.FinishLevelDialogListener,
        EyeballView.EyeballViewListener,
        Controller.ControllerListener,
        PopupMenu.OnMenuItemClickListener {

    static final String SAVE_FILENAME = "save_game.json";

    private Controller controller;
    private ImageView[] tiles;
    private ImageView[] goals;
    private TileViewBuilder viewBuilder;
    private EyeballView eyeball;
    private int rowsLastRender = 0;
    private int columnsLastRender = 0;

    public static boolean isSoundMuted = false;

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                    controller.loadLevelsFromJson(inputStream);
                    destroyAllTiles();
                    render();
                    eyeball.reset();
                    Toast.makeText(this, getString(R.string.game_loaded), Toast.LENGTH_SHORT).show();
                }
                catch (IOException e) {
                    Toast.makeText(this, getString(R.string.error_parse), Toast.LENGTH_SHORT).show();
                    //e.printStackTrace();
                }
                catch (IllegalStateException e) {
                    Toast.makeText(this, getString(R.string.error_parse), Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        // Set up the controller
        controller = new Controller(this, getResources().openRawResource(R.raw.default_level_data));

        // Set up level and eyeball
        setUpLevel();
        eyeball = new EyeballView(this, controller, viewBuilder, this);

        // Display level name
        TextView levelNameView = findViewById(R.id.levelNameView);
        levelNameView.setText(controller.getLevelName());

        // Set up button listeners
        findViewById(R.id.muteButton).setOnClickListener(v -> onMuteButtonClicked((ImageView)v));
        findViewById(R.id.pauseButton).setOnClickListener(v -> onPauseButtonClicked());
        findViewById(R.id.resumeButton).setOnClickListener(v -> onPlayButtonClicked());
        findViewById(R.id.undoButton).setOnClickListener(v -> onUndoButtonClicked());

        // Set up the dropdown menu
        findViewById(R.id.menuButton).setOnClickListener(this::onMenuButtonClicked);
    }

    private void onMuteButtonClicked(ImageView view) {
        isSoundMuted = !isSoundMuted;
        view.setImageResource(isSoundMuted ? R.drawable.audio_off : R.drawable.audio_on);
    }


    private void onPauseButtonClicked() {
        controller.pauseTimer();
        View pauseView = findViewById(R.id.pauseLayout);
        pauseView.setVisibility(View.VISIBLE);
        findViewById(R.id.levelConstraintLayout).setVisibility(View.INVISIBLE);
    }

    private void onPlayButtonClicked() {
        controller.resumeTimer();
        findViewById(R.id.pauseLayout).setVisibility(View.INVISIBLE);
        findViewById(R.id.levelConstraintLayout).setVisibility(View.VISIBLE);
    }

    private void onUndoButtonClicked() {
        controller.undoMove();
        render();
        eyeball.moveEyeball();
    }

    private void onMenuButtonClicked(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
        popup.show();
    }

    private void onLoadLevelSetButtonClicked() {
        mGetContent.launch("application/json");
    }

    private void checkForWinOrLose() {
        if (!checkForLevelWin()) {
            checkForLevelLose();
        }
    }

    private boolean checkForLevelWin() {
        if (controller.hasWonLevel()) {
            showFinishLevelDialog(true);
            controller.pauseTimer();
            if (!isSoundMuted)
                MediaPlayer.create(this, R.raw.win).start();
            return true;
        }
        return false;
    }

    private void checkForLevelLose() {
        if (controller.hasLostLevel()) {
            showFinishLevelDialog(false);
            controller.pauseTimer();
            if (!isSoundMuted)
                MediaPlayer.create(this, R.raw.error).start();
        }
    }

    private void showFinishLevelDialog(boolean didWin) {
        FinishLevelDialogFragment fragment = new FinishLevelDialogFragment();
        Bundle args = new Bundle();
        args.putInt("seconds", controller.getTimerSeconds());
        args.putInt("moves", controller.getMoveCount());
        args.putString("level_name", controller.getLevelName());
        args.putBoolean("did_win", didWin);
        fragment.setArguments(args);
        fragment.show(
                getSupportFragmentManager(),
                "Finish Level Dialog Fragment"
        );
    }

    private int[] getDeviceSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return new int[]{ displayMetrics.widthPixels, displayMetrics.heightPixels };
    }

    private int getDrawableSquareId (Color color, Shape shape) {
        String filename = shape.name().toLowerCase() + "_" + color.name().toLowerCase();
        return getResources().getIdentifier(filename, "drawable", getPackageName());
    }

    @Override
    public void onFinishLevelDialogNextLevel() {
        nextLevel();
    }

    @Override
    public void onFinishLevelDialogRestartLevel() {
        restartLevel();
    }

    @Override
    public void onFinishLevelDialogShowMoves() {
        eyeball.showMoveHistory();
    }

    @Override
    public void onEyeballViewMoveFinished() {
        checkForWinOrLose();
    }

    @Override
    public void onEyeballViewHistoryFinished() {
        checkForWinOrLose();
    }

    @Override
    public void onControllerTimerUpdate(int seconds) {
        TextView timeView = findViewById(R.id.timeCountView);
        timeView.setText(String.valueOf(seconds));
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.restartLevelMenuItem) {
            restartLevel();
            return true;
        } else if (itemId == R.id.showMovesMenuItem) {
            eyeball.showMoveHistory();
            return true;
        } else if (itemId == R.id.undoMenuItem) {
            onUndoButtonClicked();
            return true;
        } else if (itemId == R.id.loadLevelSetMenuItem) {
            onLoadLevelSetButtonClicked();
            return true;
        } else if (itemId == R.id.saveGameMenuItem) {
            saveGame();
        } else if (itemId == R.id.loadGameMenuItem) {
            loadGame();
        }
        return false;
    }

    private class SquareClickListener implements View.OnClickListener {
        int row, column;

        public SquareClickListener(int row, int column) {
            this.row = row;
            this.column = column;
        }

        @Override
        public void onClick(View v) {
            squareClicked(row, column);
        }
    }

    private String getMoveErrorMessage(Message message) {
        switch (message) {
            case SAME_SQUARE -> {
                return getString(R.string.message_if_same_square);
            }
            case DIFFERENT_SHAPE_OR_COLOR -> {
                return getString(R.string.message_if_different_shape_or_colour);
            }
            case BACKWARDS_MOVE -> {
                return getString(R.string.message_if_backwards_move);
            }
            case MOVING_OVER_BLANK -> {
                return getString(R.string.message_if_moving_over_blank);
            }
            case MOVING_DIAGONALLY -> {
                return getString(R.string.message_if_moving_diagonally);
            }
            default -> {
                return "Move OK";
            }
        }
    }

    private void squareClicked(int row, int column) {
        if (!eyeball.isMoving()) {
            if (controller.canMoveTo(row, column)) {
                eyeball.moveEyeball(row, column);
                render();
            } else {
                String message = getMoveErrorMessage(controller.messageIfMovingTo(row, column));
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                if (!isSoundMuted)
                    MediaPlayer.create(this, R.raw.error).start();
            }
        }
    }

    private void restartLevel() {
        controller.restartCurrentLevel();
        render();
        eyeball.reset();
        controller.resumeTimer();
    }

    private void nextLevel() {
        destroyAllTiles();
        controller.nextLevel();
        render();
        eyeball.reset();
        TextView levelNameView = findViewById(R.id.levelNameView);
        levelNameView.setText(controller.getLevelName());
    }

    private void addTile(int row, int column, int index, Color color, Shape shape) {
        ImageView tile = new ImageView(this);
        tile.setImageResource(getDrawableSquareId(color, shape));
        viewBuilder.addView(tile, row, column);
        tile.setOnClickListener(new SquareClickListener(row, column));
        tiles[index] = tile;
    }

    private void addGoal(int row, int column, int index) {
        ImageView goal = new ImageView(this);
        goal.setImageResource(R.drawable.goal);
        viewBuilder.addView(goal, row, column);
        goals[index] = goal;
    }

    private void saveGame() {
        try (OutputStream outputStream =  openFileOutput(SAVE_FILENAME, Context.MODE_PRIVATE)) {
            controller.saveGame(outputStream);
            Toast.makeText(this, getString(R.string.game_saved), Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_file_not_found), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void loadGame() {
        try (InputStream inputStream = openFileInput(SAVE_FILENAME)) {
            controller.loadLevelsFromJson(inputStream);
            destroyAllTiles();
            render();
            eyeball.reset();
            Toast.makeText(this, getString(R.string.game_loaded), Toast.LENGTH_SHORT).show();
        }
        catch (FileNotFoundException e) {
            Toast.makeText(this, getString(R.string.error_file_not_found), Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_parse), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void setUpLevel() {
        int[] displaySize = getDeviceSize();

        viewBuilder = new TileViewBuilder(
                findViewById(R.id.levelConstraintLayout),
                displaySize[0], displaySize[1]
        );
        render();
    }

    private void destroyAllTiles() {
        for (int row = 0; row < rowsLastRender; row++) {
            for (int column = 0; column < columnsLastRender; column++) {
                int index = column + row * columnsLastRender;
                if (tiles[index] != null) {
                    viewBuilder.destroyView(tiles[index]);
                    tiles[index] = null;
                }
                if (goals[index] != null) {
                    viewBuilder.destroyView(goals[index]);
                    goals[index] = null;
                }
            }
        }
    }

    private void renderTiles() {
        int rows = controller.getLevelHeight();
        int columns = controller.getLevelWidth();

        if (rows != rowsLastRender || columns != columnsLastRender) {
            destroyAllTiles();
            tiles = new ImageView[rows * columns];
            goals = new ImageView[rows * columns];
            viewBuilder.setSize(rows, columns);
        }

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Color color = controller.getColorAt(row, column);
                Shape shape = controller.getShapeAt(row, column);
                int index = column + row * columns;
                if (color != Color.BLANK && shape != Shape.BLANK) {
                    if (tiles[index] == null) {
                        addTile(row, column, index, color, shape);
                    }
                }
                else if (tiles[index] != null) {
                    viewBuilder.destroyView(tiles[index]);
                    tiles[index] = null;
                }
                if (controller.hasGoalAt(row, column)) {
                    if (goals[index] == null) {
                        addGoal(row, column, index);
                    }
                }
                else if (goals[index] != null) {
                    viewBuilder.destroyView(goals[index]);
                    goals[index] = null;
                }
            }
        }

        rowsLastRender = rows;
        columnsLastRender = columns;
    }

    private void renderGoalCount() {
        TextView goalText = findViewById(R.id.goalsCompletedView);
        goalText.setText(String.format(
                Locale.UK,
                "%d/%d",
                controller.getTotalGoalCount() - controller.getGoalCount(),
                controller.getTotalGoalCount()
        ));
    }

    private void renderMoveCount() {
        TextView movesText = findViewById(R.id.moveCountView);
        movesText.setText(String.valueOf(controller.getMoveCount()));
    }

    private void render() {
        renderTiles();
        renderGoalCount();
        renderMoveCount();
    }

}