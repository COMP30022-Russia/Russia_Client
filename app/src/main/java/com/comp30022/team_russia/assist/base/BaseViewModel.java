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

    //CHECKSTYLE.OFF: LineLengthCheck
    //CHECKSTYLE.OFF: MethodTypeParameterNameCheck
    public static <T1, T2, R> LiveData<R> combineLatest(LiveData<T1> source1, LiveData<T2> source2, Function2<T1, T2, R> mapper) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(source1.getValue(), source2.getValue()));

        s.addSource(source1, observer);
        s.addSource(source2, observer);
        return s;
    }


    protected static <T1, T2, T3, R> LiveData<R> combineLatest(LiveData<T1> source1, LiveData<T2> source2, LiveData<T3> source3, Function3<T1, T2, T3, R> mapper) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(source1.getValue(), source2.getValue(), source3.getValue()));

        s.addSource(source1, observer);
        s.addSource(source2, observer);
        s.addSource(source3, observer);
        return s;
    }


    public static <T1, T2, T3, T4, R> LiveData<R> combineLatest(LiveData<T1> source1, LiveData<T2> source2, LiveData<T3> source3, LiveData<T4> source4, Function4<T1, T2, T3, T4, R> mapper) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(source1.getValue(), source2.getValue(), source3.getValue(), source4.getValue()));

        s.addSource(source1, observer);
        s.addSource(source2, observer);
        s.addSource(source3, observer);
        s.addSource(source4, observer);
        return s;
    }


    protected static <T1, T2, T3, T4, T5, R> LiveData<R> combineLatest(LiveData<T1> source1, LiveData<T2> source2, LiveData<T3> source3, LiveData<T4> source4, LiveData<T5> source5, Function5<T1, T2, T3, T4, T5, R> mapper) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(source1.getValue(), source2.getValue(), source3.getValue(), source4.getValue(), source5.getValue()));

        s.addSource(source1, observer);
        s.addSource(source2, observer);
        s.addSource(source3, observer);
        s.addSource(source4, observer);
        s.addSource(source5, observer);
        return s;
    }


    public static <T1, T2, T3, T4, T5, T6, R> LiveData<R> combineLatest(LiveData<T1> source1, LiveData<T2> source2, LiveData<T3> source3, LiveData<T4> source4, LiveData<T5> source5, LiveData<T6> source6, Function6<T1, T2, T3, T4, T5, T6, R> mapper) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(source1.getValue(), source2.getValue(), source3.getValue(), source4.getValue(), source5.getValue(), source6.getValue()));

        s.addSource(source1, observer);
        s.addSource(source2, observer);
        s.addSource(source3, observer);
        s.addSource(source4, observer);
        s.addSource(source5, observer);
        s.addSource(source6, observer);
        return s;
    }


    public static <T1, T2, T3, T4, T5, T6, T7, R> LiveData<R> combineLatest(LiveData<T1> source1, LiveData<T2> source2, LiveData<T3> source3, LiveData<T4> source4, LiveData<T5> source5, LiveData<T6> source6, LiveData<T7> source7, Function7<T1, T2, T3, T4, T5, T6, T7, R> mapper) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(source1.getValue(), source2.getValue(), source3.getValue(), source4.getValue(), source5.getValue(), source6.getValue(), source7.getValue()));

        s.addSource(source1, observer);
        s.addSource(source2, observer);
        s.addSource(source3, observer);
        s.addSource(source4, observer);
        s.addSource(source5, observer);
        s.addSource(source6, observer);
        s.addSource(source7, observer);
        return s;
    }


    protected static <T1, T2, T3, T4, T5, T6, T7, T8, R> LiveData<R> combineLatest(LiveData<T1> source1, LiveData<T2> source2, LiveData<T3> source3, LiveData<T4> source4, LiveData<T5> source5, LiveData<T6> source6, LiveData<T7> source7, LiveData<T8> source8, Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> mapper) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(source1.getValue(), source2.getValue(), source3.getValue(), source4.getValue(), source5.getValue(), source6.getValue(), source7.getValue(), source8.getValue()));

        s.addSource(source1, observer);
        s.addSource(source2, observer);
        s.addSource(source3, observer);
        s.addSource(source4, observer);
        s.addSource(source5, observer);
        s.addSource(source6, observer);
        s.addSource(source7, observer);
        s.addSource(source8, observer);
        return s;
    }


    protected static <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> LiveData<R> combineLatest(LiveData<T1> source1, LiveData<T2> source2, LiveData<T3> source3, LiveData<T4> source4, LiveData<T5> source5, LiveData<T6> source6, LiveData<T7> source7, LiveData<T8> source8, LiveData<T9> source9, Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> mapper) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(source1.getValue(), source2.getValue(), source3.getValue(), source4.getValue(), source5.getValue(), source6.getValue(), source7.getValue(), source8.getValue(), source9.getValue()));

        s.addSource(source1, observer);
        s.addSource(source2, observer);
        s.addSource(source3, observer);
        s.addSource(source4, observer);
        s.addSource(source5, observer);
        s.addSource(source6, observer);
        s.addSource(source7, observer);
        s.addSource(source8, observer);
        s.addSource(source9, observer);
        return s;
    }


    protected static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> LiveData<R> combineLatest(LiveData<T1> source1, LiveData<T2> source2, LiveData<T3> source3, LiveData<T4> source4, LiveData<T5> source5, LiveData<T6> source6, LiveData<T7> source7, LiveData<T8> source8, LiveData<T9> source9, LiveData<T10> source10, Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> mapper) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(source1.getValue(), source2.getValue(), source3.getValue(), source4.getValue(), source5.getValue(), source6.getValue(), source7.getValue(), source8.getValue(), source9.getValue(), source10.getValue()));

        s.addSource(source1, observer);
        s.addSource(source2, observer);
        s.addSource(source3, observer);
        s.addSource(source4, observer);
        s.addSource(source5, observer);
        s.addSource(source6, observer);
        s.addSource(source7, observer);
        s.addSource(source8, observer);
        s.addSource(source9, observer);
        s.addSource(source10, observer);
        return s;
    }


    protected static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> LiveData<R> combineLatest(LiveData<T1> source1, LiveData<T2> source2, LiveData<T3> source3, LiveData<T4> source4, LiveData<T5> source5, LiveData<T6> source6, LiveData<T7> source7, LiveData<T8> source8, LiveData<T9> source9, LiveData<T10> source10, LiveData<T11> source11, Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> mapper) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(source1.getValue(), source2.getValue(), source3.getValue(), source4.getValue(), source5.getValue(), source6.getValue(), source7.getValue(), source8.getValue(), source9.getValue(), source10.getValue(), source11.getValue()));

        s.addSource(source1, observer);
        s.addSource(source2, observer);
        s.addSource(source3, observer);
        s.addSource(source4, observer);
        s.addSource(source5, observer);
        s.addSource(source6, observer);
        s.addSource(source7, observer);
        s.addSource(source8, observer);
        s.addSource(source9, observer);
        s.addSource(source10, observer);
        s.addSource(source11, observer);
        return s;
    }


    protected static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> LiveData<R> combineLatest(LiveData<T1> source1, LiveData<T2> source2, LiveData<T3> source3, LiveData<T4> source4, LiveData<T5> source5, LiveData<T6> source6, LiveData<T7> source7, LiveData<T8> source8, LiveData<T9> source9, LiveData<T10> source10, LiveData<T11> source11, LiveData<T12> source12, Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> mapper) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(source1.getValue(), source2.getValue(), source3.getValue(), source4.getValue(), source5.getValue(), source6.getValue(), source7.getValue(), source8.getValue(), source9.getValue(), source10.getValue(), source11.getValue(), source12.getValue()));

        s.addSource(source1, observer);
        s.addSource(source2, observer);
        s.addSource(source3, observer);
        s.addSource(source4, observer);
        s.addSource(source5, observer);
        s.addSource(source6, observer);
        s.addSource(source7, observer);
        s.addSource(source8, observer);
        s.addSource(source9, observer);
        s.addSource(source10, observer);
        s.addSource(source11, observer);
        s.addSource(source12, observer);
        return s;
    }


    protected static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R> LiveData<R> combineLatest(LiveData<T1> source1, LiveData<T2> source2, LiveData<T3> source3, LiveData<T4> source4, LiveData<T5> source5, LiveData<T6> source6, LiveData<T7> source7, LiveData<T8> source8, LiveData<T9> source9, LiveData<T10> source10, LiveData<T11> source11, LiveData<T12> source12, LiveData<T13> source13, Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R> mapper) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(source1.getValue(), source2.getValue(), source3.getValue(), source4.getValue(), source5.getValue(), source6.getValue(), source7.getValue(), source8.getValue(), source9.getValue(), source10.getValue(), source11.getValue(), source12.getValue(), source13.getValue()));

        s.addSource(source1, observer);
        s.addSource(source2, observer);
        s.addSource(source3, observer);
        s.addSource(source4, observer);
        s.addSource(source5, observer);
        s.addSource(source6, observer);
        s.addSource(source7, observer);
        s.addSource(source8, observer);
        s.addSource(source9, observer);
        s.addSource(source10, observer);
        s.addSource(source11, observer);
        s.addSource(source12, observer);
        s.addSource(source13, observer);
        return s;
    }


    protected static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R> LiveData<R> combineLatest(LiveData<T1> source1, LiveData<T2> source2, LiveData<T3> source3, LiveData<T4> source4, LiveData<T5> source5, LiveData<T6> source6, LiveData<T7> source7, LiveData<T8> source8, LiveData<T9> source9, LiveData<T10> source10, LiveData<T11> source11, LiveData<T12> source12, LiveData<T13> source13, LiveData<T14> source14, Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R> mapper) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(source1.getValue(), source2.getValue(), source3.getValue(), source4.getValue(), source5.getValue(), source6.getValue(), source7.getValue(), source8.getValue(), source9.getValue(), source10.getValue(), source11.getValue(), source12.getValue(), source13.getValue(), source14.getValue()));

        s.addSource(source1, observer);
        s.addSource(source2, observer);
        s.addSource(source3, observer);
        s.addSource(source4, observer);
        s.addSource(source5, observer);
        s.addSource(source6, observer);
        s.addSource(source7, observer);
        s.addSource(source8, observer);
        s.addSource(source9, observer);
        s.addSource(source10, observer);
        s.addSource(source11, observer);
        s.addSource(source12, observer);
        s.addSource(source13, observer);
        s.addSource(source14, observer);
        return s;
    }


    protected static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R> LiveData<R> combineLatest(LiveData<T1> source1, LiveData<T2> source2, LiveData<T3> source3, LiveData<T4> source4, LiveData<T5> source5, LiveData<T6> source6, LiveData<T7> source7, LiveData<T8> source8, LiveData<T9> source9, LiveData<T10> source10, LiveData<T11> source11, LiveData<T12> source12, LiveData<T13> source13, LiveData<T14> source14, LiveData<T15> source15, Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R> mapper) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(source1.getValue(), source2.getValue(), source3.getValue(), source4.getValue(), source5.getValue(), source6.getValue(), source7.getValue(), source8.getValue(), source9.getValue(), source10.getValue(), source11.getValue(), source12.getValue(), source13.getValue(), source14.getValue(), source15.getValue()));

        s.addSource(source1, observer);
        s.addSource(source2, observer);
        s.addSource(source3, observer);
        s.addSource(source4, observer);
        s.addSource(source5, observer);
        s.addSource(source6, observer);
        s.addSource(source7, observer);
        s.addSource(source8, observer);
        s.addSource(source9, observer);
        s.addSource(source10, observer);
        s.addSource(source11, observer);
        s.addSource(source12, observer);
        s.addSource(source13, observer);
        s.addSource(source14, observer);
        s.addSource(source15, observer);
        return s;
    }


    protected static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R> LiveData<R> combineLatest(LiveData<T1> source1, LiveData<T2> source2, LiveData<T3> source3, LiveData<T4> source4, LiveData<T5> source5, LiveData<T6> source6, LiveData<T7> source7, LiveData<T8> source8, LiveData<T9> source9, LiveData<T10> source10, LiveData<T11> source11, LiveData<T12> source12, LiveData<T13> source13, LiveData<T14> source14, LiveData<T15> source15, LiveData<T16> source16, Function16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R> mapper) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(source1.getValue(), source2.getValue(), source3.getValue(), source4.getValue(), source5.getValue(), source6.getValue(), source7.getValue(), source8.getValue(), source9.getValue(), source10.getValue(), source11.getValue(), source12.getValue(), source13.getValue(), source14.getValue(), source15.getValue(), source16.getValue()));

        s.addSource(source1, observer);
        s.addSource(source2, observer);
        s.addSource(source3, observer);
        s.addSource(source4, observer);
        s.addSource(source5, observer);
        s.addSource(source6, observer);
        s.addSource(source7, observer);
        s.addSource(source8, observer);
        s.addSource(source9, observer);
        s.addSource(source10, observer);
        s.addSource(source11, observer);
        s.addSource(source12, observer);
        s.addSource(source13, observer);
        s.addSource(source14, observer);
        s.addSource(source15, observer);
        s.addSource(source16, observer);
        return s;
    }


    protected static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R> LiveData<R> combineLatest(LiveData<T1> source1, LiveData<T2> source2, LiveData<T3> source3, LiveData<T4> source4, LiveData<T5> source5, LiveData<T6> source6, LiveData<T7> source7, LiveData<T8> source8, LiveData<T9> source9, LiveData<T10> source10, LiveData<T11> source11, LiveData<T12> source12, LiveData<T13> source13, LiveData<T14> source14, LiveData<T15> source15, LiveData<T16> source16, LiveData<T17> source17, Function17<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R> mapper) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(source1.getValue(), source2.getValue(), source3.getValue(), source4.getValue(), source5.getValue(), source6.getValue(), source7.getValue(), source8.getValue(), source9.getValue(), source10.getValue(), source11.getValue(), source12.getValue(), source13.getValue(), source14.getValue(), source15.getValue(), source16.getValue(), source17.getValue()));

        s.addSource(source1, observer);
        s.addSource(source2, observer);
        s.addSource(source3, observer);
        s.addSource(source4, observer);
        s.addSource(source5, observer);
        s.addSource(source6, observer);
        s.addSource(source7, observer);
        s.addSource(source8, observer);
        s.addSource(source9, observer);
        s.addSource(source10, observer);
        s.addSource(source11, observer);
        s.addSource(source12, observer);
        s.addSource(source13, observer);
        s.addSource(source14, observer);
        s.addSource(source15, observer);
        s.addSource(source16, observer);
        s.addSource(source17, observer);
        return s;
    }


    protected static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R> LiveData<R> combineLatest(LiveData<T1> source1, LiveData<T2> source2, LiveData<T3> source3, LiveData<T4> source4, LiveData<T5> source5, LiveData<T6> source6, LiveData<T7> source7, LiveData<T8> source8, LiveData<T9> source9, LiveData<T10> source10, LiveData<T11> source11, LiveData<T12> source12, LiveData<T13> source13, LiveData<T14> source14, LiveData<T15> source15, LiveData<T16> source16, LiveData<T17> source17, LiveData<T18> source18, Function18<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R> mapper) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(source1.getValue(), source2.getValue(), source3.getValue(), source4.getValue(), source5.getValue(), source6.getValue(), source7.getValue(), source8.getValue(), source9.getValue(), source10.getValue(), source11.getValue(), source12.getValue(), source13.getValue(), source14.getValue(), source15.getValue(), source16.getValue(), source17.getValue(), source18.getValue()));

        s.addSource(source1, observer);
        s.addSource(source2, observer);
        s.addSource(source3, observer);
        s.addSource(source4, observer);
        s.addSource(source5, observer);
        s.addSource(source6, observer);
        s.addSource(source7, observer);
        s.addSource(source8, observer);
        s.addSource(source9, observer);
        s.addSource(source10, observer);
        s.addSource(source11, observer);
        s.addSource(source12, observer);
        s.addSource(source13, observer);
        s.addSource(source14, observer);
        s.addSource(source15, observer);
        s.addSource(source16, observer);
        s.addSource(source17, observer);
        s.addSource(source18, observer);
        return s;
    }


    protected static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R> LiveData<R> combineLatest(LiveData<T1> source1, LiveData<T2> source2, LiveData<T3> source3, LiveData<T4> source4, LiveData<T5> source5, LiveData<T6> source6, LiveData<T7> source7, LiveData<T8> source8, LiveData<T9> source9, LiveData<T10> source10, LiveData<T11> source11, LiveData<T12> source12, LiveData<T13> source13, LiveData<T14> source14, LiveData<T15> source15, LiveData<T16> source16, LiveData<T17> source17, LiveData<T18> source18, LiveData<T19> source19, Function19<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R> mapper) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(source1.getValue(), source2.getValue(), source3.getValue(), source4.getValue(), source5.getValue(), source6.getValue(), source7.getValue(), source8.getValue(), source9.getValue(), source10.getValue(), source11.getValue(), source12.getValue(), source13.getValue(), source14.getValue(), source15.getValue(), source16.getValue(), source17.getValue(), source18.getValue(), source19.getValue()));

        s.addSource(source1, observer);
        s.addSource(source2, observer);
        s.addSource(source3, observer);
        s.addSource(source4, observer);
        s.addSource(source5, observer);
        s.addSource(source6, observer);
        s.addSource(source7, observer);
        s.addSource(source8, observer);
        s.addSource(source9, observer);
        s.addSource(source10, observer);
        s.addSource(source11, observer);
        s.addSource(source12, observer);
        s.addSource(source13, observer);
        s.addSource(source14, observer);
        s.addSource(source15, observer);
        s.addSource(source16, observer);
        s.addSource(source17, observer);
        s.addSource(source18, observer);
        s.addSource(source19, observer);
        return s;
    }


    protected static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R> LiveData<R> combineLatest(LiveData<T1> source1, LiveData<T2> source2, LiveData<T3> source3, LiveData<T4> source4, LiveData<T5> source5, LiveData<T6> source6, LiveData<T7> source7, LiveData<T8> source8, LiveData<T9> source9, LiveData<T10> source10, LiveData<T11> source11, LiveData<T12> source12, LiveData<T13> source13, LiveData<T14> source14, LiveData<T15> source15, LiveData<T16> source16, LiveData<T17> source17, LiveData<T18> source18, LiveData<T19> source19, LiveData<T20> source20, Function20<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R> mapper) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(source1.getValue(), source2.getValue(), source3.getValue(), source4.getValue(), source5.getValue(), source6.getValue(), source7.getValue(), source8.getValue(), source9.getValue(), source10.getValue(), source11.getValue(), source12.getValue(), source13.getValue(), source14.getValue(), source15.getValue(), source16.getValue(), source17.getValue(), source18.getValue(), source19.getValue(), source20.getValue()));

        s.addSource(source1, observer);
        s.addSource(source2, observer);
        s.addSource(source3, observer);
        s.addSource(source4, observer);
        s.addSource(source5, observer);
        s.addSource(source6, observer);
        s.addSource(source7, observer);
        s.addSource(source8, observer);
        s.addSource(source9, observer);
        s.addSource(source10, observer);
        s.addSource(source11, observer);
        s.addSource(source12, observer);
        s.addSource(source13, observer);
        s.addSource(source14, observer);
        s.addSource(source15, observer);
        s.addSource(source16, observer);
        s.addSource(source17, observer);
        s.addSource(source18, observer);
        s.addSource(source19, observer);
        s.addSource(source20, observer);
        return s;
    }


    protected static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R> LiveData<R> combineLatest(LiveData<T1> source1, LiveData<T2> source2, LiveData<T3> source3, LiveData<T4> source4, LiveData<T5> source5, LiveData<T6> source6, LiveData<T7> source7, LiveData<T8> source8, LiveData<T9> source9, LiveData<T10> source10, LiveData<T11> source11, LiveData<T12> source12, LiveData<T13> source13, LiveData<T14> source14, LiveData<T15> source15, LiveData<T16> source16, LiveData<T17> source17, LiveData<T18> source18, LiveData<T19> source19, LiveData<T20> source20, LiveData<T21> source21, Function21<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R> mapper) {
        SupportMediatorLiveData<R> s = new SupportMediatorLiveData<>();
        Observer observer = o -> s.setValue(mapper.invoke(source1.getValue(), source2.getValue(), source3.getValue(), source4.getValue(), source5.getValue(), source6.getValue(), source7.getValue(), source8.getValue(), source9.getValue(), source10.getValue(), source11.getValue(), source12.getValue(), source13.getValue(), source14.getValue(), source15.getValue(), source16.getValue(), source17.getValue(), source18.getValue(), source19.getValue(), source20.getValue(), source21.getValue()));

        s.addSource(source1, observer);
        s.addSource(source2, observer);
        s.addSource(source3, observer);
        s.addSource(source4, observer);
        s.addSource(source5, observer);
        s.addSource(source6, observer);
        s.addSource(source7, observer);
        s.addSource(source8, observer);
        s.addSource(source9, observer);
        s.addSource(source10, observer);
        s.addSource(source11, observer);
        s.addSource(source12, observer);
        s.addSource(source13, observer);
        s.addSource(source14, observer);
        s.addSource(source15, observer);
        s.addSource(source16, observer);
        s.addSource(source17, observer);
        s.addSource(source18, observer);
        s.addSource(source19, observer);
        s.addSource(source20, observer);
        s.addSource(source21, observer);
        return s;
    }
}
