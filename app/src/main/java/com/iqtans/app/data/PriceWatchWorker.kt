package com.iqtans.app.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.iqtans.app.BuildConfig
import com.iqtans.app.domain.PriceTrust
import com.iqtans.app.domain.formatMoney
import java.util.concurrent.TimeUnit

class PriceWatchWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val baseUrl = BuildConfig.IQTANS_API_BASE_URL.trim()
        if (baseUrl.isBlank()) return Result.success()

        val storage = LocalPreferences(applicationContext)
        val watches = storage.loadWatches()
        if (watches.isEmpty()) return Result.success()

        val cards = storage.loadCards()
        val client = BackendSearchClient(baseUrl)
        val updated = watches.toMutableList()
        var anyProviderSucceeded = false

        watches.forEachIndexed { index, watch ->
            runCatching {
                val envelope = client.search(watch.toSearchRequest(), cards)
                val hotel = envelope.hotels.firstOrNull { it.id == watch.hotelId }
                    ?: envelope.hotels.firstOrNull { it.name.equals(watch.hotelName, ignoreCase = true) }
                    ?: return@runCatching
                val best = hotel.offers.filter { it.trust == PriceTrust.VERIFIED_LIVE }
                    .minByOrNull { it.finalAmount } ?: return@runCatching

                anyProviderSucceeded = true
                val price = best.finalAmount
                val previousBest = watch.bestSeenPrice.takeIf { it > 0.0 } ?: watch.lastSeenPrice
                val isNewLow = previousBest <= 0.0 || price < previousBest - 0.009
                val targetHit = watch.targetPrice?.let { target ->
                    price <= target + 0.009 && (watch.lastNotifiedPrice == null || price < watch.lastNotifiedPrice - 0.009)
                } == true
                val shouldNotify = isNewLow || targetHit

                updated[index] = watch.copy(
                    lastSeenPrice = price,
                    bestSeenPrice = if (previousBest > 0.0) minOf(previousBest, price) else price,
                    lastNotifiedPrice = if (shouldNotify) price else watch.lastNotifiedPrice,
                    lastCheckedAt = System.currentTimeMillis()
                )

                if (shouldNotify) notifyPriceDrop(watch.hotelName, watch.savedPrice, price, watch.currency, watch.id)
            }
        }

        storage.saveWatches(updated)
        return if (anyProviderSucceeded) Result.success() else Result.retry()
    }

    private fun notifyPriceDrop(hotelName: String, savedPrice: Double, currentPrice: Double, currency: String, watchId: String) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "مراقبة أسعار الفنادق", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "تنبيهات اقتنص عند العثور على سعر أقل لنفس الإقامة"
                }
            )
        }

        val launchIntent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                applicationContext,
                watchId.hashCode(),
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        val saved = (savedPrice - currentPrice).coerceAtLeast(0.0)
        val notification = Notification.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle("اقتنص وجد سعرًا أقل")
            .setContentText("$hotelName أصبح ${formatMoney(currentPrice)} $currency${if (saved > 0) " — وفر ${formatMoney(saved)}" else ""}")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        manager.notify(watchId.hashCode(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "price_watches"
    }
}

object PriceWatchScheduler {
    private const val UNIQUE_WORK = "iqtans-price-watch"

    fun schedule(context: Context) {
        if (BuildConfig.IQTANS_API_BASE_URL.isBlank()) return
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val request = PeriodicWorkRequestBuilder<PriceWatchWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
