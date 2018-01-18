package tiwence.fr.playersoundskotlin.module

import android.app.Application
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Tiwence on 09/01/2018.
 */

@Module
class AppModule {

    var mApplication: Application? = null

    constructor(application: Application) {
        mApplication = application
    }

    @Provides
    @Singleton
    fun providesApplication(): Application? {
        return mApplication
    }
}