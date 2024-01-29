package com.chargebee.android.billingservice

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.*
import com.chargebee.android.ErrorDetail
import com.chargebee.android.billingservice.BillingErrorCode.Companion.throwCBException
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.models.CBNonSubscriptionResponse
import com.chargebee.android.models.CBProduct
import com.chargebee.android.models.ChangeProductParams
import com.chargebee.android.models.PricingPhase
import com.chargebee.android.models.PurchaseProductParams
import com.chargebee.android.models.PurchaseTransaction
import com.chargebee.android.models.SubscriptionOffer
import com.chargebee.android.network.CBReceiptResponse
import com.chargebee.android.restore.CBRestorePurchaseManager

class BillingClientManager(context: Context) : PurchasesUpdatedListener {

    private val CONNECT_TIMER_START_MILLISECONDS = 1L * 1000L
    private var billingClient: BillingClient? = null
    var mContext: Context? = context
    private val handler = Handler(Looper.getMainLooper())
    private var purchaseCallBack: CBCallback.PurchaseCallback<String>? = null
    private val TAG = javaClass.simpleName
    private lateinit var purchaseProductParams: PurchaseProductParams
    private lateinit var changeProductParams: ChangeProductParams
    private lateinit var restorePurchaseCallBack: CBCallback.RestorePurchaseCallback
    private var oneTimePurchaseCallback: CBCallback.OneTimePurchaseCallback? = null

    init {
        this.mContext = context
    }

    internal fun retrieveProducts(
        products: ArrayList<String>, callBack: CBCallback.ListProductsCallback<ArrayList<CBProduct>>
    ) {
        val productsList = ArrayList<CBProduct>()
        retrieveProducts(BillingClient.ProductType.SUBS, products, { subsProductsList ->
            productsList.addAll(subsProductsList)
            retrieveProducts(BillingClient.ProductType.INAPP, products, { inAppProductsList ->
                productsList.addAll(inAppProductsList)
                callBack.onSuccess(productsList)
            }, { error ->
                callBack.onError(error)
            })
        }, { error ->
            callBack.onError(error)
        })
    }

    internal fun retrieveProducts(
        @BillingClient.ProductType productType: String,
        products: ArrayList<String>, response: (ArrayList<CBProduct>) -> Unit,
        errorDetail: (CBException) -> Unit
    ) {
        onConnected({ status ->
            if (status)
                loadProductDetails(productType, products, {
                    response(it)
                }, {
                    errorDetail(it)
                })
            else
                errorDetail(
                    connectionError
                )
        }, { error ->
            errorDetail(error)
        })
    }

