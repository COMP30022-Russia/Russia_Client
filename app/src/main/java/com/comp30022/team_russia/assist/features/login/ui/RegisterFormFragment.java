package com.comp30022.team_russia.assist.features.login.ui;

import android.app.DatePickerDialog;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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

    /**
     * The view model.
     */
    private RegisterFormViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private boolean isAp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(RegisterFormViewModel.class);

        isAp = getArguments().getBoolean("apInitiated");
        viewModel.isAp.setValue(isAp);

        // Change toolbar title depending on the selected user type
        ((TitleChangable) Objects.requireNonNull(getActivity())).updateTitle(isAp
            ? getResources().getString(R.string.register_ap)
            : getResources().getString(R.string.register_carer));

        FragmentRegisterFormBinding binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_register_form, container, false);
        // Sets our view model as a variable that can be used by the view.
        // This variable name should be the same as in the one in <data> in activity_login.xml
        binding.setViewModel(viewModel);
        // Allows this Activity to listen for changes in the view model.
        binding.setLifecycleOwner(this);

        setupNavigationHandler(viewModel);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* Last edit text line to automatically confirm when pressing done */
        EditText lastEditText = view.findViewById(isAp ? R.id.edtEmNumber : R.id.edtPassword);
        lastEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        lastEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.confirmClicked();
                return true;
            }
            return false;
        });


        /* Date Picker for DOB */
        Calendar myCalendar = Calendar.getInstance();

        EditText dobEditText = view.findViewById(R.id.edtBirthdate);
        DatePickerDialog.OnDateSetListener date = (view1, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String myFormat = "yyyy-MM-dd";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

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
}
