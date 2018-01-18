package tiwence.fr.playersoundskotlin.component

import dagger.Component
import tiwence.fr.playersoundskotlin.MainActivity
import tiwence.fr.playersoundskotlin.service.MusicService
import tiwence.fr.playersoundskotlin.module.AppModule
import tiwence.fr.playersoundskotlin.module.NetModule
import javax.inject.Singleton

/**
 * Created by Tiwence on 09/01/2018.
 */

@Singleton
@Component(modules = arrayOf(AppModule::class, NetModule::class))
interface NetComponent {
    fun inject(activity: MainActivity)
    fun inject(service: MusicService)
}
