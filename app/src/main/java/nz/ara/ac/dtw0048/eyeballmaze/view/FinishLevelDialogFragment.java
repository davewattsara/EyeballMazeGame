package nz.ara.ac.dtw0048.eyeballmaze.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Locale;

import nz.ara.ac.dtw0048.eyeballmaze.R;

public class FinishLevelDialogFragment extends DialogFragment {

    public interface FinishLevelDialogListener {
        void onFinishLevelDialogNextLevel();
        void onFinishLevelDialogRestartLevel();
        void onFinishLevelDialogShowMoves();
    }
    private FinishLevelDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (FinishLevelDialogListener)context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        assert getArguments() != null;
        boolean won = getArguments().getBoolean("did_win");
        String titleString = String.format(
                "%s %s",
                getArguments().getString("level_name"),
                won ? getString(R.string.completed) : getString(R.string.failed)
        );
        String resultsString = won ? getWinString() : getString(R.string.failed_reason);
        builder.setTitle(titleString)
                .setMessage(resultsString)
                .setNegativeButton(R.string.show_moves, (dialog, id) -> listener.onFinishLevelDialogShowMoves());
        if (won)
            builder.setPositiveButton(R.string.next_level, (dialog, id) -> listener.onFinishLevelDialogNextLevel());
        else
            builder.setPositiveButton(R.string.restart_level, (dialog, id) -> listener.onFinishLevelDialogRestartLevel());
        return builder.create();
    }

    private String getWinString() {
        assert getArguments() != null;
        return String.format(
                Locale.UK,
                "%s %d %s\n%s %d",
                getString(R.string.time_taken),
                getArguments().getInt("seconds"),
                getString(R.string.seconds),
                getString(R.string.moves),
                getArguments().getInt("moves")
        );
    }
}
