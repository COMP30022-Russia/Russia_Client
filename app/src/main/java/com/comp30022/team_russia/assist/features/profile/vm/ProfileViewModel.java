package com.comp30022.team_russia.assist.features.profile.vm;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.net.Uri;
import android.util.Log;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.features.login.models.AssistedPerson;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.profile.services.ProfileDetailsService;

import javax.inject.Inject;


/**
 * ProfileViewModel.
 */
public class ProfileViewModel extends BaseViewModel {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private final AuthService authService;
    private final ProfileDetailsService profileDetailsService;
    private final ToastService toastService;
    private final LoggerInterface logger;

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

    public final MediatorLiveData<Uri> profilePicUri = new MediatorLiveData<>();


    /**
     * Constructor.
     */
    @Inject
    public ProfileViewModel(AuthService authService,
                            ProfileDetailsService profileDetailsService,
                            ToastService toastService,
                            LoggerFactory  loggerFactory) {

        this.authService = authService;
        this.profileDetailsService = profileDetailsService;
        this.toastService = toastService;
        this.logger  = loggerFactory.getLoggerForClass(this.getClass());

        //profilePicUri.postValue(null);
        reload();
    }

    /**
     * Sets up an observer to reload profile on login.
     */
    public void reload() {
        authService.isLoggedIn().observeForever(loggedIn -> {
            if (loggedIn == null || !loggedIn) {
                return;
            }
            profileDetailsService.getDetails().thenAccept((isOk) -> {
                if (isOk) {
                    User profUser = profileDetailsService.getCurrentUser();
                    User user = authService.getCurrentUser();
                    if (user.getUserType() == User.UserType.AP) {
                        isAp.postValue(true);
                        AssistedPerson userAp = (AssistedPerson)
                            profileDetailsService.getCurrentUser();
                        emergencyName.postValue(userAp.getEmergencyContactName());
                        emergencyNumber.postValue(userAp.getEmergencyContactNumber());
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

            // load picture

            profileDetailsService.getPic().thenAccept((isOk) -> {
                logger.debug("isOK:" + isOk);
                LiveData<String> x = profileDetailsService.getProfilePicPath();
                profilePicUri.addSource(x, (path) -> {
                    logger.debug("uri:" + path);
                    if (path == null) {
                       // profilePicUri.postValue(null);
                    } else {
                        profilePicUri.postValue(Uri.parse(path));
                    }
                });
            });
        });

    }

    public void updatePic(String filePath) {
        profileDetailsService.updatePic(filePath);
    }

    /**
     * Handle event edit profile button clicked.
     */
    public void onEditProfileButtonClicked() {
        navigateTo(R.id.action_edit_profile);
        Log.d("profile","Button pressed");
    }

    /**
     * Handles logout button click.
     */
    public void logout() {
        authService.logout();
    }
}
