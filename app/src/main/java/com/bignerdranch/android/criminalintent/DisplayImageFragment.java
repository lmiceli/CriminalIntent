package com.bignerdranch.android.criminalintent;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by lmiceli on 18/05/2016.
 */
public class DisplayImageFragment extends DialogFragment {

    private static final String ARG_PHOTO_FILE = "photo_file";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        File file = (File) getArguments().getSerializable(ARG_PHOTO_FILE);

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_image_display, null);

        ImageView mPhotoView = (ImageView) v.findViewById(R.id.crime_photo_detail);

        updatePhotoView(mPhotoView, file);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // nothing to do
                        // go back
                    }
                })
                .create();
    }

    public static DisplayImageFragment newInstance(File file) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_PHOTO_FILE, file);

        DisplayImageFragment fragment = new DisplayImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void updatePhotoView(ImageView mPhotoView, File mPhotoFile) {

        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

}
