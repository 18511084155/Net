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
        Configuration.init {
            item {
                action = RAIL_WAY_TRAIN
                info = "火车票车次查询"
                url = "train/tickets/%s/queryByTrainNo?"
                method=RequestMethod.post
                params = arrayOf("key", "trainno")
            }
            item {
                action = WEATHER
                info = "天气查询"
                url = "v1/weather/query?"
                params = arrayOf("key", "city","province")
            }

        }
    }

}

