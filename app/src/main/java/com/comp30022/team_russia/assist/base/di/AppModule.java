package com.comp30022.team_russia.assist.base.di;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.comp30022.team_russia.assist.ConfigurationManager;
import com.comp30022.team_russia.assist.base.db.RussiaDatabase;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Application level objects, e.g. the Application context, Room database.
 */
@Module
public class AppModule {

    @Provides
    @Singleton
    RussiaDatabase provideRoomDatabase(Application application) {
        return Room.databaseBuilder(
            application,
            RussiaDatabase.class,
            "russia.db").build();
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit() {
        // @todo: load URL from config
        return new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(ConfigurationManager.getInstance().getProperty("SERVER_URL"))
            .build();
    }
}
