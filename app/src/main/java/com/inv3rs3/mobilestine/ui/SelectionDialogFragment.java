package com.inv3rs3.mobilestine.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.inv3rs3.mobilestine.R;

/**
 * Simple single choice selection dialog
 * The parent activity must implement the DialogSelectionCallback interface
 */
public class SelectionDialogFragment extends DialogFragment
{
    private static final String ARGS_DIALOG_OPTIONS = "ARG_DIALOG_OPTIONS";
    private static final String ARGS_DIALOG_TITLE = "ARG_DIALOG_TITLE";

    private int _selectedIndex;

    public static SelectionDialogFragment create(String title, String[] options)
    {
        Bundle arguments = new Bundle();
        arguments.putString(ARGS_DIALOG_TITLE, title);
        arguments.putStringArray(ARGS_DIALOG_OPTIONS, options);

        SelectionDialogFragment dialogFragment = new SelectionDialogFragment();
        dialogFragment.setArguments(arguments);

        return dialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        String title = getArguments().getString(ARGS_DIALOG_TITLE);
        final String[] options = getArguments().getStringArray(ARGS_DIALOG_OPTIONS);

        _selectedIndex = 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setSingleChoiceItems(options, _selectedIndex, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface anInterface, int i)
                    {
                        _selectedIndex = i;
                    }
                })
                .setPositiveButton(R.string.dialog_select, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface anInterface, int i)
                    {
                        ((SelectionDialogCallback) getActivity()).selected(options[_selectedIndex], _selectedIndex);
                        anInterface.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface anInterface, int i)
                    {
                        ((SelectionDialogCallback) getActivity()).canceled();
                        anInterface.cancel();
                    }
                });

        return builder.create();
    }

    public interface SelectionDialogCallback
    {
        public void selected(String selected, int index);

        public void canceled();
    }
}
