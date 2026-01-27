package com.parthipan.cheapeats.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Service for handling Google Play Billing "Thank the Developer" tips
 */
class BillingService(private val context: Context) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingService"

        // Product IDs - these must match what you configure in Play Console
        const val TIP_SMALL = "tip_small"      // $1.99
        const val TIP_MEDIUM = "tip_medium"    // $4.99
        const val TIP_LARGE = "tip_large"      // $9.99
    }

    private var billingClient: BillingClient? = null

    private val _connectionState = MutableStateFlow<BillingConnectionState>(BillingConnectionState.Disconnected)
    val connectionState: StateFlow<BillingConnectionState> = _connectionState.asStateFlow()

    private val _tipProducts = MutableStateFlow<List<TipProduct>>(emptyList())
    val tipProducts: StateFlow<List<TipProduct>> = _tipProducts.asStateFlow()

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()

        startConnection()
    }

    private fun startConnection() {
        _connectionState.value = BillingConnectionState.Connecting

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing client connected")
                    _connectionState.value = BillingConnectionState.Connected
                    queryTipProducts()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                    _connectionState.value = BillingConnectionState.Error(billingResult.debugMessage)
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
                _connectionState.value = BillingConnectionState.Disconnected
            }
        })
    }

    private fun queryTipProducts() {
        val productList = listOf(TIP_SMALL, TIP_MEDIUM, TIP_LARGE).map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val tips = productDetailsList.map { details ->
                    TipProduct(
                        productId = details.productId,
                        name = details.name,
                        description = details.description,
                        formattedPrice = details.oneTimePurchaseOfferDetails?.formattedPrice ?: "",
                        productDetails = details
                    )
                }.sortedBy {
                    when (it.productId) {
                        TIP_SMALL -> 0
                        TIP_MEDIUM -> 1
                        TIP_LARGE -> 2
                        else -> 3
                    }
                }
                _tipProducts.value = tips
                Log.d(TAG, "Loaded ${tips.size} tip products")
            } else {
                Log.e(TAG, "Failed to query products: ${billingResult.debugMessage}")
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, product: TipProduct) {
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(product.productDetails)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        _purchaseState.value = PurchaseState.Processing

        val result = billingClient?.launchBillingFlow(activity, billingFlowParams)
        if (result?.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Failed to launch billing flow: ${result?.debugMessage}")
            _purchaseState.value = PurchaseState.Error("Failed to start purchase")
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User canceled purchase")
                _purchaseState.value = PurchaseState.Cancelled
            }
            else -> {
                Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
                _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Acknowledge the purchase (required for consumables)
            if (!purchase.isAcknowledged) {
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient?.consumeAsync(consumeParams) { billingResult, _ ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Purchase consumed successfully")
                        _purchaseState.value = PurchaseState.Success
                    } else {
                        Log.e(TAG, "Failed to consume purchase: ${billingResult.debugMessage}")
                        _purchaseState.value = PurchaseState.Error("Failed to complete purchase")
                    }
                }
            }
        }
    }

    fun resetPurchaseState() {
        _purchaseState.value = PurchaseState.Idle
    }

    fun disconnect() {
        billingClient?.endConnection()
        billingClient = null
        _connectionState.value = BillingConnectionState.Disconnected
    }
}

data class TipProduct(
    val productId: String,
    val name: String,
    val description: String,
    val formattedPrice: String,
    val productDetails: ProductDetails
)

sealed class BillingConnectionState {
    object Disconnected : BillingConnectionState()
    object Connecting : BillingConnectionState()
    object Connected : BillingConnectionState()
    data class Error(val message: String) : BillingConnectionState()
}

sealed class PurchaseState {
    object Idle : PurchaseState()
    object Processing : PurchaseState()
    object Success : PurchaseState()
    object Cancelled : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}
