package com.bignerdranch.android.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat.IntentBuilder;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bignerdranch.android.criminalintent.model.Crime;
import com.bignerdranch.android.criminalintent.model.CrimeLab;
import com.bignerdranch.android.criminalintent.model.DateTimeUtils;

import java.io.File;
import java.util.Date;
import java.util.UUID;

import static com.bignerdranch.android.criminalintent.model.DateTimeUtils.formatDate;
import static com.bignerdranch.android.criminalintent.model.DateTimeUtils.formatTime;

/**
 * Created by lmiceli on 10/05/2016.
 */
public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    // id's to use in FragmentManager list
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final String DIALOG_DISPLAY_IMAGE = "DialogDisplayImage";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PHOTO = 3;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private ImageButton mCallSuspectButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private File mPhotoFile;
    private Callbacks mCallbacks;

    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);

        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        setTitleEditText(v);
        setDateButton(v);
        setTimeButton(v);
        setSolvedCheckBox(v);
        setReportButton(v);
        setSuspectButton(v);
        setCallSuspectButton(v);
        setPhotoButton(v);
        setPhotoView(v);

        return v;
    }

    /**
     * necessary for the tablet 2pane to stay updated on crime edition
     * "CrimeFragment will be doing a Time Warp two-step a lot internally: Jump to the left, save  mCrime to
     * CrimeLab . Step to the right, call  mCallbacks.onCrimeUpdated(Crime)."
     */
    private void updateCrime() {
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }

    private void setPhotoView(View v) {
        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
        buildDisplayImageDialog(v);

        updatePhotoView();
    }

    private void updatePhotoView() {

        ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                if (mPhotoFile == null || !mPhotoFile.exists()) {
                    mPhotoView.setImageDrawable(null);
                } else {
                    Bitmap bitmap;

                    if (mPhotoView.getHeight() > 0 &&
                            mPhotoView.getWidth() > 0) {
                        bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(),
                                mPhotoView.getWidth(), mPhotoView.getHeight());
                    } else {
                        bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
                    }

                    mPhotoView.setImageBitmap(bitmap);
                }

                mPhotoView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        };
        mPhotoView.getViewTreeObserver().addOnGlobalLayoutListener(listener);

    }

    private void setPhotoButton(View v) {
        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);

        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        PackageManager packageManager = getActivity().getPackageManager();
        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;

        if (canTakePhoto) {
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });
    }

    private void setDateButton(View v) {
        mDateButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(mCrime.getDate());
                // IMPORTANT: so we can get the result back on the onActivityResult of CrimeFragment
                // This can be explicitly called when using 2 fragments hosted by the same activity
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });
    }

    private void setTimeButton(View v) {
        mTimeButton = (Button) v.findViewById(R.id.crime_time);
        updateTime();
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment
                        .newInstance(mCrime.getDate());
                // IMPORTANT: so we can get the result back on the onActivityResult of CrimeFragment
                // This can be explicitly called when using 2 fragments hosted by the same activity
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
            }
        });
    }

    private void buildDisplayImageDialog(View v) {
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DisplayImageFragment dialog = DisplayImageFragment
                        .newInstance(mPhotoFile);
                dialog.show(manager, DIALOG_DISPLAY_IMAGE);
            }
        });
    }

    private void setReportButton(View view) {
        mReportButton = (Button) view.findViewById(R.id.crime_report);

        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                IntentBuilder intentBuilder = IntentBuilder.from(getActivity());
                intentBuilder.setChooserTitle(R.string.send_report)
                        .setType("text/plain")
                        .setSubject(getString(R.string.crime_report_subject))
                        .setText(getCrimeReport())
                        .startChooser();

            }
        });

    }

    private void setSuspectButton(View view) {
        mSuspectButton = (Button) view.findViewById(R.id.crime_suspect);

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);

        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        mSuspectButton.setText(getSuspectName());

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

    }

    /**
     *
     * @return
     */
    private String getSuspectName() {
        String suspect = null;
        // Find suspect name in contacts db
        if (mCrime.getSuspectId() != null) {

            // dafault in case no permission to read name
            suspect = mCrime.getSuspectId();

            int permissionToReadContacts = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_CONTACTS);

            if (PackageManager.PERMISSION_GRANTED == permissionToReadContacts) {

                // TODO make a generic code to query without so much boilerplate

                String[] queryFields = new String[]{
                        ContactsContract.Contacts.DISPLAY_NAME
                };

                // TODO make this in some utils
                String selection = ContactsContract.Contacts._ID + " = ?";

                String[] selectionArgs = new String[]{
                        mCrime.getSuspectId(),
                };

                Cursor c = getActivity().getContentResolver()
                        .query(ContactsContract.Contacts.CONTENT_URI,
                                queryFields, selection, selectionArgs, null);
                try {
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        suspect = c.getString(0);
                    }

                } finally {
                    c.close();
                }

            } else {
                askForReadContactsPermission();
            }

        }

        return suspect;
    }

    private void setCallSuspectButton(View view) {
        mCallSuspectButton = (ImageButton) view.findViewById(R.id.crime_call_suspect);
        mCallSuspectButton.setEnabled(false); // Default

        boolean canReadContacts = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;

        if (mCrime.getSuspectId() != null && canReadContacts) {

            String[] queryFields = new String[]{
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            };

            // TODO make this in some utils
            String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";

            String[] selectionArgs = new String[]{
                    mCrime.getSuspectId(),
            };

            Cursor c = getActivity().getContentResolver()
                    .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            queryFields, selection, selectionArgs, null);
            try {
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    String phoneNumber = c.getString(0);

                    mCallSuspectButton.setEnabled(true);

                    final Intent callSuspect = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));

                    mCallSuspectButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(callSuspect);
                        }
                    });

                }

            } finally {
                c.close();
            }

        }


    }

    private void setTitleEditText(View v) {
        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // intentionally blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // also blank
            }
        });
    }

    private void setSolvedCheckBox(View v) {
        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Set the crime's solved property
                mCrime.setSolved(isChecked);
                updateCrime();
            }
        });
    }

    private String getCrimeReport() {

        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = getSuspectName();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * Crime instances get modified in CrimeFragment, and will need to be written out when
     * CrimeFragment is done
     */
    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity())
                .updateCrime(mCrime);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            // preserve time.
            mCrime.setDate(DateTimeUtils.mergeDateAndTime(date, mCrime.getDate()));
            updateCrime();
            updateDate();
        } else if (requestCode == REQUEST_TIME) {
            Date time = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mCrime.setDate(DateTimeUtils.mergeDateAndTime(mCrime.getDate(), time));
            updateCrime();
            updateTime();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            // Specify which fields you want your query to return
            // values for.
            String[] queryFields = new String[]{
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            // Perform your query - the contactUri is like a "where"
            // clause here
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);
            try {
                // Double-check that you actually got results
                if (c.getCount() == 0) {
                    return;
                }
                // Pull out the first column of the first row of data -
                // that is your suspect's name.
                c.moveToFirst();
                String suspectId = c.getString(0);
                String suspectName = c.getString(1);
                mCrime.setSuspectId(suspectId);
                mSuspectButton.setText(suspectName);
                updateCrime();
            } finally {
                c.close();
            }
        } else if (requestCode == REQUEST_PHOTO) {
            updatePhotoView();
            updateCrime();
        }

    }

    private void askForReadContactsPermission() {
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.READ_CONTACTS)) {

            Toast.makeText(getActivity(), R.string.read_contacts_rationale, Toast.LENGTH_SHORT).show();
        } else {

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    mSuspectButton.setText(getSuspectName());
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_crime:
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateTime() {
        mTimeButton.setText(formatTime(getContext(), mCrime.getDate()));
    }

    private void updateDate() {
        mDateButton.setText(formatDate(getContext(), mCrime.getDate()));
    }
}
