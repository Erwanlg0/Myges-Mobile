package com.elg.studly.adapters.secondary.play

import android.app.Activity
import android.content.IntentSender
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidPlayQualityManager @Inject constructor() {
    fun checkForFlexibleUpdate(activity: Activity) {
        val manager = AppUpdateManagerFactory.create(activity)
        manager.appUpdateInfo
            .addOnSuccessListener { info ->
                if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    manager.completeUpdate()
                } else if (
                    info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) {
                    try {
                        manager.startUpdateFlowForResult(info, AppUpdateType.FLEXIBLE, activity, UPDATE_REQUEST_CODE)
                    } catch (e: IntentSender.SendIntentException) {
                    }
                }
            }
    }

    fun requestReview(activity: Activity) {
        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow()
            .addOnSuccessListener { reviewInfo -> manager.launchReviewFlow(activity, reviewInfo) }
    }

    companion object {
        private const val UPDATE_REQUEST_CODE = 4217
    }
}
