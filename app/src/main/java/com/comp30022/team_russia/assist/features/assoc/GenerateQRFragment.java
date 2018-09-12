package com.comp30022.team_russia.assist.features.assoc;

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
import com.comp30022.team_russia.assist.databinding.FragmentGenerateQrBinding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class GenerateQRFragment extends BaseFragment {
    private GenerateQRViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(GenerateQRViewModel.class);

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
        ImageView QRCodeImageView = getView().findViewById(R.id.QRImageView);
        viewModel.token.observe(this, token -> QRCodeImageView.setImageBitmap(convertContentToBitmap(token)));
    }

    /**
     * Converts a string into a QR code bitmap
     * @param content String to be converted
     * @return QR code representing content
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
