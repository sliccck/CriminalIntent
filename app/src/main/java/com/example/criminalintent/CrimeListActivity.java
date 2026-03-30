package com.example.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class CrimeListActivity extends SingleFragmentActivity
        implements CrimeListFragment.Callbacks, CrimeFragment.Callbacks {

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (findViewById(R.id.detail_fragment_container) != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment detailFragment = fragmentManager.findFragmentById(R.id.detail_fragment_container);

            if (detailFragment == null) {
                detailFragment = new PlaceholderFragment();
                fragmentManager.beginTransaction()
                        .add(R.id.detail_fragment_container, detailFragment)
                        .commit();
            }
        }
    }

    @Override
    public void onCrimeSelected(Crime crime) {
        if (findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = CrimePagerActivity.newIntent(this, crime.getId());
            startActivity(intent);
        } else {
            Fragment newDetail = CrimeFragment.newInstance(crime.getId());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, newDetail)
                    .commit();
        }
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        CrimeListFragment listFragment = (CrimeListFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
        if (listFragment != null) {
            listFragment.updateUI();
        }
    }

    @Override
    public void onCrimeDeleted(Crime crime) {
        if (findViewById(R.id.detail_fragment_container) != null) {
            Fragment placeholder = new PlaceholderFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, placeholder)
                    .commit();
        }

        CrimeListFragment listFragment = (CrimeListFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
        if (listFragment != null) {
            listFragment.updateUI();
        }
    }

    @Override
    public void onBackSelected() {
        if (findViewById(R.id.detail_fragment_container) != null) {
            Fragment placeholder = new PlaceholderFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, placeholder)
                    .commit();
        }
    }
}
