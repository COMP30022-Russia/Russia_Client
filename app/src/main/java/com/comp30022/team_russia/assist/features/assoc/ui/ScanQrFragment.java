package com.comp30022.team_russia.assist.features.assoc.ui;

import android.Manifest;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseFragment;
import com.comp30022.team_russia.assist.base.di.Injectable;
import com.comp30022.team_russia.assist.databinding.FragmentScanQrBinding;
import com.comp30022.team_russia.assist.features.assoc.vm.ScanQrViewModel;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

// Adapted from:
// https://github.com/journeyapps/zxing-android-embedded/blob/master/sample/src/main/java/example/zxing/ContinuousCaptureActivity.java
// https://developer.android.com/training/permissions/requesting

/**
 * Scan QR screen.
 */
public class ScanQrFragment extends BaseFragment implements Injectable {
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private ScanQrViewModel viewModel;

    /**
     * Permissions request code.
     */
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;

    // Barcode view
    private DecoratedBarcodeView barcodeView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ScanQrViewModel.class);

        FragmentScanQrBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_scan_qr, container, false);

        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        // Request for camera permissions
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    Objects.requireNonNull(getActivity()),
                    Manifest.permission.CAMERA)) {
                showExplanation();
            } else {
                requestPermissions(
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
        }

        setupNavigationHandler(viewModel);

        // Watch for navigate back to home event
        viewModel.navigateBackToHome.observe(this,
            value -> Navigation.findNavController(
                Objects.requireNonNull(getView())).popBackStack());
        // Watch and display toast message
        viewModel.toastMessage.observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // Specify scan parameters and start scan
        barcodeView = Objects.requireNonNull(getView()).findViewById(R.id.barcode_scanner);
        Collection<BarcodeFormat> formats =
            Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeView.setStatusText("Scan an association QR code");
        barcodeView.decodeSingle(callback);

        // Stop/start scan depending on isBusy
        viewModel.isBusy.observe(this, isBusy -> {
            if (isBusy != null && isBusy) {
                barcodeView.pause();
            } else {
                barcodeView.resume();
            }
        });
    }

    /**
     * Callback for barcode scan.
     */
    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null) {
                return;
            }

            // Perform actions with result of scan
            Log.d("Barcode Result", result.getText());
            viewModel.onScanResult(result.getText());
        }

        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // Pause scanning when switching tabs.
        if (barcodeView != null) {
            if (isVisibleToUser) {
                barcodeView.resume();
            } else {
                barcodeView.pause();
            }
        }
    }

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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode) {
        case MY_PERMISSIONS_REQUEST_CAMERA: {
            // If request is cancelled, the result array is empty
            if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // Permission denied
                showDeniedDialog();
            }
            return;
        }
        default:
            break;
        }
    }

    /**
     * Shows permission request explanation.
     */
    private void showExplanation() {
        AlertDialog alertDialog = new AlertDialog.Builder(
            Objects.requireNonNull(getContext())).create();
        alertDialog.setTitle("Camera Access");
        alertDialog.setMessage("Camera is needed to scan QR code.");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
            (dialog, which) -> {
                dialog.dismiss();
                requestPermissions(
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
            });
        alertDialog.show();
    }

    /**
     * Called when user denies permission request.
     */
    private void showDeniedDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(
            Objects.requireNonNull(getContext())).create();
        alertDialog.setTitle("Denied Camera Access");
        alertDialog.setMessage("Camera is needed to scan QR code. Please accept.");

        // Re-request for permissions
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Review Camera Access",
            (dialog, which) -> {
                dialog.dismiss();
                requestPermissions(
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);

            });

        // Go to My QR tab if ignored
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Ignore",
            (dialog, which) -> {
                dialog.dismiss();
                TabLayout tabs = (Objects.requireNonNull(getActivity())).findViewById(R.id.tabs);
                Objects.requireNonNull(tabs.getTabAt(1)).select();
            });

        alertDialog.show();
    }
}
