package com.comp30022.team_russia.assist.features.user_detail.vm;

import android.arch.lifecycle.MutableLiveData;
import android.graphics.Bitmap;
import android.util.Log;

import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.profile.models.ProfilePic;
import com.comp30022.team_russia.assist.features.profile.services.ProfileDetailsService;
import com.comp30022.team_russia.assist.features.user_detail.services.RealTimeLocationService;

import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;

/**
 * User Detail View Model.
 */
public class UserDetailViewModel extends BaseViewModel {

    private static final String TAG = "UserDetailViewModel";

    private final AuthService authService;

    private final UserService userService;

    private final RealTimeLocationService realTimeLocationService;

    private final ProfileDetailsService profileDetailsService;

    private final ToastService toastService;

    private final LoggerInterface logger;

    public boolean currentUserIsAp;

    public final MutableLiveData<Integer> associationId = new MutableLiveData<>();

    public final MutableLiveData<Bitmap> otherUserImage = new MutableLiveData<>();

    public final MutableLiveData<String> otherUserName = new MutableLiveData<>();

    public final MutableLiveData<String> otherUserAge = new MutableLiveData<>();

    public final MutableLiveData<String> otherUserMobileNumber = new MutableLiveData<>();

    public final MutableLiveData<Integer> otherUserId = new MutableLiveData<>();

    public final MutableLiveData<LatLng> currentApLocation = new MutableLiveData<>();


    /**
     * User detail view model.
     * @param authService authentication service
     * @param userService user service
     * @param toastService toast service
     * @param realTimeLocationService real time location service
     */
    @Inject
    public UserDetailViewModel(AuthService authService,
                               UserService userService,
                               ToastService toastService,
                               LoggerFactory loggerFactory,
                               RealTimeLocationService realTimeLocationService,
                               ProfileDetailsService profileDetailsService) {

        this.authService = authService;
        this.userService = userService;
        this.realTimeLocationService = realTimeLocationService;
        this.profileDetailsService = profileDetailsService;
        this.toastService = toastService;
        this.logger = loggerFactory.create(this.getClass().getSimpleName());

    }

    /**
     * Get the details of the other user to be displayed on the profile page.
     */
    public void getOtherUserDetails() {

        logger.info("getOtherUserDetails called in Vm");

        userService.getUserFromAssociation(associationId.getValue())
            .thenAcceptAsync(result -> {
                if (result.isSuccessful()) {
                    logger.info("getOtherUserDetails called in Vm is SUCCESSFUL");

                    otherUserName.postValue(result.unwrap().getRealname());
                    otherUserAge.postValue(Integer.toString(result.unwrap().getAge()));
                    otherUserMobileNumber.postValue(result.unwrap().getMobileNumber());
                    otherUserId.postValue(result.unwrap().getUserId());

                } else {
                    logger.info("getOtherUserDetails called in Vm FAILED");
                    // todo retry
                }
            });

        //todo get profile image
        profileDetailsService.getUsersProfilePicture(otherUserId.getValue()).thenAccept(result -> {
            if (result.isSuccessful()) {
                otherUserImage.postValue(result.unwrap().getProfilePicture());
                logger.info("getOtherUserDetails successfully got image");
            } else {
                toastService.toastShort("other user don't have profile picture");
                logger.error("getOtherUserDetails failed to get profile image");
            }
        });
    }

    /**
     * Get the Ap's location to be displayed on the map view.
     */
    public void getApLocation() {
        logger.info("getApLocation IN VM called");

        realTimeLocationService.getApCurrentLocation(otherUserId.getValue()).thenAccept(result -> {
            if (result.isSuccessful()) {
                currentApLocation.postValue(result.unwrap());
                logger.info("getApLocation in vm SUCCESS");
            } else {
                logger.info("getApLocation in vm FAILED");
                toastService.toastLong("Cant retrieve Ap's location");
            }
        });
    }

}
