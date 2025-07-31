package nz.ara.ac.dtw0048.eyeballmaze.view;

import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

public class TileViewBuilder {
    static final int MARGIN_TOP = 500;
    static final int MARGIN_BOTTOM = 100;
    static final int MARGIN_LEFT = 20;
    static final int MARGIN_RIGHT = 20;

    private final ConstraintLayout constraintLayout;
    private final ConstraintSet constraintSet;
    private int squareSize;
    private final int deviceWidth;
    private final int deviceHeight;
    private int paddingLeft = 0;
    private int paddingTop = 0;

    public TileViewBuilder(ConstraintLayout constraintLayout, int deviceWidth, int deviceHeight) {
        this.constraintLayout = constraintLayout;
        this.constraintSet = new ConstraintSet();
        this.constraintSet.clone(this.constraintLayout);
        this.deviceWidth = deviceWidth;
        this.deviceHeight = deviceHeight;
    }

    public void addView(View view) {
        int viewId = View.generateViewId();
        view.setId(viewId);
        constraintLayout.addView(view);
        constraintSet.constrainHeight(viewId, squareSize);
        constraintSet.constrainWidth(viewId, squareSize);
        constraintSet.applyTo(constraintLayout);
    }

    public void moveAndResizeView(View view, float row, float column) {
        int viewId = view.getId();
        constraintSet.constrainHeight(viewId, squareSize);
        constraintSet.constrainWidth(viewId, squareSize);
        moveView(view, row, column);
    }

    public void moveView(View view, float row, float column) {
        int viewId = view.getId();
        constraintSet.connect(
                viewId, ConstraintSet.TOP,
                constraintLayout.getId(), ConstraintSet.TOP,
                getPositionY(row)
        );
        constraintSet.connect(
                viewId, ConstraintSet.LEFT,
                constraintLayout.getId(), ConstraintSet.LEFT,
                getPositionX(column)
        );
        constraintSet.applyTo(constraintLayout);
    }

    public void moveView(View view, float row, float column, float rotation) {
        moveView(view, row, column);
        view.setRotation(rotation);
    }

    public void addView(View view, float row, float column) {
        addView(view);
        moveView(view, row, column);
    }

    public void addView(View view, float row, float column, float rotation) {
        addView(view, row, column);
        view.setRotation(rotation);
    }

    public void destroyView(View view) {
        constraintLayout.removeView(view);
    }

    private int getPositionX(float column) {
        return Math.round(column * squareSize) + MARGIN_LEFT + paddingLeft;
    }

    private int getPositionY(float row) {
        return  Math.round(row * squareSize) + MARGIN_TOP + paddingTop;
    }

    public void setSize(int rows, int columns) {
        int width = deviceWidth - MARGIN_LEFT - MARGIN_RIGHT;
        int height = deviceHeight - MARGIN_TOP - MARGIN_BOTTOM;

        if (width * rows > columns * height) {
            // If too tall
            squareSize = height / rows;
            paddingTop = 0;
            paddingLeft = (width - columns * squareSize) / 2;
        }
        else {
            // Else too wide
            squareSize = width / columns;
            paddingLeft = 0;
            paddingTop = (height - rows * squareSize) / 2;
        }
    }
}
