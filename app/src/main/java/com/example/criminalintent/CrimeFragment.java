package com.example.criminalintent;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class CrimeFragment extends Fragment {
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String ARG_IS_NEW_CRIME = "is_new_crime";

    private Crime mCrime;
    private File mPhotoFile;
    private Uri mPhotoUri;
    private boolean mIsNewCrime;
    private boolean mWasAdded;
    private boolean mIsDeleted = false;

    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private EditText mSuspectField;
    private Button mSuspectButton;
    private Button mAddCrimeButton;
    private ImageButton mDeleteCrimeButton;
    private Button mContactPoliceButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;

    private final ActivityResultLauncher<Void> mPickContact =
        registerForActivityResult(new ActivityResultContracts.PickContact(), this::onContactSelected);

    private final ActivityResultLauncher<Uri> mTakePhoto =
        registerForActivityResult(new ActivityResultContracts.TakePicture(), this::onPhotoCaptured);

    public static CrimeFragment newInstance(UUID crimeId) {
        return newInstance(crimeId, false);
    }

    public static CrimeFragment newInstance(UUID crimeId, boolean isNewCrime) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        args.putBoolean(ARG_IS_NEW_CRIME, isNewCrime);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) requireArguments().getSerializable(ARG_CRIME_ID);
        mIsNewCrime = requireArguments().getBoolean(ARG_IS_NEW_CRIME, false);

        if (mIsNewCrime) {
            mCrime = new Crime(crimeId);
        } else {
            mCrime = CrimeLab.get(requireActivity()).getCrime(crimeId);
        }

        if (mCrime == null) {
            mCrime = new Crime(crimeId);
            mIsNewCrime = true;
        }

        if (savedInstanceState != null) {
            mWasAdded = savedInstanceState.getBoolean("was_added", false);
        }

        mPhotoFile = CrimeLab.get(requireActivity()).getPhotoFile(mCrime);
        mPhotoUri = FileProvider.getUriForFile(
                requireActivity(),
                requireActivity().getPackageName() + ".fileprovider",
                mPhotoFile
        );

        String dateRequestKey = mCrime.getId().toString() + "_date";
        String timeRequestKey = mCrime.getId().toString() + "_time";

        getParentFragmentManager().setFragmentResultListener(
                dateRequestKey,
                this,
                (requestKey, bundle) -> {
                    Date date = (Date) bundle.getSerializable(DatePickerFragment.RESULT_DATE);
                    updateCrimeDate(date);
                    updateDateAndTime();
                }
        );

        getParentFragmentManager().setFragmentResultListener(
                timeRequestKey,
                this,
                (requestKey, bundle) -> {
                    Date date = (Date) bundle.getSerializable(TimePickerFragment.RESULT_TIME);
                    updateCrimeTime(date);
                    updateDateAndTime();
                }
        );
    }

    private void updateCrimeDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mCrime.getDate());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        mCrime.setDate(calendar.getTime());
    }

    private void updateCrimeTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        calendar.setTime(mCrime.getDate());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        mCrime.setDate(calendar.getTime());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = view.findViewById(R.id.crime_title);
        mDateButton = view.findViewById(R.id.crime_date);
        mTimeButton = view.findViewById(R.id.crime_time);
        mSolvedCheckBox = view.findViewById(R.id.crime_solved);
        mReportButton = view.findViewById(R.id.crime_report);
        mSuspectField = view.findViewById(R.id.crime_suspect);
        mSuspectButton = view.findViewById(R.id.crime_suspect_button);
        mAddCrimeButton = view.findViewById(R.id.crime_add);
        mDeleteCrimeButton = view.findViewById(R.id.crime_delete);
        mContactPoliceButton = view.findViewById(R.id.contact_police_button);
        mPhotoButton = view.findViewById(R.id.crime_camera);
        mPhotoView = view.findViewById(R.id.crime_photo);

        mTitleField.setText(mCrime.getTitle());
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSuspectField.setText(mCrime.getSuspect());
        
        updateDateAndTime();
        updatePhotoView();

        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                mCrime.setTitle(charSequence.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        mSuspectField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                mCrime.setSuspect(charSequence.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate(), mCrime.getId().toString() + "_date");
                dialog.show(getParentFragmentManager(), DatePickerFragment.TAG);
            }
        });

        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getDate(), mCrime.getId().toString() + "_time");
                dialog.show(getParentFragmentManager(), TimePickerFragment.TAG);
            }
        });

        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                startActivity(Intent.createChooser(intent, getString(R.string.send_report)));
            }
        });

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mTakePhoto.launch(mPhotoUri);
                } catch (ActivityNotFoundException exception) {
                    Toast.makeText(requireActivity(), R.string.no_camera_app, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPickContact.launch(null);
            }
        });

        mContactPoliceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String suspectNumber = mCrime.getSuspectNumber();
                Intent intent;
                if (suspectNumber != null && !suspectNumber.isEmpty()) {
                    intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + suspectNumber));
                } else {
                    intent = new Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI);
                }

                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(requireActivity(), R.string.no_contacts_app, Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (mIsNewCrime && !mWasAdded) {
            mAddCrimeButton.setVisibility(View.VISIBLE);
            mDeleteCrimeButton.setVisibility(View.GONE);
            mContactPoliceButton.setVisibility(View.GONE);
            mAddCrimeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CrimeLab.get(requireActivity()).addCrime(mCrime);
                    mWasAdded = true;
                    requireActivity().finish();
                }
            });
        } else {
            mAddCrimeButton.setVisibility(View.GONE);
            mDeleteCrimeButton.setVisibility(View.VISIBLE);
            mContactPoliceButton.setVisibility(View.VISIBLE);
        }

        mDeleteCrimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CrimeLab.get(requireActivity()).deleteCrime(mCrime);
                mIsDeleted = true;
                requireActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!mIsDeleted && (!mIsNewCrime || mWasAdded)) {
            CrimeLab.get(requireActivity()).updateCrime(mCrime);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("was_added", mWasAdded);
    }

    @Override
    public void onStart() {
        super.onStart();
        updatePhotoView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPhotoView.setImageDrawable(null);
    }

    private void updateDateAndTime() {
        mDateButton.setText(DateFormat.getDateInstance(DateFormat.FULL).format(mCrime.getDate()));
        mTimeButton.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(mCrime.getDate()));
    }

    private String getCrimeReport() {
        String solvedString = mCrime.isSolved() ? getString(R.string.crime_report_solved) : getString(R.string.crime_report_unsolved);
        String dateString = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT).format(mCrime.getDate());
        String suspect = mCrime.getSuspect();
        String suspectString = (suspect == null || suspect.isEmpty()) ? getString(R.string.crime_report_no_suspect) : getString(R.string.crime_report_suspect, suspect);

        return getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspectString);
    }

    private void onContactSelected(Uri contactUri) {
        if (contactUri == null) return;
        String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID};
        try (Cursor cursor = requireActivity().getContentResolver().query(contactUri, queryFields, null, null, null)) {
            if (cursor == null || cursor.getCount() == 0) return;
            cursor.moveToFirst();
            String suspectName = cursor.getString(0);
            String contactId = cursor.getString(1);
            mCrime.setSuspect(suspectName);
            mSuspectField.setText(suspectName);

            try (Cursor phoneCursor = requireActivity().getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{contactId}, null)) {
                if (phoneCursor != null && phoneCursor.moveToFirst()) {
                    String number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    mCrime.setSuspectNumber(number);
                }
            } catch (SecurityException se) {
                Toast.makeText(requireActivity(), "Cannot access phone number without permission.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onPhotoCaptured(Boolean didTakePhoto) {
        if (didTakePhoto) updatePhotoView();
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
            mPhotoView.setContentDescription(getString(R.string.crime_photo_no_image_description));
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), requireActivity());
            mPhotoView.setImageBitmap(bitmap);
            mPhotoView.setContentDescription(getString(R.string.crime_photo_image_description));
        }
    }
}
