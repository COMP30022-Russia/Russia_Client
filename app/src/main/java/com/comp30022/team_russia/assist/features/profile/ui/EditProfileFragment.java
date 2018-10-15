package com.comp30022.team_russia.assist.features.profile.ui;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseFragment;
import com.comp30022.team_russia.assist.base.di.Injectable;
import com.comp30022.team_russia.assist.databinding.FragmentEditProfileBinding;
import com.comp30022.team_russia.assist.features.profile.vm.EditProfileViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

/**
 * Edit ProfileFragment.
 */
public class EditProfileFragment extends BaseFragment implements Injectable {

    private EditProfileViewModel viewModel;

    private FragmentEditProfileBinding binding;

    private MenuItem confirmButton;


    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(EditProfileViewModel.class);

        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_edit_profile, container, false);
        
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
        
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel.shouldExitEditMode.observe(this, this::listenConfirmButtonClicked);

        EditText password = getView().findViewById(R.id.edtPassword);
        password.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (viewModel.password.getValue().equals("Placeholder")) {
                    viewModel.password.setValue("");
                }
            }
            if (!hasFocus) {
                if (viewModel.password.getValue().equals("")) {
                    viewModel.password.setValue("Placeholder");
                }
            }
        });

        // Define listener for date picker
        DatePickerDialog.OnDateSetListener dateSetListener;
        dateSetListener = (datePicker, year, month, day) -> {
            @SuppressLint("DefaultLocale")
            String date = String.format("%04d-%02d-%02d", year, month + 1, day);
            viewModel.birthDate.setValue(date);
        };

        // Listen to click event and bring up date picker
        binding.edtBirthdate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                Objects.requireNonNull(getContext()),
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                dateSetListener,
                year,month,day);
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });

        // Next action on keyboard
        EditText beforeDobEditText = view.findViewById(R.id.edtMobile);
        beforeDobEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        beforeDobEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.edtBirthdate.performClick();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_confirm_edit_profile, menu);

        confirmButton = menu.findItem(R.id.confirm_edit_profile_icon);
        confirmButton.setEnabled(false);
        viewModel.isConfirmButtonEnabled.observe(this, this::enableConfirmButton);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.confirm_edit_profile_icon:
            viewModel.onEditProfileConfirmButtonClicked();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }


    private void listenConfirmButtonClicked(Boolean confirmButtonClicked) {
        if (confirmButtonClicked) {
            getActivity().onBackPressed();
        }
    }

    private void enableConfirmButton(Boolean confirmButtonEnabled) {
        if (confirmButtonEnabled) {
            confirmButton.setEnabled(true);
            confirmButton.setVisible(true);
        } else {
            confirmButton.setEnabled(false);
            confirmButton.setVisible(false);
        }
    }
}
