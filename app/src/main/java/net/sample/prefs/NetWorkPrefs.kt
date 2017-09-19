package net.sample.prefs

import cz.netlibrary.model.Configuration
import cz.netlibrary.model.RequestMethod

/**
 * Created by cz on 2017/6/7.
 */
object NetWorkPrefs {
    val RAIL_WAY_TRAIN="rail_way_train"
    val WEATHER="weather"
    init {
        Configuration.register(OtherNetPrefs())
    }

}

