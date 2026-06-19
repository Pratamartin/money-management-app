package com.pratatec.moneymgtapp.wear.sync

import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.pratatec.moneymgtapp.wear.data.local.WearTokenStorage

class TokenListenerService : WearableListenerService() {

    override fun onDataChanged(events: DataEventBuffer) {
        val storage = WearTokenStorage(applicationContext)
        for (event in events) {
            val item = event.dataItem
            if (item.uri.path == "/auth_token") {
                val map = DataMapItem.fromDataItem(item).dataMap
                val access = map.getString("access") ?: continue
                val refresh = map.getString("refresh") ?: continue
                storage.save(access, refresh)
            }
        }
    }
}
