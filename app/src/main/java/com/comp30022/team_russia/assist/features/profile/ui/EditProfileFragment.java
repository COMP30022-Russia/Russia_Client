package com.comp30022.team_russia.assist.features.profile.ui;

import android.app.DatePickerDialog;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
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

import javax.inject.Inject;

/**
 * Edit ProfileFragment.
 */
public class EditProfileFragment extends BaseFragment implements Injectable {

    private EditProfileViewModel viewModel;

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private MenuItem confirmButton;


    @Inject
    ViewModelProvider.Factory viewModelFactory;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(EditProfileViewModel.class);

        FragmentEditProfileBinding binding = DataBindingUtil.inflate(inflater,
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


        /* Date Picker for DOB */
        Calendar myCalendar = Calendar.getInstance();

        EditText dobEditText = view.findViewById(R.id.edtBirthdate);
        DatePickerDialog.OnDateSetListener date = (view1, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);

            dobEditText.setText(sdf.format(myCalendar.getTime()));
        };

        dobEditText.setOnClickListener(v -> new DatePickerDialog(getContext(), date, myCalendar
            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
            myCalendar.get(Calendar.DAY_OF_MONTH)).show());

        EditText beforeDobEditText = view.findViewById(R.id.edtMobile);
        beforeDobEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        beforeDobEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                view.findViewById(R.id.edtPassword).requestFocus();
                dobEditText.performClick();
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
