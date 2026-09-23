package com.limani.music.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {

    private const val TAG = "AdManager"

    // Official Google Test Ad Unit IDs
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // Placeholders for Production Ad Unit IDs (DO NOT USE APP ID HERE)
    // Replace with actual Banner / Interstitial Ad Unit IDs provided in format "ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY"
    val PRODUCTION_BANNER_AD_UNIT_ID: String? = null
    val PRODUCTION_INTERSTITIAL_AD_UNIT_ID: String? = null

    /**
     * Returns the active Banner Ad Unit ID.
     * Uses PRODUCTION_BANNER_AD_UNIT_ID if set, otherwise falls back to official Google TEST_BANNER_AD_UNIT_ID.
     */
    fun getBannerAdUnitId(): String {
        return PRODUCTION_BANNER_AD_UNIT_ID?.takeIf { it.isNotBlank() } ?: TEST_BANNER_AD_UNIT_ID
    }

    /**
     * Returns the active Interstitial Ad Unit ID.
     * Uses PRODUCTION_INTERSTITIAL_AD_UNIT_ID if set, otherwise falls back to official Google TEST_INTERSTITIAL_AD_UNIT_ID.
     */
    fun getInterstitialAdUnitId(): String {
        return PRODUCTION_INTERSTITIAL_AD_UNIT_ID?.takeIf { it.isNotBlank() } ?: TEST_INTERSTITIAL_AD_UNIT_ID
    }

    /**
     * Creates and loads a Banner AdView safely without throwing exceptions or crashing the application.
     */
    fun createAndLoadBannerAd(context: Context, adSize: AdSize = AdSize.BANNER): AdView {
        val adView = AdView(context)
        try {
            adView.setAdSize(adSize)
            adView.adUnitId = getBannerAdUnitId()
            adView.adListener = object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.w(TAG, "Banner ad failed to load: ${adError.message}")
                }

                override fun onAdLoaded() {
                    Log.d(TAG, "Banner ad loaded successfully")
                }
            }
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        } catch (e: Exception) {
            Log.e(TAG, "Exception while creating/loading banner ad", e)
        }
        return adView
    }

    /**
     * Loads an Interstitial Ad safely.
     */
    fun loadInterstitialAd(
        context: Context,
        onAdLoaded: (InterstitialAd) -> Unit,
        onAdFailedToLoad: ((LoadAdError) -> Unit)? = null
    ) {
        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                getInterstitialAdUnitId(),
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.d(TAG, "Interstitial ad loaded successfully")
                        onAdLoaded(interstitialAd)
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.w(TAG, "Interstitial ad failed to load: ${adError.message}")
                        onAdFailedToLoad?.invoke(adError)
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception while loading interstitial ad", e)
        }
    }

    /**
     * Shows an Interstitial Ad safely.
     */
    fun showInterstitialAd(activity: Activity, interstitialAd: InterstitialAd?) {
        try {
            if (interstitialAd != null) {
                interstitialAd.show(activity)
            } else {
                Log.d(TAG, "Interstitial ad is null, cannot show")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while showing interstitial ad", e)
        }
    }
}
