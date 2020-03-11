package com.adapty.api

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.util.Log
import com.adapty.Adapty.Companion.applicationContext
import com.adapty.api.entity.profile.AttributeProfileReq
import com.adapty.api.entity.profile.DataProfileReq
import com.adapty.api.entity.restore.RestoreItem
import com.adapty.api.entity.syncmeta.DataSyncMetaReq
import com.adapty.api.entity.validate.AttributeRestoreReceiptReq
import com.adapty.api.entity.validate.AttributeValidateReceiptReq
import com.adapty.api.entity.validate.DataRestoreReceiptReq
import com.adapty.api.entity.validate.DataValidateReceiptReq
import com.adapty.api.requests.*
import com.adapty.purchase.SUBS
import com.adapty.utils.PreferenceManager
import com.adapty.utils.UUIDTimeBased
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures.addCallback
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class ApiClientRepository(var preferenceManager: PreferenceManager) {

    private var apiClient = ApiClient(applicationContext)

    fun createProfile(customerUserId: String?, adaptyCallback: AdaptyCallback) {

        var uuid = preferenceManager.profileID
        if (uuid.isEmpty()) {
            uuid = UUIDTimeBased.generateId().toString()
            preferenceManager.profileID = uuid
            preferenceManager.installationMetaID = uuid
        }

        val profileRequest = CreateProfileRequest()
        profileRequest.data = DataProfileReq()
        profileRequest.data?.id = uuid
        profileRequest.data?.type = "adapty_analytics_profile"
        if (!customerUserId.isNullOrEmpty()) {
            profileRequest.data?.attributes = AttributeProfileReq()
            profileRequest.data?.attributes?.customerUserId = customerUserId
        }

        val task: AsyncTask<Void?, Void?, String?> =
            @SuppressLint("StaticFieldLeak")
            object : AsyncTask<Void?, Void?, String?>() {

                override fun doInBackground(vararg params: Void?): String? {
                    var idInfo: AdvertisingIdClient.Info? = null
                    var advertId: String? = null
                    try {
                        idInfo = AdvertisingIdClient.getAdvertisingIdInfo(applicationContext)
                        advertId = idInfo!!.id
                    } catch (e: Exception) { }

                    return advertId
                }

                override fun onPostExecute(advertId: String?) {
                    if (advertId != null) {
                        profileRequest.data?.attributes?.advertisingId = advertId
                    }
                    apiClient.createProfile(profileRequest, adaptyCallback)
                }
            }
        task.execute()
    }

    fun updateProfile(
        customerUserId: String?,
        email: String?,
        phoneNumber: String?,
        facebookUserId: String?,
        mixpanelUserId: String?,
        amplitudeUserId: String?,
        firstName: String?,
        lastName: String?,
        gender: String?,
        birthday: String?,
        adaptyCallback: AdaptyCallback
    ) {

        var uuid = preferenceManager.profileID
        if (uuid.isEmpty()) {
            uuid = UUIDTimeBased.generateId().toString()
            preferenceManager.profileID = uuid
        }

        val profileRequest = UpdateProfileRequest()
        profileRequest.data = DataProfileReq()
        profileRequest.data?.id = uuid
        profileRequest.data?.type = "adapty_analytics_profile"
        profileRequest.data?.attributes = AttributeProfileReq()
        profileRequest.data?.attributes?.apply {
            this.customerUserId = customerUserId
            this.email = email
            this.phoneNumber = phoneNumber
            this.facebookUserId = facebookUserId
            this.mixpanelUserId = mixpanelUserId
            this.amplitudeUserId = amplitudeUserId
            this.firstName = firstName
            this.lastName = lastName
            this.gender = gender
            this.birthday = birthday
        }

        val task: AsyncTask<Void?, Void?, String?> =
            @SuppressLint("StaticFieldLeak")
            object : AsyncTask<Void?, Void?, String?>() {

                override fun doInBackground(vararg params: Void?): String? {
                    var idInfo: AdvertisingIdClient.Info? = null
                    var advertId: String? = null
                    try {
                        idInfo = AdvertisingIdClient.getAdvertisingIdInfo(applicationContext)
                        advertId = idInfo!!.id
                    } catch (e: Exception) { }

                    return advertId
                }

                override fun onPostExecute(advertId: String?) {
                    if (advertId != null) {
                        profileRequest.data?.attributes?.advertisingId = advertId
                    }
                    apiClient.updateProfile(profileRequest, adaptyCallback)
                }
            }
        task.execute()
    }

    fun syncMetaInstall(adaptyCallback: AdaptyCallback? = null) {

        var uuid = preferenceManager.profileID
        if (uuid.isEmpty()) {
            uuid = UUIDTimeBased.generateId().toString()
            preferenceManager.profileID = uuid
            preferenceManager.installationMetaID = uuid
        }

        val syncMetaRequest = SyncMetaInstallRequest()
        syncMetaRequest.data = DataSyncMetaReq()
        syncMetaRequest.data?.id = uuid
        syncMetaRequest.data?.type = "adapty_analytics_profile_installation_meta"

        apiClient.syncMeta(syncMetaRequest, adaptyCallback)
    }

    fun validatePurchase(
        purchaseType: String,
        productId: String,
        purchaseToken: String,
        adaptyCallback: AdaptyCallback? = null
    ) {
        var uuid = preferenceManager.profileID
        if (uuid.isEmpty()) {
            uuid = UUIDTimeBased.generateId().toString()
            preferenceManager.profileID = uuid
            preferenceManager.installationMetaID = uuid
        }

        val validateReceiptRequest = ValidateReceiptRequest()
        validateReceiptRequest.data = DataValidateReceiptReq()
        validateReceiptRequest.data?.id = uuid
        validateReceiptRequest.data?.type = "google_receipt_validation_result"
        validateReceiptRequest.data?.attributes = AttributeValidateReceiptReq()
        validateReceiptRequest.data?.attributes?.productId = productId
        validateReceiptRequest.data?.attributes?.purchaseToken = purchaseToken
        validateReceiptRequest.data?.attributes?.profileId = uuid
        validateReceiptRequest.data?.attributes?.isSubscription = (purchaseType == SUBS)

        apiClient.validatePurchase(validateReceiptRequest, adaptyCallback)
    }

    fun restore(purchases: ArrayList<RestoreItem>, adaptyCallback: AdaptyCallback? = null) {
        var uuid = preferenceManager.profileID
        if (uuid.isEmpty()) {
            uuid = UUIDTimeBased.generateId().toString()
            preferenceManager.profileID = uuid
            preferenceManager.installationMetaID = uuid
        }

        val restoreReceiptRequest = RestoreReceiptRequest()
        restoreReceiptRequest.data = DataRestoreReceiptReq()
        restoreReceiptRequest.data?.type = "google_receipt_validation_result"
        restoreReceiptRequest.data?.attributes = AttributeRestoreReceiptReq()
        restoreReceiptRequest.data?.attributes?.profileId = uuid
        restoreReceiptRequest.data?.attributes?.restoreItems = purchases

        apiClient.restorePurchase(restoreReceiptRequest, adaptyCallback)
    }

    companion object Factory {

        private lateinit var instance: ApiClientRepository

        @Synchronized
        fun getInstance(preferenceManager: PreferenceManager): ApiClientRepository {
            if (!::instance.isInitialized)
                instance = ApiClientRepository(preferenceManager)

            return instance
        }
    }
}