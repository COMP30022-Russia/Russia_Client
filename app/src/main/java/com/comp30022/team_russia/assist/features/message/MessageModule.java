package com.comp30022.team_russia.assist.features.message;

import android.arch.lifecycle.ViewModel;

import com.comp30022.team_russia.assist.base.di.ViewModelKey;
import com.comp30022.team_russia.assist.features.message.db.MessageRepository;
import com.comp30022.team_russia.assist.features.message.db.MessageRepositoryImpl;
import com.comp30022.team_russia.assist.features.message.services.ChatService;
import com.comp30022.team_russia.assist.features.message.services.ChatServiceImpl;
import com.comp30022.team_russia.assist.features.message.ui.MessageListFragment;
import com.comp30022.team_russia.assist.features.message.vm.MessageListViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoMap;

import javax.inject.Singleton;

/**
 * (Dependency Injection) Top-level Dagger module for the Messaging-related
 * feature area.
 */
@SuppressWarnings("unused")
@Module()
public abstract class MessageModule {

    // Services
    @Singleton
    @Binds
    public abstract MessageRepository bindMessageRepository(MessageRepositoryImpl repo);

    @Singleton
    @Binds
    public abstract ChatService bindChatService(ChatServiceImpl chatService);

    // ViewModels
    @Binds
    @IntoMap
    @ViewModelKey(MessageListViewModel.class)
    abstract ViewModel bindMessageListViewModel(MessageListViewModel messageListViewModel);

    // Fragments
    @ContributesAndroidInjector
    public abstract MessageListFragment contributeMessageListFragment();
}
