package com.comp30022.team_russia.assist.base.di;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.comp30022.team_russia.assist.ConfigurationManager;
import com.comp30022.team_russia.assist.base.AndroidLoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.ToastService;
import com.comp30022.team_russia.assist.base.ToastServiceImpl;
import com.comp30022.team_russia.assist.base.db.RussiaDatabase;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Application level objects, e.g. the Application context, Room database.
 */
@Module
public abstract class AppModule {

    @Binds
    @Singleton
    public abstract LoggerFactory bindLoggerFactory(AndroidLoggerFactory loggerFactory);

    @Binds
    @Singleton
    public abstract ToastService bindToastService(ToastServiceImpl toastService);


    @Provides
    @Singleton
    public static RussiaDatabase provideRoomDatabase(Application application) {
        return Room.databaseBuilder(
            application,
            RussiaDatabase.class,
            "russia.db").build();
    }

    @Provides
    @Singleton
    public static Retrofit provideRetrofit() {
        return new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(ConfigurationManager.getInstance().getProperty("SERVER_URL"))
            .build();
    }
}
