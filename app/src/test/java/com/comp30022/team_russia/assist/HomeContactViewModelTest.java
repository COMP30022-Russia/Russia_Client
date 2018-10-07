package com.comp30022.team_russia.assist;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.os.Bundle;

import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.NavigationEventArgs;
import com.comp30022.team_russia.assist.features.assoc.models.AssociationDto;
import com.comp30022.team_russia.assist.features.assoc.models.UserResponseDto;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;
import com.comp30022.team_russia.assist.features.home_contacts.models.ContactListItemData;
import com.comp30022.team_russia.assist.features.home_contacts.vm.HomeContactViewModel;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.push.services.PubSubHub;
import com.comp30022.team_russia.assist.util.LastCall;

import com.comp30022.team_russia.assist.util.TestLoggerFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java9.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HomeContactViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule
        = new InstantTaskExecutorRule();

    private UserService userService;
    private AuthService authServiceNotLoggedIn;
    private AuthService authServiceLoggedIn;
    private PubSubHub pubSubHub;
    private final LoggerFactory testLoggerFactory =  new TestLoggerFactory();

    private HomeContactViewModel viewModel;

    @Before
    public void setUp() {
        // Mock an AuthService that is in "logged in" state
        authServiceLoggedIn = mock(AuthService.class);
        MutableLiveData<Boolean> trueLiveData = new MutableLiveData<>();
        trueLiveData.setValue(true);
        when(authServiceLoggedIn.isLoggedInUnboxed()).thenReturn(true);
        when(authServiceLoggedIn.isLoggedIn()).thenReturn(trueLiveData);

        // Mock an AuthService that is in "logged out" state
        authServiceNotLoggedIn = mock(AuthService.class);
        MutableLiveData<Boolean> falseLiveData = new MutableLiveData<>();
        falseLiveData.setValue(false);
        when(authServiceNotLoggedIn.isLoggedInUnboxed()).thenReturn(false);
        when(authServiceNotLoggedIn.isLoggedIn()).thenReturn(falseLiveData);

        pubSubHub = mock(PubSubHub.class);
        when(pubSubHub.subscribe(any(), any())).thenReturn(() -> { });


        List<AssociationDto> associations = new ArrayList<>();
        UserResponseDto user1 = mock(UserResponseDto.class);
        when(user1.getId()).thenReturn(1);
        when(user1.getName()).thenReturn("User 1");

        UserResponseDto user2 = mock(UserResponseDto.class);
        when(user2.getId()).thenReturn(2);
        when(user2.getName()).thenReturn("User 2");

        UserResponseDto user3 = mock(UserResponseDto.class);
        when(user3.getId()).thenReturn(9);
        when(user3.getName()).thenReturn("User 9");

        associations.add(new AssociationDto(1, user1));
        associations.add(new AssociationDto(2, user2));
        associations.add(new AssociationDto(3, user3));
        userService = mock(UserService.class);
        when(userService.getAssociatedUsers()).thenReturn(
            CompletableFuture.completedFuture(associations));

    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_load_contacts() {
        viewModel = new HomeContactViewModel(authServiceLoggedIn, userService, testLoggerFactory, pubSubHub);
        Observer<List<ContactListItemData>> observer = mock(Observer.class);

        viewModel.contactList.observeForever(observer);

        List<ContactListItemData> expectResult = new ArrayList<>();

        expectResult.add(new ContactListItemData(1, 1, "User 1", "No message"));
        expectResult.add(new ContactListItemData(2, 2, "User 2", "No message"));
        expectResult.add(new ContactListItemData(3, 9, "User 9", "No message"));

        verify(observer, LastCall.lastCall()).onChanged(expectResult);
    }

    @Test
    public void should_not_load_when_not_authenticated() {
        viewModel = new HomeContactViewModel(authServiceNotLoggedIn, userService, testLoggerFactory, pubSubHub);

        Observer<List<ContactListItemData>> observer = mock(Observer.class);

        viewModel.contactList.observeForever(observer);

        verify(observer, LastCall.lastCall()).onChanged(new ArrayList<>());
    }

    @Test
    public void should_navigate_when_item_clicked() {
        viewModel = new HomeContactViewModel(authServiceLoggedIn, userService, testLoggerFactory, pubSubHub);

        Observer<NavigationEventArgs> observer = mock(Observer.class);

        viewModel.navigateAction.observeForever(observer);
        viewModel.onListItemClicked(new ContactListItemData(1, 1, "User 1", "No message"));

        Bundle expectedBundle = new Bundle();
        expectedBundle.putInt("associationId", 1);

        // verify that "navigationAction" is fired
        verify(observer, atLeastOnce()).onChanged(any());

        NavigationEventArgs args = viewModel.navigateAction.getValue();
        assertEquals(R.id.action_view_chat, args.getActionId());
        // @todo: Verify the argument for navigation
        // We can't actually verify what's in the Bundle now,
        // because Bundle is not mocked.
    }
}
