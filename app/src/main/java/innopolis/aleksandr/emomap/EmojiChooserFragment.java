package innopolis.aleksandr.emomap;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class EmojiChooserFragment extends DialogFragment{

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, String string, int mood);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
    NoticeDialogListener mListener;
    EditText comment;
    RadioGroup moodGroup;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View emojiView = getActivity().getLayoutInflater().inflate(R.layout.choose_emoji, null);

        comment = (EditText)emojiView.findViewById(R.id.username);
        moodGroup = (RadioGroup)emojiView.findViewById(R.id.mood_chooser);
        builder.setView(emojiView)
                .setPositiveButton("Put emoji to my current location", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        int moodButton = moodGroup.getCheckedRadioButtonId();
                        int mood = -1;
                        if (moodButton == R.id.sad) {
                            mood = Constants.SAD;
                        } else if (moodButton == R.id.indifferent) {
                            mood = Constants.INDIFFERENT;
                        } else if  (moodButton == R.id.happy) {
                            mood = Constants.HAPPY;
                        }
                        mListener.onDialogPositiveClick(EmojiChooserFragment.this, comment.getText().toString(), mood);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(EmojiChooserFragment.this);
                        EmojiChooserFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

}
