package tiwence.fr.playersoundskotlin

import android.app.Application
import tiwence.fr.playersoundskotlin.component.DaggerNetComponent
import tiwence.fr.playersoundskotlin.component.NetComponent
import tiwence.fr.playersoundskotlin.module.AppModule
import tiwence.fr.playersoundskotlin.module.NetModule
import tiwence.fr.playersoundskotlin.util.Constants

/**
 * Created by Tiwence on 09/01/2018.
 */

class MyApplication : Application() {

    private var mNetComponent : NetComponent? = null;

    override fun onCreate() {
        super.onCreate()

        mNetComponent = DaggerNetComponent.builder()
                .appModule(AppModule(this))
                .netModule(NetModule(Constants.apiBaseUrl))
                .build()
    }

    fun getNetComponent() : NetComponent? { return  mNetComponent }
}