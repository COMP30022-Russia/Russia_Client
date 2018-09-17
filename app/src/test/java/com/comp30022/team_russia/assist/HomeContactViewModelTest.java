package com.comp30022.team_russia.assist;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.util.Pair;

import com.comp30022.team_russia.assist.features.assoc.models.AssociationDTO;
import com.comp30022.team_russia.assist.features.assoc.models.UserResponseDTO;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;
import com.comp30022.team_russia.assist.features.home_contacts.models.ContactListItemData;
import com.comp30022.team_russia.assist.features.home_contacts.ui.HomeContactViewModel;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.message.models.Association;
import com.comp30022.team_russia.assist.util.LastCall;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java9.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

public class HomeContactViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule
        = new InstantTaskExecutorRule();

    private UserService userService;
    private AuthService authService;

    private HomeContactViewModel viewModel;

    @Before
    public void setUp() {
        userService = mock(UserService.class);
        authService = mock(AuthService.class);

        viewModel = new HomeContactViewModel(authService, userService);

        when(authService.isLoggedInUnboxed()).thenReturn(true);

        List<AssociationDTO> associations = new ArrayList<>();
        UserResponseDTO user1 = mock(UserResponseDTO.class);
        when(user1.getId()).thenReturn(1);
        when(user1.getName()).thenReturn("User 1");

        UserResponseDTO user2 = mock(UserResponseDTO.class);
        when(user2.getId()).thenReturn(2);
        when(user2.getName()).thenReturn("User 2");

        UserResponseDTO user3 = mock(UserResponseDTO.class);
        when(user3.getId()).thenReturn(9);
        when(user3.getName()).thenReturn("User 9");

        associations.add(new AssociationDTO(1, user1));
        associations.add(new AssociationDTO(2, user2));
        associations.add(new AssociationDTO(3, user3));
        when(userService.getAssociatedUsers()).thenReturn(
            CompletableFuture.completedFuture(associations));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_load_contacts() {
        Observer<List<ContactListItemData>> observer = mock(Observer.class);

        viewModel.contactList.observeForever(observer);
        viewModel.reloadContactList();

        List<ContactListItemData> expectResult = new ArrayList<>();

        expectResult.add(new ContactListItemData(1, 1, "User 1", "No message"));
        expectResult.add(new ContactListItemData(2, 2, "User 2", "No message"));
        expectResult.add(new ContactListItemData(3, 9, "User 9", "No message"));

        verify(observer, LastCall.lastCall()).onChanged(expectResult);
    }

    @Test
    public void should_not_load_when_not_authenticated() {
        when(authService.isLoggedInUnboxed()).thenReturn(false);
        Observer<List<ContactListItemData>> observer = mock(Observer.class);

        viewModel.contactList.observeForever(observer);
        viewModel.reloadContactList();

        verify(observer, LastCall.lastCall()).onChanged(new ArrayList<>());
    }

    @Test
    public void should_navigate_when_item_clicked() {
        Observer<Pair<Integer, Bundle>> observer = mock(Observer.class);

        viewModel.navigateAction.observeForever(observer);
        viewModel.reloadContactList();
        viewModel.onListItemClicked(new ContactListItemData(1, 1, "User 1", "No message"));

        Bundle expectedBundle = new Bundle();
        expectedBundle.putInt("associationId", 1);

        // verify that "navigationAction" is fired
        verify(observer, LastCall.lastCall())
            .onChanged(new Pair<>(R.id.action_view_chat, any()));

        // @todo: Verify the argument for navigation
        // We can't actually verify what's in the Pair, Bundle now,
        // because Bundle is not mocked.
        // What we can only verify is that the navigation event is indeed fired.
    }
}
