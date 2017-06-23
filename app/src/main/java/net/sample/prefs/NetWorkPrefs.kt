package net.sample.prefs

import cz.netlibrary.model.Configuration

/**
 * Created by cz on 2017/6/7.
 */
object NetWorkPrefs {
    val RAIL_WAY_TRAIN="rail_way_train"
    init {
        Configuration.init {
            item {
                action = RAIL_WAY_TRAIN
                info = "火车票车次查询"
                url = "train/tickets/queryByTrainNo?"
                params = arrayOf("key", "trainno")
            }
            item {

            }

        }
    }

}

