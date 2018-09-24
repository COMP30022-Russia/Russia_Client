package com.comp30022.team_russia.assist.base.di;

import android.arch.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;

/**
 * Dagger module for ViewModelFactory.
 */
@Module
public abstract class ViewModelFactoryModule {
    @Binds
    public abstract ViewModelProvider.Factory
        bindViewModelFactory(DaggerViewModelFactory viewModelFactory);
}
