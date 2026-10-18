package com.example.myempty.floatingproject.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myempty.floatingproject.model.SlimeStats
import com.example.myempty.floatingproject.store.SlimeDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class HungerWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        try {
            val ctx = applicationContext
            val current = SlimeDataStore.statsFlow(ctx).first()
            val updated = current.copyWithHungerDelta(-1) // drop 1 point per run
            SlimeDataStore.saveStats(ctx, updated)
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}