package com.pratatec.moneymgtapp.sync

import android.content.Context
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

object WearTokenSync {
    suspend fun push(context: Context, access: String, refresh: String) {
        runCatching {
            val request = PutDataMapRequest.create("/auth_token").apply {
                dataMap.putString("access", access)
                dataMap.putString("refresh", refresh)
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()
            Wearable.getDataClient(context).putDataItem(request).await()
        }
    }

    suspend fun pushPin(context: Context, pinHash: String) {
        runCatching {
            val request = PutDataMapRequest.create("/wear_pin").apply {
                dataMap.putString("pin_hash", pinHash)
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()
            Wearable.getDataClient(context).putDataItem(request).await()
        }
    }
}
