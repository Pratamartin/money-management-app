package com.pratatec.moneymgtapp.wear.sync

import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.pratatec.moneymgtapp.wear.data.local.WearPinStorage
import com.pratatec.moneymgtapp.wear.data.local.WearTokenStorage

class TokenListenerService : WearableListenerService() {

    override fun onDataChanged(events: DataEventBuffer) {
        val tokenStorage = WearTokenStorage(applicationContext)
        val pinStorage = WearPinStorage(applicationContext)
        for (event in events) {
            val item = event.dataItem
            when (item.uri.path) {
                "/auth_token" -> {
                    val map = DataMapItem.fromDataItem(item).dataMap
                    val access = map.getString("access") ?: continue
                    val refresh = map.getString("refresh") ?: continue
                    tokenStorage.save(access, refresh)
                }
                "/wear_pin" -> {
                    val map = DataMapItem.fromDataItem(item).dataMap
                    val hash = map.getString("pin_hash") ?: continue
                    pinStorage.savePin(hash)
                }
            }
        }
    }
}
