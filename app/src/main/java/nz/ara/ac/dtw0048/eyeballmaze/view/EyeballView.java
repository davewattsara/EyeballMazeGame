package nz.ara.ac.dtw0048.eyeballmaze.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.ImageView;

import android.os.Handler;

import java.io.IOException;

import nz.ara.ac.dtw0048.eyeballmaze.R;
import nz.ara.ac.dtw0048.eyeballmaze.controller.Controller;
import nz.ara.ac.dtw0048.eyeballmaze.model.Direction;
import nz.ara.ac.dtw0048.eyeballmaze.model.MovePosition;

public class EyeballView {
    interface EyeballViewListener {
        void onEyeballViewMoveFinished();
        void onEyeballViewHistoryFinished();
    }
    static final float MOVE_SPEED = 0.1f;
    static final long MOVE_WAIT_DELAY = 500L;
    private final ImageView view;
    private float eyeballRow;
    private float eyeballColumn;
    private boolean isEyeballMoving;
    public boolean isShowingMoveHistory;
    public  int moveHistoryIndex;
    public MovePosition[] moveHistory;
    private final Controller controller;
    private final TileViewBuilder viewBuilder;
    private final Handler moveHandler;
    private final EyeballViewListener listener;
    private final MediaPlayer moveSoundPlayer;

    private final Runnable processMove = new Runnable() {
        @Override
        public void run() {

            // Get the target position
            float targetColumn = getEyeballColumn();
            float targetRow = getEyeballRow();

            // Move the actual position vertically
            if (Math.abs(targetRow - eyeballRow) <= MOVE_SPEED)
                eyeballRow = targetRow;
            else
                eyeballRow += Math.signum(targetRow - eyeballRow) * MOVE_SPEED;

            // and horizontally
            if (Math.abs(targetColumn - eyeballColumn) <= MOVE_SPEED)
                eyeballColumn = targetColumn;
            else
                eyeballColumn += Math.signum(targetColumn - eyeballColumn) * MOVE_SPEED;

            // Move the view to the actual position and rotation
            viewBuilder.moveView(view, eyeballRow, eyeballColumn, getEyeballRotation());

            // If the target has been reached
            if (eyeballColumn == targetColumn && eyeballRow == targetRow) {

                // Stop moving and alert listener or do next move in history
                isEyeballMoving = false;
                if (isShowingMoveHistory) {
                    ++moveHistoryIndex;
                    if (moveHistoryIndex < moveHistory.length)
                        moveHandler.postDelayed(this, MOVE_WAIT_DELAY);
                    else {
                        isShowingMoveHistory = false;
                        listener.onEyeballViewHistoryFinished();
                    }
                }
                else {
                    listener.onEyeballViewMoveFinished();
                }
            }
            else
                // keep the movement loop running at 100fps
                moveHandler.postDelayed(this, 10);
        }
    };

    public EyeballView(Context context, Controller controller, TileViewBuilder viewBuilder, EyeballViewListener listener) {
        moveHandler = new Handler();
        this.controller = controller;
        this.viewBuilder = viewBuilder;
        view = new ImageView(context);
        view.setImageResource(R.drawable.eyeball);
        eyeballRow = getEyeballRow();
        eyeballColumn = getEyeballColumn();
        viewBuilder.addView(view, eyeballRow, eyeballColumn, getEyeballRotation());
        this.listener = listener;
        moveSoundPlayer = MediaPlayer.create(context, R.raw.move);
        isEyeballMoving = false;
        isShowingMoveHistory = false;
    }

    private float getEyeballRotation() {
        Direction direction = isShowingMoveHistory ?
                moveHistory[moveHistoryIndex].direction : controller.getEyeballDirection();
        return switch (direction) {
            case UP -> 0;
            case DOWN -> 180;
            case LEFT -> 270;
            case RIGHT -> 90;
        };
    }

    private int getEyeballRow() {
        if (isShowingMoveHistory)
            return moveHistory[moveHistoryIndex].row;
        return controller.getEyeballRow();
    }

    private int getEyeballColumn() {
        if (isShowingMoveHistory)
            return moveHistory[moveHistoryIndex].column;
        return controller.getEyeballColumn();
    }

    public boolean isMoving() {
        return isEyeballMoving || isShowingMoveHistory;
    }

    private void playMoveSound() {
        if (LevelActivity.isSoundMuted)
            return;

        moveSoundPlayer.stop();
        try {
            moveSoundPlayer.prepare();
        }
        catch (IllegalStateException e) {
            Log.e("EyeballView", "playMoveSound: illegalStateException", e);
        }
        catch (IOException e) {
            Log.e("EyeballView", "playMoveSound: IOException", e);
        }
        finally {
            moveSoundPlayer.start();
        }
    }

    public void moveEyeball(int row, int column) {
        controller.moveTo(row, column);
        isEyeballMoving = true;
        moveHandler.post(processMove);
        playMoveSound();
    }

    public void moveEyeball() {
        isEyeballMoving = true;
        moveHandler.post(processMove);
        playMoveSound();
    }

    public void reset() {
        eyeballRow = controller.getEyeballRow();
        eyeballColumn = controller.getEyeballColumn();
        viewBuilder.moveAndResizeView(view, eyeballRow, eyeballColumn);
        view.bringToFront();
        isShowingMoveHistory = false;
        isEyeballMoving = false;
    }

    public void showMoveHistory() {
        moveHistoryIndex = 0;
        isShowingMoveHistory = true;
        moveHistory = controller.getMoveHistory();
        eyeballRow = getEyeballRow();
        eyeballColumn = getEyeballColumn();
        viewBuilder.moveView(view, eyeballRow, eyeballColumn, getEyeballRotation());
        moveHandler.post(processMove);
    }
}
