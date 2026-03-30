package com.example.criminalintent;

import android.content.Context;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.List;

public class CrimeListFragment extends Fragment {

    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private Callbacks mCallbacks;

    /**
     * Required interface for hosting activities.
     */
    public interface Callbacks {
        void onCrimeSelected(Crime crime);
        void onCrimeDeleted(Crime crime);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Callbacks) {
            mCallbacks = (Callbacks) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Crime crime = mAdapter.mCrimes.get(position);
                CrimeLab.get(requireActivity()).deleteCrime(crime);
                updateUI();
                if (mCallbacks != null) {
                    mCallbacks.onCrimeDeleted(crime);
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(mCrimeRecyclerView);

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem newItem = menu.findItem(R.id.new_crime);
        if (CrimeLab.get(requireActivity()).getCrimes().size() >= 10) {
            newItem.setVisible(false);
        } else {
            newItem.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.new_crime) {
            Crime crime = new Crime();
            CrimeLab.get(getActivity()).addCrime(crime);
            updateUI();
            if (mCallbacks != null) {
                mCallbacks.onCrimeSelected(crime);
            }
            return true;
        }

        if (item.getItemId() == R.id.change_language) {
            showLanguageDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "Español"};
        String[] languageCodes = {"en", "es"};

        // Create a custom ContextThemeWrapper to force the theme
        ContextThemeWrapper themedContext = new ContextThemeWrapper(requireActivity(), R.style.Theme_CriminalIntent_AlertDialog);

        new AlertDialog.Builder(themedContext)
                .setTitle("Choose Language")
                .setItems(languages, (dialog, which) -> {
                    LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(languageCodes[which]);
                    AppCompatDelegate.setApplicationLocales(appLocales);
                })
                .show();
    }

    public void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(requireActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();
        }

        requireActivity().invalidateOptionsMenu();
    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Crime mCrime;
        private final TextView mTitleTextView;
        private final TextView mDateTextView;
        private final TextView mStatusTextView;
        private final TextView mSuspectTextView;
        private final ImageView mSolvedImageView;
        private final ImageButton mDeleteButton;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime, parent, false));

            itemView.setOnClickListener(this);
            mTitleTextView = itemView.findViewById(R.id.crime_title);
            mStatusTextView = itemView.findViewById(R.id.crime_status);
            mDateTextView = itemView.findViewById(R.id.crime_date);
            mSuspectTextView = itemView.findViewById(R.id.crime_suspect);
            mSolvedImageView = itemView.findViewById(R.id.crime_solved_icon);
            mDeleteButton = itemView.findViewById(R.id.crime_delete);

            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CrimeLab.get(requireActivity()).deleteCrime(mCrime);
                    updateUI();
                    if (mCallbacks != null) {
                        mCallbacks.onCrimeDeleted(mCrime);
                    }
                }
            });
        }

        public void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT).format(mCrime.getDate()));
            mDateTextView.setVisibility(View.VISIBLE);

            if (mCrime.isSolved()) {
                mStatusTextView.setText(R.string.case_closed);
                mSolvedImageView.setVisibility(View.VISIBLE);
                mSolvedImageView.setColorFilter(ContextCompat.getColor(getActivity(), R.color.orange_primary));
            } else {
                mStatusTextView.setText(R.string.case_open);
                mSolvedImageView.setVisibility(View.GONE);
                mSolvedImageView.clearColorFilter();
            }

            String suspect = mCrime.getSuspect();
            if (suspect != null && !suspect.isEmpty()) {
                mSuspectTextView.setText("Suspect: " + suspect);
                mSuspectTextView.setVisibility(View.VISIBLE);
            } else {
                mSuspectTextView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            if (mCallbacks != null) {
                mCallbacks.onCrimeSelected(mCrime);
            }
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {
        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }
    }
}
