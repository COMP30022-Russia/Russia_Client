package com.comp30022.team_russia.assist.features.assoc.ui;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseFragment;
import com.comp30022.team_russia.assist.base.di.Injectable;
import com.comp30022.team_russia.assist.databinding.FragmentGenerateQrBinding;
import com.comp30022.team_russia.assist.features.assoc.vm.GenerateQrViewModel;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Objects;

import javax.inject.Inject;

/**
 * Fragment where a QR code is display for other users to scan.
 */
public class GenerateQrFragment extends BaseFragment implements Injectable {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private GenerateQrViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        viewModel = ViewModelProviders.of(this,viewModelFactory).get(GenerateQrViewModel.class);
        FragmentGenerateQrBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_generate_qr, container, false);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        setupNavigationHandler(viewModel);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // Observe token in VM and generate/update QR code when appropriate
        ImageView qrCodeImageView = Objects.requireNonNull(
            getView()).findViewById(R.id.QRImageView);
        viewModel.token.observe(this,
            token -> qrCodeImageView.setImageBitmap(convertContentToBitmap(token)));
    }

    /**
     * Converts a string into a QR code bitmap.
     * @param content String to be converted
     * @return QR code (as Bitmap), representing content
     */
    private Bitmap convertContentToBitmap(String content) {
        // Create/encode bitmap
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        Bitmap bitmap = null;
        try {
            bitmap = barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 200, 200);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        // Return bitmap
        return bitmap;
    }
}
