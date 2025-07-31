package nz.ara.ac.dtw0048.eyeballmaze.view;

import android.os.Handler;
import android.widget.TextView;

public class LevelTimerView {

    private final TextView view;
    private final Handler updateHandler;
    private long timeAtStart;
    private long timeAtPause;

    private final Runnable processTimerUpdate = new Runnable() {
        @Override
        public void run() {
            if (timeAtPause == 0) {
                // Calculate time for next redraw
                long currentTime = System.currentTimeMillis();
                long timeSinceStart = currentTime - timeAtStart;
                long closestSecond = Math.round(timeSinceStart / 1000.0);
                updateHandler.postDelayed(this, timeAtStart + (closestSecond + 1L) * 1000L - currentTime);

                // Redraw
                view.setText(String.valueOf(closestSecond));
            }
        }
    };

    public LevelTimerView(TextView view) {
        this.view = view;
        updateHandler = new Handler();
    }

    public void start() {
        timeAtStart = System.currentTimeMillis();
        timeAtPause = 0;
        updateHandler.removeCallbacks(processTimerUpdate);
        updateHandler.postDelayed(processTimerUpdate, 1000);
        view.setText("0");
    }

    public void pause() {
        timeAtPause = System.currentTimeMillis();
        updateHandler.removeCallbacks(processTimerUpdate);
    }

    public void resume() {
        long currentTime = System.currentTimeMillis();
        long timePausedFor = currentTime - timeAtPause;
        timeAtStart += timePausedFor;
        timeAtPause = 0;
        long timeSinceStart = currentTime - timeAtStart;
        long nextSecond = ((long)Math.ceil(timeSinceStart / 1000.0)) * 1000L;
        updateHandler.removeCallbacks(processTimerUpdate);
        updateHandler.postDelayed(processTimerUpdate, nextSecond - timeSinceStart);
    }

    public boolean isPaused() {
        return timeAtPause != 0;
    }

    public int seconds () {
        if (isPaused()) {
            return (int)Math.floor((timeAtPause - timeAtStart) / 1000.0);
        }
        long timeSinceStart = System.currentTimeMillis() - timeAtStart;
        return (int)Math.floor(timeSinceStart / 1000.0);
    }
}
