package net.sample.prefs

import cz.netlibrary.model.NetPrefsItem
import cz.netlibrary.model.RequestMethod

/**
 * Created by cz on 2017/9/19.
 */
class OtherNetPrefs:NetPrefsItem(){
    init {
        item {
            action = NetWorkPrefs.RAIL_WAY_TRAIN
            info = "火车票车次查询"
            url = "train/tickets/%s/queryByTrainNo?"
            method= RequestMethod.post
            params = arrayOf("key", "trainno")
        }
        item {
            action = NetWorkPrefs.WEATHER
            info = "天气查询"
            url = "v1/weather/query?"
            params = arrayOf("key", "city","province")
        }
    }
}