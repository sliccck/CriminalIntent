package com.example.criminalintent;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DatePickerFragment extends DialogFragment {
    private static final String ARG_DATE = "date";
    private static final String ARG_REQUEST_KEY = "request_key";

    public static final String REQUEST_DATE = "request_date";
    public static final String RESULT_DATE = "result_date";
    public static final String TAG = "DatePickerFragment";

    public static DatePickerFragment newInstance(Date date, String requestKey) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);
        args.putString(ARG_REQUEST_KEY, requestKey);

        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Date date = (Date) requireArguments().getSerializable(ARG_DATE);
        String requestKey = requireArguments().getString(ARG_REQUEST_KEY);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Date resultDate = new GregorianCalendar(year, month, dayOfMonth).getTime();
                Bundle result = new Bundle();
                result.putSerializable(RESULT_DATE, resultDate);
                getParentFragmentManager().setFragmentResult(requestKey, result);
            }
        };

        return new DatePickerDialog(requireContext(), R.style.Theme_CriminalIntent_AlertDialog, listener, year, month, day);
    }
}
