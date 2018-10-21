package com.comp30022.team_russia.assist;

import com.google.android.gms.maps.model.LatLng;
import java9.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.net.Uri;

import com.comp30022.team_russia.assist.base.ActionResult;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;
import com.comp30022.team_russia.assist.features.login.models.AssistedPerson;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.profile.services.ProfileDetailsService;
import com.comp30022.team_russia.assist.features.profile.services.ProfileDetailsServiceImpl;
import com.comp30022.team_russia.assist.features.location.services.RealTimeLocationService;
import com.comp30022.team_russia.assist.features.location.services.RealTimeLocationServiceImpl;
import com.comp30022.team_russia.assist.features.profile.vm.UserDetailViewModel;
import com.comp30022.team_russia.assist.util.LastCall;
import com.comp30022.team_russia.assist.util.TestLoggerFactory;
import com.comp30022.team_russia.assist.util.TestToastService;

import static org.mockito.Mockito.*;

public class UserDetailViewModelTest extends TestBase {

    private UserService userService;

    private ToastService toastService;

    private LoggerFactory loggerFactory;

    private RealTimeLocationService realTimeLocationService;

    private ProfileDetailsService profileDetailsService;

    private UserDetailViewModel viewModel;

    /**
     * Fake user.
     */
    private User user;

    /**
     * Fake location.
     */
    private LatLng location;

    @Before
    public void setUp() {
        userService = mock(UserService.class);
        toastService = new TestToastService();
        loggerFactory = new TestLoggerFactory();
        realTimeLocationService = mock(RealTimeLocationServiceImpl.class);
        profileDetailsService = mock(ProfileDetailsServiceImpl.class);

        viewModel = new UserDetailViewModel(userService, toastService, loggerFactory,
            realTimeLocationService, profileDetailsService, executorService);

        user = new AssistedPerson(1, "AA", "", "A",
            "1", "2002-1-1", "Y", "2");
        location = new LatLng(1, 1);

        // Mock service calls
        when(userService.getUserFromAssociation(1)).thenReturn(CompletableFuture.completedFuture(
            new ActionResult<>(user)));
        when(profileDetailsService.getUsersProfilePicture(1)).thenReturn(CompletableFuture.completedFuture(
            new ActionResult<>(new LiveData<String>() {
                @Override
                public String getValue() {
                    return "hello";
                }
            })
        ));
        when(realTimeLocationService.getApCurrentLocation(1)).thenReturn(CompletableFuture.completedFuture(
            new ActionResult<>(location)
        ));

        // Set association ID
        viewModel.associationId.postValue(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void get_other_user_name_should_update_name() {
        // Observe name
        Observer<String> otherUserNameObserver = mock(Observer.class);
        viewModel.otherUserName.observeForever(otherUserNameObserver);

        viewModel.getOtherUserDetails();
        verify(otherUserNameObserver, LastCall.lastCall()).onChanged(user.getRealName());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void get_other_user_name_should_update_age() {
        // Observe age
        Observer<String> otherUserAgeObserver = mock(Observer.class);
        viewModel.otherUserAge.observeForever(otherUserAgeObserver);

        viewModel.getOtherUserDetails();
        verify(otherUserAgeObserver,
            LastCall.lastCall()).onChanged(Integer.toString(user.getAge()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void get_other_user_name_should_update_mobile_number() {
        // Observe mobile number
        Observer<String> otherUserMobileNumberObserver = mock(Observer.class);
        viewModel.otherUserMobileNumber.observeForever(otherUserMobileNumberObserver);

        viewModel.getOtherUserDetails();
        verify(otherUserMobileNumberObserver, LastCall.lastCall()).onChanged(user.getMobileNumber());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void get_other_user_name_should_update_image_uri() {
        // Observe image URI
        Observer<Uri> otherUserImageUriObserver = mock(Observer.class);
        viewModel.otherUserImageUri.observeForever(otherUserImageUriObserver);

        viewModel.getOtherUserDetails();
        verify(otherUserImageUriObserver, LastCall.lastCall()).onChanged(Uri.parse("Hello"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void get_ap_location_should_update_ap_location() {
        // Observe location
        Observer<LatLng> currentApLocation_observer = mock(Observer.class);
        viewModel.currentApLocation.observeForever(currentApLocation_observer);

        // Need to retrieve details before finding location.
        viewModel.getOtherUserDetails();
        viewModel.getApLocation();

        verify(currentApLocation_observer, LastCall.lastCall()).onChanged(location);
    }
}
