package com.example.criminalintent;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Date;

public class TimePickerFragment extends DialogFragment {
    private static final String ARG_TIME = "time";
    private static final String ARG_REQUEST_KEY = "request_key";

    public static final String RESULT_TIME = "result_time";
    public static final String TAG = "TimePickerFragment";

    public static TimePickerFragment newInstance(Date date, String requestKey) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_TIME, date);
        args.putString(ARG_REQUEST_KEY, requestKey);

        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Date date = (Date) requireArguments().getSerializable(ARG_TIME);
        String requestKey = requireArguments().getString(ARG_REQUEST_KEY);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                Date resultDate = calendar.getTime();

                Bundle result = new Bundle();
                result.putSerializable(RESULT_TIME, resultDate);
                getParentFragmentManager().setFragmentResult(requestKey, result);
            }
        };

        return new TimePickerDialog(requireContext(), R.style.Theme_CriminalIntent_AlertDialog, listener, hour, minute, false);
    }
}
