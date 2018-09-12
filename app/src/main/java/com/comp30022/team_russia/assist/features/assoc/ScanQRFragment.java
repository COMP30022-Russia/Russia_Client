package com.comp30022.team_russia.assist.features.assoc;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseFragment;

import com.comp30022.team_russia.assist.databinding.FragmentScanQrBinding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

// Adapted from:
// https://github.com/journeyapps/zxing-android-embedded/blob/master/sample/src/main/java/example/zxing/ContinuousCaptureActivity.java

public class ScanQRFragment extends BaseFragment {
    // Barcode view
    private DecoratedBarcodeView barcodeView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ScanQRViewModel viewModel = ViewModelProviders.of(this).get(ScanQRViewModel.class);

        FragmentScanQrBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_scan_qr, container, false);

        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        setupNavigationHandler(viewModel);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // Specify scan parameters and start scan
        barcodeView = getView().findViewById(R.id.barcode_scanner);
        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeView.setStatusText("Scan an association QR code");
        barcodeView.decodeSingle(callback);
    }

    /** Callback for barcode scan */
    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null) return;

            // Perform actions with result of scan
            Log.d("Barcode Result", result.getText());
            barcodeView.pause();
        }

        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}
