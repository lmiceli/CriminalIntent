package com.bignerdranch.android.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bignerdranch.android.criminalintent.model.Crime;
import com.bignerdranch.android.criminalintent.model.CrimeLab;

import java.util.List;

/**
 * Created by lmiceli on 11/05/2016.
 */
public class CrimeListFragment extends Fragment {


    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    // optimization for updateUI on resume, might not work on low memory conditions, but is an easy to
    // code solution that will work most of the time, and would do the normal/slower way when not.
    // this was from a challenge in the book to fix which is apparently not much of a performance issue anyway.
    // NOTE: removed after new changes made dificult to maintain
    // private Integer mCrimeOpenForEdition = null;
    private static final String TAG = "CrimeListFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            notifyChanges();
        }
    }

    private void notifyChanges() {
//        if (mCrimeOpenForEdition != null){
//            Log.d(TAG, "notifyChanges: updated position " + mCrimeOpenForEdition);
//            mAdapter.notifyItemChanged(mCrimeOpenForEdition);
//            mCrimeOpenForEdition = null;
//        }
//        else {
            mAdapter.notifyDataSetChanged();
//        }
    }

    private class CrimeHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private TextView mTitleTextView;
        private TextView mDateTextView;
        private CheckBox mSolvedCheckBox;
        private Integer mPosition;
        private Crime mCrime;
        private static final String TAG = "CrimeHolder";

        public CrimeHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_crime_title_text_view);
            mDateTextView = (TextView) itemView.findViewById(R.id.list_item_crime_date_text_view);
            mSolvedCheckBox = (CheckBox) itemView.findViewById(R.id.list_item_crime_solved_checkbox);
        }

        public void bindCrime(Crime crime, Integer position) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(mCrime.getDate().toString());
            mSolvedCheckBox.setChecked(mCrime.isSolved());
            mPosition = position;
        }


        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: clicked " + v);

            getActivity().setResult(1);

//            Intent intent = CrimeActivity.newIntent(getActivity(), mCrime.getId());
//            startActivity(intent);

//            mCrimeOpenForEdition = mPosition;

            Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getId());
            startActivity(intent);
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {

        private List<Crime> mCrimes;
        private Crime modified;

        public Crime getModified() {
            return modified;
        }

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_crime, parent, false);

            return new CrimeHolder(view);
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);

            holder.bindCrime(crime, position);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }
    }

}
