package com.comp30022.team_russia.assist.features.profile.ui;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseFragment;
import com.comp30022.team_russia.assist.base.di.Injectable;
import com.comp30022.team_russia.assist.databinding.FragmentProfileBinding;
import com.comp30022.team_russia.assist.features.profile.vm.ProfileViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.inject.Inject;

/**
 * A fragment for profile screen.
 */
public class ProfileFragment extends BaseFragment implements Injectable {

    private ProfileViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    ImageView profileImage;
    public static final int PICK_IMAGE = 1;
    Uri imageUri;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(ProfileViewModel.class);

        FragmentProfileBinding binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_profile, container, false);

        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        setupNavigationHandler(viewModel);
        
        viewModel.reload();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profileImage = view.findViewById(R.id.profileView);
        profileImage.setImageResource(R.drawable.ic_add);


        profileImage.setOnClickListener(v -> chooseImage());
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap squared;
        if (requestCode == PICK_IMAGE && data != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    this.getContext().getContentResolver(),imageUri);
                squared = squareImage(bitmap);
                profileImage.setImageBitmap(squared);


                //https://stackoverflow.com/questions/45828401/how-to-post-a-bitmap-to-a-server-using-retrofit-android
                String filename = "image.jpg";
                //create a file to write bitmap data
                File file = new File(getContext().getCacheDir(), filename);
                file.createNewFile();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50 /*ignored for PNG*/, bos);
                byte[] bitmapdata = bos.toByteArray();

                //write the bytes in file
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();

                viewModel.updatePic(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Turns picture into square if picture not already a square
    //source: https://stackoverflow.com/questions/6908604/android-crop-center-of-bitmap
    private Bitmap squareImage(Bitmap srcBmp) {
        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()) {

            dstBmp = Bitmap.createBitmap(
                srcBmp,
                srcBmp.getWidth() / 2 - srcBmp.getHeight() / 2,
                0,
                srcBmp.getHeight(),
                srcBmp.getHeight()
            );

        } else {

            dstBmp = Bitmap.createBitmap(
                srcBmp,
                0,
                srcBmp.getHeight() / 2 - srcBmp.getWidth() / 2,
                srcBmp.getWidth(),
                srcBmp.getWidth()
            );
        }
        return dstBmp;
    }



    /** put edit button at the app bar instead. **/

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_edit_profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.edit_profile_icon:
            viewModel.onEditProfileButtonClicked();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