    /* Get the SKU/Products from Play Console */
    private fun loadProductDetails(
        @BillingClient.ProductType productType: String,
        products: ArrayList<String>,
        response: (ArrayList<CBProduct>) -> Unit,
        errorDetail: (CBException) -> Unit
    ) {
        try {

            val queryProductDetails = products.map {
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it)
                    .setProductType(productType)
                    .build()
            }

            val productDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(queryProductDetails).build()

            billingClient?.queryProductDetailsAsync(
                productDetailsParams
            ) { billingResult, productsDetail ->
                if (billingResult.responseCode == OK && productsDetail != null) {
                    try {
                        val cbProductDetails = arrayListOf<CBProduct>()
                        for (productDetail in productsDetail) {
                            val cbProduct = convertToCbProduct(productDetail)
                            cbProductDetails.add(cbProduct)
                        }
                        Log.i(TAG, "Product details :$cbProductDetails")
                        response(cbProductDetails)
                    } catch (ex: CBException) {
                        errorDetail(
                            CBException(
                                ErrorDetail(
                                    message = "Error while parsing data",
                                    httpStatusCode = billingResult.responseCode
                                )
                            )
                        )
                        Log.e(TAG, "exception :" + ex.message)
                    }
                } else {
                    Log.e(TAG, "Response Code :" + billingResult.responseCode)
                    errorDetail(
                        throwCBException(billingResult)
                    )
                }
            }
        } catch (exp: CBException) {
            Log.e(TAG, "exception :$exp.message")
            errorDetail(CBException(ErrorDetail(message = "${exp.message}")))
        }
    }
    private fun convertToCbProduct(productDetail: ProductDetails): CBProduct {
        val subscriptionOffers = subscriptionOffers(productDetail.subscriptionOfferDetails)
        val oneTimePurchaseOffer = oneTimePurchaseOffer(productDetail.oneTimePurchaseOfferDetails)
        return CBProduct(
            productDetail.productId,
            productDetail.title,
            productDetail.description,
            ProductType.getProductType(productDetail.productType),
            subscriptionOffers,
            oneTimePurchaseOffer
        )
    }

    private fun oneTimePurchaseOffer(oneTimePurchaseOfferDetails: ProductDetails.OneTimePurchaseOfferDetails?): PricingPhase? {
        return oneTimePurchaseOfferDetails?.let {
            return PricingPhase(oneTimePurchaseOfferDetails.formattedPrice,
                oneTimePurchaseOfferDetails.priceAmountMicros,
                oneTimePurchaseOfferDetails.priceCurrencyCode)
        }
    }

    private fun subscriptionOffers(subscriptionOfferDetails: List<ProductDetails.SubscriptionOfferDetails>?): List<SubscriptionOffer>? {
        return subscriptionOfferDetails?.let { it.map { i -> subscriptionOffer(i) } }
    }

    private fun subscriptionOffer(subscriptionOfferDetail: ProductDetails.SubscriptionOfferDetails): SubscriptionOffer {
        val pricingPhases = pricingPhases(subscriptionOfferDetail.pricingPhases)
        return SubscriptionOffer(
            subscriptionOfferDetail.basePlanId,
            subscriptionOfferDetail.offerId,
            subscriptionOfferDetail.offerToken,
            pricingPhases
        )
    }

    private fun pricingPhases(pricingPhases: ProductDetails.PricingPhases): List<PricingPhase> {
        return pricingPhases.pricingPhaseList.map {
            PricingPhase(
                it.formattedPrice,
                it.priceAmountMicros,
                it.priceCurrencyCode,
                it.billingPeriod,
                it.billingCycleCount
            )
        }
    }

    internal fun purchase(
        purchaseProductParams: PurchaseProductParams,
        purchaseCallBack: CBCallback.PurchaseCallback<String>
    ) {
        this.purchaseCallBack = purchaseCallBack
        onConnected({ status ->
            if (status) {
                purchase(purchaseProductParams)
            } else
                purchaseCallBack.onError(
                    connectionError
                )
        }, { error ->
            purchaseCallBack.onError(error)
        })

    }

    /* Purchase the product: Initiates the billing flow for an In-app-purchase  */
    private fun purchase(purchaseProductParams: PurchaseProductParams) {
        this.purchaseProductParams = purchaseProductParams
        val offerToken = purchaseProductParams.offerToken

        val queryProductDetails = arrayListOf(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(purchaseProductParams.product.id)
                .setProductType(purchaseProductParams.product.type.value)
                .build())

        val productDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(queryProductDetails).build()

        billingClient?.queryProductDetailsAsync(
            productDetailsParams
        ) { billingResult, productsDetail ->
            if (billingResult.responseCode == OK && productsDetail != null) {
                val productDetailsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productsDetail.first())
                offerToken?.let { productDetailsBuilder.setOfferToken(it) }
                val productDetailsParamsList =
                    listOf(productDetailsBuilder.build())

                val billingFlowParams =
                    BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()

                billingClient?.launchBillingFlow(mContext as Activity, billingFlowParams)
                    .takeIf { billingResult ->
                        billingResult?.responseCode != OK
                    }?.let { billingResult ->
                        Log.e(TAG, "Failed to launch billing flow $billingResult")
                        val billingError = CBException(
                            ErrorDetail(
                                message = GPErrorCode.LaunchBillingFlowError.errorMsg,
                                httpStatusCode = billingResult.responseCode
                            )
                        )
                        purchaseCallBack?.onError(
                            billingError
                        )
                    }
            } else {
                Log.e(TAG, "Failed to fetch product :" + billingResult.responseCode)
                purchaseCallBack?.onError(throwCBException(billingResult))
            }
        }

    }

    internal fun changeProduct(
        changeProductParams: ChangeProductParams,
        purchaseCallBack: CBCallback.PurchaseCallback<String>
    ) {
        this.purchaseCallBack = purchaseCallBack
        onConnected({ status ->
            if (status) {
                changeProduct(changeProductParams)
            } else
                purchaseCallBack.onError(
                    connectionError
                )
        }, { error ->
            purchaseCallBack.onError(error)
        })

    }

    /* Update the product: Initiates the billing flow for an change subscription  */
    private fun changeProduct(changeProductParams: ChangeProductParams) {
        this.changeProductParams = changeProductParams
        this.purchaseProductParams = changeProductParams.purchaseProductParams
        val offerToken = changeProductParams.purchaseProductParams.offerToken
        val oldProductId = changeProductParams.oldProductId
        val prorationMode = BillingFlowParams.ProrationMode.IMMEDIATE_WITH_TIME_PRORATION

        val queryProductDetails = arrayListOf(QueryProductDetailsParams.Product.newBuilder()
            .setProductId(this.purchaseProductParams.product.id)
            .setProductType(this.purchaseProductParams.product.type.value)
            .build())

        val productDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(queryProductDetails).build()

        billingClient?.queryProductDetailsAsync(
            productDetailsParams
        ) { billingResult, productsDetail ->
            if (billingResult.responseCode == OK && productsDetail != null) {
                val productDetailsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productsDetail.first())
                offerToken?.let { productDetailsBuilder.setOfferToken(it) }
                val productDetailsParamsList =
                    listOf(productDetailsBuilder.build())

                queryAllSubsPurchaseHistory(ProductType.SUBS.value) { subscriptionHistory ->
                    val oldPurchaseToken: String = getOldPurchaseToken(subscriptionHistory, oldProductId)

                    val billingFlowParams =
                        BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .setSubscriptionUpdateParams(
                                BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                                    .setOldPurchaseToken(oldPurchaseToken)
                                    .setReplaceProrationMode(prorationMode).build()
                            ).build()

                    billingClient?.launchBillingFlow(mContext as Activity, billingFlowParams)
                        .takeIf { billingResult ->
                            billingResult?.responseCode != OK
                        }?.let { billingResult ->
                            Log.e(TAG, "Failed to launch billing flow $billingResult")
                            val billingError = CBException(
                                ErrorDetail(
                                    message = GPErrorCode.LaunchBillingFlowError.errorMsg,
                                    httpStatusCode = billingResult.responseCode
                                )
                            )
                            if (ProductType.SUBS == this.purchaseProductParams.product.type) {
                                purchaseCallBack?.onError(
                                    billingError
                                )
                            } else {
                                oneTimePurchaseCallback?.onError(
                                    billingError
                                )
                            }
                        }
                }
            } else {
                Log.e(TAG, "Failed to fetch product :" + billingResult.responseCode)
                if (ProductType.SUBS == this.purchaseProductParams.product.type) {
                    purchaseCallBack?.onError(throwCBException(billingResult))
                } else {
                    oneTimePurchaseCallback?.onError(throwCBException(billingResult))
                }

            }
        }

    }

    /**
     * This method will return old/previous purchase token.
     *
     * @param [subscriptionHistory] The list of previous subscriptions.
     * @param [oldProductId] The old/previous product id.
     */
    private fun getOldPurchaseToken(subscriptionHistory: List<PurchaseTransaction>?, oldProductId: String): String {
        val purchaseTransactionHistory = mutableListOf<PurchaseTransaction>()
        purchaseTransactionHistory.addAll(subscriptionHistory ?: emptyList())
        val prevProduct: PurchaseTransaction? = purchaseTransactionHistory.find { it.productId.first() == oldProductId }
        return prevProduct?.purchaseToken.orEmpty()
    }

    /**
     * This method will provide all the purchases associated with the current account based on the [includeInActivePurchases] flag set.
     * And the associated purchases will be synced with Chargebee.
     *
     * @param [completionCallback] The listener will be called when restore purchase completes.
     */
    internal fun restorePurchases(completionCallback: CBCallback.RestorePurchaseCallback) {
        this.restorePurchaseCallBack = completionCallback
        onConnected({ status ->
            queryPurchaseHistoryFromStore(status)
        }, { error ->
            completionCallback.onError(error)
        })
    }

    /* Checks if the specified feature is supported by the Play Store */
    fun isFeatureSupported(): Boolean {
        try {
            val featureSupportedResult =
                billingClient?.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
            when (featureSupportedResult?.responseCode) {
                OK -> {
                    return true
                }

                FEATURE_NOT_SUPPORTED -> {
                    return false
                }
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Play Services not available ")
        }
        return false
    }

    /* Checks if the billing client connected to the service */
    fun isBillingClientReady(): Boolean? {
        return billingClient?.isReady
    }

    /* Google Play calls this method to deliver the result of the Purchase Process/Operation */
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            OK -> {
                purchases?.forEach { purchase ->
                    when (purchase.purchaseState) {
                        Purchase.PurchaseState.PURCHASED -> {
                            acknowledgePurchase(purchase)
                        }

                        Purchase.PurchaseState.PENDING -> {
                            purchaseCallBack?.onError(
                                CBException(
                                    ErrorDetail(
                                        message = GPErrorCode.PurchasePending.errorMsg,
                                        httpStatusCode = billingResult.responseCode
                                    )
                                )
                            )
                        }

                        Purchase.PurchaseState.UNSPECIFIED_STATE -> {
                            purchaseCallBack?.onError(
                                CBException(
                                    ErrorDetail(
                                        message = GPErrorCode.PurchaseUnspecified.errorMsg,
                                        httpStatusCode = billingResult.responseCode
                                    )
                                )
                            )
                        }
                    }
                }
            }

            else -> {
                if (purchaseProductParams.product.type == ProductType.SUBS)
                    if (this.changeProductParams.oldProductId.isNotEmpty() && billingResult.responseCode == ERROR) {
                        purchaseCallBack?.onError(
                            CBException(
                                ErrorDetail(
                                    message = GPErrorCode.InvalidProductIdError.errorMsg,
                                    httpStatusCode = billingResult.responseCode
                                )
                            )
                        )
                    }
                    else {
                        purchaseCallBack?.onError(
                            throwCBException(billingResult)
                        )
                    }
                else
                    oneTimePurchaseCallback?.onError(
                        throwCBException(billingResult)
                    )
            }
        }
    }

    /* Acknowledge the Purchases */
    private fun acknowledgePurchase(purchase: Purchase) {
        when (purchaseProductParams.product.type) {
            ProductType.SUBS -> {
                isAcknowledgedPurchase(purchase, {
                    validateReceipt(purchase.purchaseToken, purchaseProductParams.product)
                }, {
                    purchaseCallBack?.onError(it)
                })
            }

            ProductType.INAPP -> {
                if (CBPurchase.productType == OneTimeProductType.CONSUMABLE) {
                    consumeAsyncPurchase(purchase.purchaseToken)
                } else {
                    isAcknowledgedPurchase(purchase, {
                        validateNonSubscriptionReceipt(purchase.purchaseToken, purchaseProductParams.product)
                    }, {
                        oneTimePurchaseCallback?.onError(it)
                    })
                }
            }
        }
    }

    private fun isAcknowledgedPurchase(
        purchase: Purchase,
        success: () -> Unit,
        error: (CBException) -> Unit
    ) {
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient?.acknowledgePurchase(params) { billingResult ->
                when (billingResult.responseCode) {
                    OK -> {
                        if (purchase.purchaseToken.isNotEmpty()) {
                            Log.i(TAG, "Google Purchase - success")
                            success()
                        } else {
                            Log.e(TAG, "Receipt Not Found")
                            error(
                                CBException(
                                    ErrorDetail(
                                        message = GPErrorCode.PurchaseReceiptNotFound.errorMsg,
                                        httpStatusCode = billingResult.responseCode
                                    )
                                )
                            )
                        }
                    }

                    else -> {
                        error(
                            throwCBException(billingResult)
                        )
                    }
                }
            }
        }
    }

    /* Consume the Purchases */
    private fun consumeAsyncPurchase(token: String) {
        consumePurchase(token) { billingResult, purchaseToken ->
            when (billingResult.responseCode) {
                OK -> {
                    validateNonSubscriptionReceipt(purchaseToken, purchaseProductParams.product)
                }

                else -> {
                    oneTimePurchaseCallback?.onError(
                        throwCBException(billingResult)
                    )
                }
            }
        }
    }

    internal fun consumePurchase(
        token: String,
        onConsumed: (billingResult: BillingResult, purchaseToken: String) -> Unit
    ) {
        onConnected({ status ->
            if (status)
                billingClient?.consumeAsync(
                    ConsumeParams.newBuilder().setPurchaseToken(token).build(), onConsumed
                )
            else
                oneTimePurchaseCallback?.onError(
                    connectionError
                )
        }, { error ->
            oneTimePurchaseCallback?.onError(error)
        })
    }

    /* Chargebee method called here to validate receipt */
    private fun validateReceipt(purchaseToken: String, product: CBProduct) {
        try {
            CBPurchase.validateReceipt(purchaseToken, product) {
                when (it) {
                    is ChargebeeResult.Success -> {
                        Log.i(
                            TAG,
                            "Validate Receipt Response:  ${(it.data as CBReceiptResponse).in_app_subscription}"
                        )
                        if (it.data.in_app_subscription != null) {
                            val subscriptionId = (it.data).in_app_subscription.subscription_id
                            Log.i(TAG, "Subscription ID:  $subscriptionId")
                            val subscriptionResult = (it.data).in_app_subscription
                            if (subscriptionId.isEmpty()) {
                                purchaseCallBack?.onSuccess(subscriptionResult, false)
                            } else {
                                purchaseCallBack?.onSuccess(subscriptionResult, true)
                            }
                        } else {
                            purchaseCallBack?.onError(CBException(ErrorDetail(message = GPErrorCode.PurchaseInvalid.errorMsg)))
                        }
                    }

                    is ChargebeeResult.Error -> {
                        Log.e(TAG, "Exception from Server - validateReceipt() :  ${it.exp.message}")
                        purchaseCallBack?.onError(it.exp)
                    }
                }
            }
        } catch (exp: Exception) {
            Log.e(TAG, "Exception from Server- validateReceipt() :  ${exp.message}")
            purchaseCallBack?.onError(CBException(ErrorDetail(message = exp.message)))
        }
    }

    private val connectionError = CBException(
        ErrorDetail(
            message = BillingErrorCode.SERVICE_UNAVAILABLE.message,
            httpStatusCode = BillingErrorCode.SERVICE_UNAVAILABLE.code
        )
    )

    private fun itemNotOwnedException(): CBException {
        return CBException(ErrorDetail(
            message = BillingErrorCode.ITEM_NOT_OWNED.message,
            httpStatusCode = BillingErrorCode.ITEM_NOT_OWNED.code
        ))
    }

    private fun queryPurchaseHistoryFromStore(
        connectionStatus: Boolean
    ) {
        if (connectionStatus) {
            queryAllSubsPurchaseHistory(ProductType.SUBS.value) { purchaseHistoryList ->
                val storeTransactions = arrayListOf<PurchaseTransaction>()
                storeTransactions.addAll(purchaseHistoryList ?: emptyList())
                CBRestorePurchaseManager.fetchStoreSubscriptionStatus(
                    storeTransactions = storeTransactions,
                    allTransactions = arrayListOf(),
                    activeTransactions = arrayListOf(),
                    restorePurchases = arrayListOf(),
                    completionCallback = restorePurchaseCallBack
                )
            }
        } else {
            restorePurchaseCallBack.onError(
                connectionError
            )
        }
    }

    private fun queryPurchaseHistory(
        storeTransactions: (List<PurchaseTransaction>) -> Unit
    ) {
        val purchaseTransactionHistory = mutableListOf<PurchaseTransaction>()
        queryAllSubsPurchaseHistory(ProductType.SUBS.value) { subscriptionHistory ->
            purchaseTransactionHistory.addAll(subscriptionHistory ?: emptyList())
            queryAllInAppPurchaseHistory(ProductType.INAPP.value) { inAppHistory ->
                purchaseTransactionHistory.addAll(inAppHistory ?: emptyList())
                storeTransactions(purchaseTransactionHistory)
            }
        }
    }

    private fun queryAllSubsPurchaseHistory(
        productType: String, purchaseTransactionList: (List<PurchaseTransaction>?) -> Unit
    ) {
        queryPurchaseHistoryAsync(productType) {
            purchaseTransactionList(it)
        }
    }

    private fun queryAllInAppPurchaseHistory(
        productType: String, purchaseTransactionList: (List<PurchaseTransaction>?) -> Unit
    ) {
        queryPurchaseHistoryAsync(productType) {
            purchaseTransactionList(it)
        }
    }

    private fun queryPurchaseHistoryAsync(
        productType: String, purchaseTransactionList: (List<PurchaseTransaction>?) -> Unit
    ) {
        val queryPurchaseHistoryParams = QueryPurchaseHistoryParams.newBuilder().setProductType(productType).build()
        billingClient?.queryPurchaseHistoryAsync(queryPurchaseHistoryParams) { billingResult, subsHistoryList ->
            if (billingResult.responseCode == OK) {
                val purchaseHistoryList = subsHistoryList?.map {
                    it.toPurchaseTransaction(productType)
                }
                purchaseTransactionList(purchaseHistoryList)
            } else {
                restorePurchaseCallBack.onError(throwCBException(billingResult))
            }
        }
    }

    private fun PurchaseHistoryRecord.toPurchaseTransaction(productType: String): PurchaseTransaction {
        return PurchaseTransaction(
            productId = this.skus,
            productType = productType,
            purchaseTime = this.purchaseTime,
            purchaseToken = this.purchaseToken
        )
    }

    private fun buildBillingClient(listener: PurchasesUpdatedListener): BillingClient? {
        if (billingClient == null) {
            billingClient = mContext?.let {
                BillingClient.newBuilder(it).enablePendingPurchases().setListener(listener)
                    .build()
            }
        }
        return billingClient
    }

    private fun onConnected(status: (Boolean) -> Unit, connectionError: (CBException) -> Unit) {
        val billingClient = buildBillingClient(this)
        if (billingClient?.isReady == false) {
            handler.postDelayed({
                billingClient.startConnection(
                    createBillingClientStateListener(status, connectionError)
                )
            }, CONNECT_TIMER_START_MILLISECONDS)
        } else status(true)
    }

    private fun createBillingClientStateListener(
        status: (Boolean) -> Unit,
        connectionError: (CBException) -> Unit
    ) = object :
        BillingClientStateListener {
        override fun onBillingServiceDisconnected() {
            Log.i(javaClass.simpleName, "onBillingServiceDisconnected")
            status(false)
        }

        override fun onBillingSetupFinished(billingResult: BillingResult) {
            when (billingResult.responseCode) {
                OK -> {
                    Log.i(TAG, "Google Billing Setup Done!")
                    status(true)
                }

                else -> {
                    connectionError(throwCBException(billingResult))
                }
            }
        }
    }

    internal fun validateReceiptWithChargebee(
        product: CBProduct,
        completionCallback: CBCallback.PurchaseCallback<String>
    ) {
        this.purchaseCallBack = completionCallback
        onConnected({ status ->
            if (status)
                queryPurchaseHistory { purchaseHistoryList ->
                    val purchaseTransaction = purchaseHistoryList.filter {
                        it.productId.first() == product.id
                    }
                    val transaction = purchaseTransaction.firstOrNull()
                    transaction?.let {
                        validateReceipt(transaction.purchaseToken, product)
                    } ?: run {
                        completionCallback.onError(itemNotOwnedException())
                    }

                } else
                completionCallback.onError(
                    connectionError
                )
        }, { error ->
            completionCallback.onError(error)
        })
    }

    internal fun purchaseNonSubscriptionProduct(
        product: CBProduct,
        oneTimePurchaseCallback: CBCallback.OneTimePurchaseCallback
    ) {
        this.oneTimePurchaseCallback = oneTimePurchaseCallback
        onConnected({ status ->
            if (status) {
                val purchaseParams = PurchaseProductParams(product)
                purchase(purchaseParams)
            } else {
                oneTimePurchaseCallback.onError(connectionError)
            }
        }, { error ->
            oneTimePurchaseCallback.onError(error)
        })
    }

    /* Chargebee method called here to validate receipt */
    private fun validateNonSubscriptionReceipt(purchaseToken: String, product: CBProduct) {
        CBPurchase.validateNonSubscriptionReceipt(purchaseToken, product) {
            when (it) {
                is ChargebeeResult.Success -> {
                    Log.i(
                        TAG,
                        "Validate Non-Subscription Receipt Response:  ${(it.data as CBNonSubscriptionResponse).nonSubscription}"
                    )
                    if (it.data.nonSubscription != null) {
                        val invoiceId = (it.data).nonSubscription.invoiceId
                        Log.i(TAG, "Invoice ID:  $invoiceId")
                        val nonSubscriptionResult = (it.data).nonSubscription
                        if (invoiceId.isEmpty()) {
                            oneTimePurchaseCallback?.onSuccess(nonSubscriptionResult, false)
                        } else {
                            oneTimePurchaseCallback?.onSuccess(nonSubscriptionResult, true)
                        }
                    } else {
                        oneTimePurchaseCallback?.onError(CBException(ErrorDetail(message = GPErrorCode.PurchaseInvalid.errorMsg)))
                    }
                }

                is ChargebeeResult.Error -> {
                    Log.e(
                        TAG,
                        "Exception from Server - validateNonSubscriptionReceipt() :  ${it.exp.message}"
                    )
                    oneTimePurchaseCallback?.onError(it.exp)
                }
            }
        }
    }

    internal fun validateNonSubscriptionReceiptWithChargebee(
        product: CBProduct,
        completionCallback: CBCallback.OneTimePurchaseCallback
    ) {
        this.oneTimePurchaseCallback = completionCallback
        onConnected({ status ->
            if (status)
                queryPurchaseHistory { purchaseHistoryList ->
                    val purchaseTransaction = purchaseHistoryList.filter {
                        it.productId.first() == product.id
                    }
                    val transaction = purchaseTransaction.firstOrNull()
                    transaction?.let {
                        validateNonSubscriptionReceipt(transaction.purchaseToken, product)
                    } ?: run {
                        completionCallback.onError(itemNotOwnedException())
                    }
                } else
                completionCallback.onError(
                    connectionError
                )
        }, { error ->
            completionCallback.onError(error)
        })
    }
}
