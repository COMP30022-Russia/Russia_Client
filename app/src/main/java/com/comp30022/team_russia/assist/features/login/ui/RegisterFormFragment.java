package com.comp30022.team_russia.assist.features.login.ui;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.EditText;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseFragment;
import com.comp30022.team_russia.assist.base.TitleChangable;
import com.comp30022.team_russia.assist.base.di.Injectable;
import com.comp30022.team_russia.assist.databinding.FragmentRegisterFormBinding;
import com.comp30022.team_russia.assist.features.login.vm.RegisterFormViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

/**
 * Registration Form (for both AP and Carer).
 */
public class RegisterFormFragment extends BaseFragment implements Injectable {
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    /**
     * The view model.
     */
    private RegisterFormViewModel viewModel;

    private FragmentRegisterFormBinding binding;

    private boolean isAp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(RegisterFormViewModel.class);

        assert getArguments() != null;
        isAp = getArguments().getBoolean("apInitiated");
        viewModel.isAp.setValue(isAp);

        // Change toolbar title depending on the selected user type
        ((TitleChangable) Objects.requireNonNull(getActivity())).updateTitle(isAp
            ? getResources().getString(R.string.register_ap)
            : getResources().getString(R.string.register_carer));

        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_register_form, container, false);
        // Sets our view model as a variable that can be used by the view.
        // This variable name should be the same as in the one in <data> in activity_login.xml
        binding.setViewModel(viewModel);
        // Allows this Activity to listen for changes in the view model.
        binding.setLifecycleOwner(this);

        setupNavigationHandler(viewModel);

        return binding.getRoot();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get last EditText line to automatically confirm when pressing done
        EditText lastEditText = view.findViewById(isAp ? R.id.edtEmNumber : R.id.edtPassword);
        lastEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        lastEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.confirmClicked();
                return true;
            }
            return false;
        });

        // Move to Birthday when pressing next
        EditText beforeDobEditText = view.findViewById(R.id.edtMobile);
        beforeDobEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        beforeDobEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.edtBirthdate.performClick();
                return true;
            }
            return false;
        });

        // Define listener for date picker
        DatePickerDialog.OnDateSetListener dateSetListener;
        dateSetListener = (datePicker, year, month, day) -> {
            String date = String.format("%04d-%02d-%02d", year, month + 1, day);
            viewModel.birthDate.setValue(date);
            view.findViewById(R.id.edtPassword).requestFocus();
        };

        // When clicking Birth date EditText, bring up date picker dialog
        binding.edtBirthdate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                Objects.requireNonNull(getContext()),
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                dateSetListener,
                year, month, day);
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });
    }
}
