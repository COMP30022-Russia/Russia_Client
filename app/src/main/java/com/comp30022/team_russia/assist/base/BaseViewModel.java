package com.comp30022.team_russia.assist.base;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.os.Bundle;
import android.util.Pair;

import com.shopify.livedataktx.SupportMediatorLiveData;

//CHECKSTYLE.OFF: AvoidStarImportCheck
import kotlin.jvm.functions.*;
//CHECKSTYLE.ON: AvoidStarImportCheck


/**
 * Base class for our view models.
 * Implements some helper methods.
 */
public abstract class BaseViewModel extends ViewModel {
    public final SingleLiveEvent<NavigationEventArgs> navigateAction = new SingleLiveEvent<>();

    protected void navigateTo(int actionId, Bundle args, boolean shouldClearStack) {
        navigateAction.postValue(new NavigationEventArgs(actionId, shouldClearStack, args));
    }

    protected void navigateTo(int actionId, Bundle args) {
        navigateTo(actionId, args, false);
    }

    protected void navigateTo(int actionId) {
        navigateTo(actionId, null);
    }

}
