package com.comp30022.team_russia.assist.features.profile.vm;

import android.arch.lifecycle.MutableLiveData;
import android.graphics.Bitmap;
import android.util.Log;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.features.login.models.AssistedPerson;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.profile.models.ProfilePic;
import com.comp30022.team_russia.assist.features.profile.services.ProfileService;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;


/**
 * ProfileViewModel.
 */
/**
 * ViewModel for RegisterAP / RegisterCarer screen.
 */

public class ProfileViewModel extends BaseViewModel {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private final AuthService authService;
    private final ProfileService profileService;
    private final ToastService toastService;

    /**
     * Whether the user is AP (true) or Carer (false).
     */
    public final MutableLiveData<Boolean> isAp = new MutableLiveData<>();

    /**
     * The "Name" field.
     */
    public final MutableLiveData<String> name = new MutableLiveData<>();

    /**
     * The "Birth date" field.
     */
    public final MutableLiveData<String> birthDate = new MutableLiveData<>();

    /**
     * The "Mobile number" field.
     */
    public final MutableLiveData<String> mobileNumber = new MutableLiveData<>();

    /**
     * The "Password" field.
     */
    public final MutableLiveData<String> password = new MutableLiveData<>();

    /**
     * The "Emergency contact name" field.
     */
    public final MutableLiveData<String> emergencyName = new MutableLiveData<>();

    /**
     * The "Emergency contact number" field.
     */
    public final MutableLiveData<String> emergencyNumber = new MutableLiveData<>();

    public final MutableLiveData<Bitmap> profilePic = new MutableLiveData<>();


    /**
     * Constructor.
     */
    @Inject
    public ProfileViewModel(AuthService authService,
                            ProfileService profileService,
                            ToastService toastService) {

        this.authService = authService;
        this.profileService = profileService;
        this.toastService = toastService;

        reload();

    }


    private String formatDate(Date date) {
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        try {
            String formattedDate = df.format(date);
            return formattedDate;
        } catch (Exception fe) {
            return "Null";
        }
    }

    public void reload() {
        authService.isLoggedIn().observeForever(loggedIn -> {
            if (loggedIn == null || loggedIn == false) {
                return;
            }
            profileService.getDetails().thenAccept((isOk) -> {
                if (isOk) {
                    User profUser = profileService.getCurrentUser();
                    User user = authService.getCurrentUser();
                    if (user.getUserType() == User.UserType.AP) {
                        isAp.postValue(true);
                        AssistedPerson userap = (AssistedPerson)profileService.getCurrentUser();
                        emergencyName.postValue(userap.getEmergencyContactName());
                        emergencyNumber.postValue(userap.getEmergencyContactNumber());
                    } else {
                        isAp.postValue(false);
                    }

                    name.postValue(profUser.getRealname());
                    mobileNumber.postValue(profUser.getMobileNumber());
                    birthDate.postValue(profUser.getDateOfBirth());
                } else {
                    toastService.toastShort("Login failed.");
                }
            });

            profileService.getPic().thenAccept((isOk) -> {
                ProfilePic profileImage = profileService.getProfilePic();
                profilePic.postValue(profileImage.getProfilePicture());
            });
        });

    }

    public void updatePic(File file) {
        profileService.updatePic(file);
    }

    /**
     * Handle event edit profile button clicked.
     */
    public void onEditProfileButtonClicked() {
        navigateTo(R.id.action_edit_profile);
        Log.d("profile","Button pressed");
    }

}
