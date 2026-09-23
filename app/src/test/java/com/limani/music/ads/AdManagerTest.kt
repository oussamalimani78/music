package com.limani.music.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AdManagerTest {

    @Test
    fun getBannerAdUnitId_returnsTestIdWhenProductionNull() {
        val bannerId = AdManager.getBannerAdUnitId()
        assertEquals(AdManager.TEST_BANNER_AD_UNIT_ID, bannerId)
    }

    @Test
    fun getInterstitialAdUnitId_returnsTestIdWhenProductionNull() {
        val interstitialId = AdManager.getInterstitialAdUnitId()
        assertEquals(AdManager.TEST_INTERSTITIAL_AD_UNIT_ID, interstitialId)
    }

    @Test
    fun testAdUnitIds_areDistinctFromAppId() {
        val appId = "ca-app-pub-3806956841670869~5031258336"
        val bannerId = AdManager.getBannerAdUnitId()
        val interstitialId = AdManager.getInterstitialAdUnitId()

        assertNotNull(bannerId)
        assertNotNull(interstitialId)
        assert(bannerId.contains("/"))
        assert(interstitialId.contains("/"))
        assert(!bannerId.contains("~"))
        assert(!interstitialId.contains("~"))
        assert(bannerId != appId)
        assert(interstitialId != appId)
    }
}
