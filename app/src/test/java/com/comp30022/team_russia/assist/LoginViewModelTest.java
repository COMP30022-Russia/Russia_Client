package com.comp30022.team_russia.assist;

import android.arch.lifecycle.Observer;

import com.comp30022.team_russia.assist.base.NavigationEventArgs;
import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.login.vm.LoginViewModel;
import com.comp30022.team_russia.assist.util.LastCall;

import java9.util.concurrent.CompletableFuture;

import com.comp30022.team_russia.assist.util.TestToastService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for LoginViewModel.
 */
public class LoginViewModelTest extends TestBase {

    private LoginViewModel loginViewModel;

    private AuthService mockAuthService;
    private ToastService mockToastService;

    /**
     * This is called before every test (each test starts in a clean state).
     */
    @Before
    public void setUp() {
        mockAuthService = mock(AuthService.class);
        mockToastService = new TestToastService();
        loginViewModel = new LoginViewModel(mockAuthService, mockToastService);

        // mock the behaviour of AuthService:
        //  - When login("user1", "correct_password") is called, the login
        //    should succeed.
        //  - When login("user1", "wrong_password") is called, the login
        //    should fail.
        when(mockAuthService.login("user1",
            "correct_password"))
            .thenReturn(CompletableFuture.completedFuture(true));
        when(mockAuthService.login("user1",
            "wrong_password"))
            .thenReturn(CompletableFuture.completedFuture(false));
    }

    /**
     * Ensure that the confirm button is not enabled by default.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void confirmBtn_disabled_by_default() {
        Observer<Boolean> observer = mock(Observer.class);

        loginViewModel.isLoginButtonEnabled.observeForever(observer);

        verify(observer, LastCall.lastCall()).onChanged(false);
    }

    /**
     * Ensure that the confirm button is enabled after entering username
     * and password.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void confirmBtn_enabled_when_inputs_valid() {
        // We use LiveData fields on our ViewModels.
        // LiveData is like an observable (think about the publisher in the
        // observable pattern).
        //
        // The UI creates observers that listen to changes in LiveData. In the
        // application, this is automatically achieved by data binding library.
        //
        // But here, we need to create a mock observer manually, so that
        // we can verify that values in the LiveData fields are what we expect.

        // The target field we are verifying, i.e. isLoginButtonEnabled, is
        // MutableLiveData<Boolean>. Hence, we create a Observer<Boolean>.

        Observer<Boolean> observer = mock(Observer.class);

        // observeForever subscribes the observer to changes in
        // isLoginButtonEnabled.
        loginViewModel.isLoginButtonEnabled.observeForever(observer);

        // Now let's mock the user behaviour. The "user" is entering the
        // username and password. (In the application, loginViewModel.username
        // and loginViewModel.password are automatically updated by data
        // binding.)
        loginViewModel.username.postValue("myusername");
        loginViewModel.password.postValue("mypassword");

        // Now we need to verify that the value of isLoginButtonEnabled is
        // indeed changed to true.

        // When the value of isLoginButtonEnabled changed, it calls the
        // onChange method on the observer, passing in the new value.
        // So we are verifying that observer.onChanged(true) has been called.

        // Note that observer.onChanged(true) could be called multiple times,
        // for example:
        //  - Initially the value is false. -> observer.onChanged(false) called.
        //  - When username is set, isLoginButtonEnabled recalculates, but
        //    the value is still false.     -> observer.onChanged(false) called.
        //  - When the password is set, the value updates again, this time to
        //    true.                         -> observer.onChanged(true) called.
        // We are only interested in the last call, which represents the final/
        // stable value of isLoginButtonEnabled.
        // (This is oversimplified, just to explain why LastCall.lastCall() is
        // needed)

        verify(observer, LastCall.lastCall()).onChanged(true);
    }

    /**
     * Ensure that a successful login clears the input.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void successful_login_clears_the_input() {
        Observer<Boolean> observerLoginBtnEnabled = mock(Observer.class);
        Observer<String> observerUsername = mock(Observer.class);
        Observer<String> observerPassword = mock(Observer.class);

        loginViewModel.isLoginButtonEnabled
            .observeForever(observerLoginBtnEnabled);
        loginViewModel.username.observeForever(observerUsername);
        loginViewModel.password.observeForever(observerPassword);


        loginViewModel.username.postValue("user1");
        loginViewModel.password.postValue("correct_password");
        verify(observerLoginBtnEnabled, LastCall.lastCall()).onChanged(true);

        loginViewModel.loginClicked();
        // verify that the username and password is correctly passed to
        // the service
        verify(mockAuthService).login("user1",
            "correct_password");

        // Verify that after login, the inputs are cleared.
        verify(observerLoginBtnEnabled, LastCall.lastCall())
            .onChanged(false);
        verify(observerUsername, LastCall.lastCall()).onChanged("");
        verify(observerPassword, LastCall.lastCall()).onChanged("");

        // Verify that authService is called
        verify(mockAuthService, atLeastOnce()).login(any(), any());

    }

    /**
     * Ensure that a failed login retains the input.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void failed_login_keeps_the_input() {
        Observer<Boolean> observerLoginBtnEnabled = mock(Observer.class);
        Observer<String> observerUsername = mock(Observer.class);
        Observer<String> observerPassword = mock(Observer.class);

        loginViewModel.isLoginButtonEnabled
            .observeForever(observerLoginBtnEnabled);
        loginViewModel.username.observeForever(observerUsername);
        loginViewModel.password.observeForever(observerPassword);


        loginViewModel.username.postValue("user1");
        loginViewModel.password.postValue("wrong_password");
        verify(observerLoginBtnEnabled, LastCall.lastCall()).onChanged(true);

        loginViewModel.loginClicked();
        // verify that the username and password is correctly passed to
        // the service
        verify(mockAuthService).login("user1", "wrong_password");

        // Verify that after login, the inputs are retained
        verify(observerLoginBtnEnabled, LastCall.lastCall()).onChanged(true);
        verify(observerUsername, LastCall.lastCall())
            .onChanged("user1");
        verify(observerPassword, LastCall.lastCall())
            .onChanged("wrong_password");

        // Verify that authService is called
        verify(mockAuthService, atLeastOnce()).login(any(), any());
    }

    /**
     * Clicking on the "Register" link should trigger navigation.
     */
    @Test
    public void register_navigation() {
        // simulate user clicking on "Register"
        loginViewModel.registerClicked();

        // verify that it does navigate to the RegisterChooseType screen.
        NavigationEventArgs args = loginViewModel.navigateAction.getValue();
        assertEquals(R.id.action_register, args.getActionId());
    }

}
