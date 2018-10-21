package com.comp30022.team_russia.assist.features.profile.vm;

import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.net.Uri;

import com.comp30022.team_russia.assist.base.BaseViewModel;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;
import com.comp30022.team_russia.assist.features.location.services.RealTimeLocationService;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.profile.services.ProfileDetailsService;

import com.google.android.gms.maps.model.LatLng;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

/**
 * User Detail View Model.
 */
public class UserDetailViewModel extends BaseViewModel {
    private final UserService userService;
    private final RealTimeLocationService realTimeLocationService;
    private final ProfileDetailsService profileDetailsService;
    private final ToastService toastService;
    private final LoggerInterface logger;
    private final ExecutorService executorService;

    /**
     * Whether the current user is an AP.
     */
    public boolean currentUserIsAp;

    /**
     * Association ID (from bundle).
     */
    public final MutableLiveData<Integer> associationId = new MutableLiveData<>();

    /**
     * Profile picture of other user.
     */
    public final MediatorLiveData<Uri> otherUserImageUri = new MediatorLiveData<>();

    /**
     * Name of other user.
     */
    public final MutableLiveData<String> otherUserName = new MutableLiveData<>();

    /**
     * Age of other user.
     */
    public final MutableLiveData<String> otherUserAge = new MutableLiveData<>();

    /**
     * Mobile number of other user.
     */
    public final MutableLiveData<String> otherUserMobileNumber = new MutableLiveData<>();

    /**
     * ID of other user (from association call).
     */
    public final MutableLiveData<Integer> otherUserId = new MutableLiveData<>();

    /**
     * AP location.
     */
    public final MutableLiveData<LatLng> currentApLocation = new MutableLiveData<>();

    /**
     * User detail view model.
     * @param userService user service
     * @param toastService toast service
     * @param loggerFactory logger
     * @param realTimeLocationService real time location service
     * @param profileDetailsService profile service
     * @param executorService The executor service.
     */
    @Inject
    public UserDetailViewModel(UserService userService,
                               ToastService toastService,
                               LoggerFactory loggerFactory,
                               RealTimeLocationService realTimeLocationService,
                               ProfileDetailsService profileDetailsService,
                               ExecutorService executorService) {
        this.userService = userService;
        this.realTimeLocationService = realTimeLocationService;
        this.profileDetailsService = profileDetailsService;
        this.toastService = toastService;
        this.logger = loggerFactory.create(this.getClass().getSimpleName());
        this.executorService = executorService;
        otherUserImageUri.postValue(null);
    }

    /**
     * Get the details of the other user to be displayed on the profile page.
     */
    public void getOtherUserDetails() {
        // Get associated user
        if (associationId.getValue() == null) {
            return;
        }
        userService.getUserFromAssociation(associationId.getValue())
            .thenAcceptAsync(result -> {
                if (result.isSuccessful()) {
                    logger.info("getOtherUserDetails called in Vm is SUCCESSFUL");
                    otherUserName.postValue(result.unwrap().getRealName());
                    otherUserAge.postValue(Integer.toString(result.unwrap().getAge()));
                    otherUserMobileNumber.postValue(result.unwrap().getMobileNumber());
                    otherUserId.postValue(result.unwrap().getUserId());
                } else {
                    logger.info("getOtherUserDetails called in Vm FAILED");
                }
            }, executorService);

        // Get user's profile picture
        if (otherUserId.getValue() == null) {
            return;
        }
        profileDetailsService.getUsersProfilePicture(otherUserId.getValue()).thenAccept(result -> {
            if (result.isSuccessful()) {
                otherUserImageUri.addSource(result.unwrap(), (path) -> {
                    if (path == null) {
                        otherUserImageUri.postValue(null);
                    } else {
                        otherUserImageUri.postValue(Uri.parse(path));
                    }
                });
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
        // Get location of AP
        if (otherUserId.getValue() == null) {
            return;
        }
        realTimeLocationService.getApCurrentLocation(otherUserId.getValue()).thenAccept(result -> {
            if (result.isSuccessful()) {
                currentApLocation.postValue(result.unwrap());
                logger.info("getApLocation in vm SUCCESS");
            } else {
                logger.info("getApLocation in vm FAILED");
                toastService.toastLong("Can't retrieve AP's location");
            }
        });
    }
}
