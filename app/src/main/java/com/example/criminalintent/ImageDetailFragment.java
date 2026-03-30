package com.example.criminalintent;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ImageDetailFragment extends DialogFragment {

    private static final String ARG_IMAGE_PATH = "image_path";

    public static ImageDetailFragment newInstance(String imagePath) {
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_PATH, imagePath);

        ImageDetailFragment fragment = new ImageDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make the dialog full screen
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_crime_image, container, false);

        ImageView imageView = v.findViewById(R.id.crime_image_full);
        String imagePath = getArguments().getString(ARG_IMAGE_PATH);

        if (imagePath != null) {
            Bitmap bitmap = PictureUtils.getScaledBitmap(imagePath, getActivity());
            imageView.setImageBitmap(bitmap);
        }

        // Close the dialog when the image is clicked
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return v;
    }
}
