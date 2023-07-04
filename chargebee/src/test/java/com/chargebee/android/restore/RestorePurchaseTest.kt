package com.chargebee.android.restore

import com.android.billingclient.api.*
import com.chargebee.android.Chargebee
import com.chargebee.android.ErrorDetail
import com.chargebee.android.billingservice.CBCallback.RestorePurchaseCallback
import com.chargebee.android.billingservice.OneTimeProductType
import com.chargebee.android.billingservice.ProductType
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.models.*
import com.chargebee.android.network.*
import com.chargebee.android.resources.ReceiptResource
import com.chargebee.android.resources.RestorePurchaseResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.CountDownLatch

@RunWith(MockitoJUnitRunner::class)
class RestorePurchaseTest {
    private var allTransactions = ArrayList<PurchaseTransaction>()
    private var restorePurchases = ArrayList<CBRestoreSubscription>()
    private var activeTransactions = ArrayList<PurchaseTransaction>()
    private var customer: CBCustomer? = null
    private val list = ArrayList<String>()
    private val storeTransactions = arrayListOf<PurchaseTransaction>()
    private val lock = CountDownLatch(1)
    private val response =
        CBReceiptResponse(ReceiptDetail("subscriptionId", "customerId", "planId"))
    private val error = CBException(
        ErrorDetail(
            message = "The Token data sent is not correct or Google service is temporarily down",
            httpStatusCode = 400
        )
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        customer = CBCustomer("test", "android", "test", "test@gmail.com")
        list.add("chargebee.pro.android")
        Chargebee.configure(
            site = "omni1-test.integrations.predev37.in",
            publishableApiKey = "test_rpKneFyplowONFtdHgnlpxh6ccdcQXNUcu",
            sdkKey = "cb-hmg6jlyvyrahvocyio57oqhoei",
            packageName = "com.chargebee.example"
        )
    }

    @After
    fun tearDown() {
        allTransactions.clear()
        restorePurchases.clear()
        activeTransactions.clear()
        customer = null
    }

    @Test
    fun test_fetchStoreSubscriptionStatus_success() {
        val lock = CountDownLatch(1)
        val purchaseTransaction = getTransaction(true)

        CBRestorePurchaseManager.fetchStoreSubscriptionStatus(
            purchaseTransaction,
            completionCallback = object : RestorePurchaseCallback {
                override fun onSuccess(result: List<CBRestoreSubscription>) {
                    lock.countDown()
                    result.forEach {
                        MatcherAssert.assertThat(
                            (it),
                            Matchers.instanceOf(CBRestoreSubscription::class.java)
                        )
                    }

                }

                override fun onError(error: CBException) {
                    lock.countDown()
                    MatcherAssert.assertThat(
                        error,
                        Matchers.instanceOf(CBException::class.java)
                    )
                }
            })
        lock.await()
    }

    @Test
    fun test_fetchStoreSubscriptionStatus_failure() {
        val purchaseTransaction = getTransaction(false)

        val storeTransaction =
            purchaseTransaction.firstOrNull()?.also { purchaseTransaction.remove(it) }
        storeTransaction?.purchaseToken?.let { purchaseToken ->
            CBRestorePurchaseManager.retrieveRestoreSubscription(purchaseToken, {}, { error ->
                lock.countDown()
                MatcherAssert.assertThat(
                    (error),
                    Matchers.instanceOf(CBException::class.java)
                )
                Mockito.verify(CBRestorePurchaseManager, Mockito.times(1))
                    .getRestorePurchases(purchaseTransaction)
            })
        }
        lock.await()
    }

    @Test
    fun test_retrieveStoreSubscription_success() {
        val purchaseTransaction = getTransaction(true)
        val cbRestorePurchasesList = arrayListOf<CBRestoreSubscription>()
        val purchaseToken = purchaseTransaction.first().purchaseToken
        val cbRestoreSubscription = CBRestoreSubscription("", "", StoreStatus.Active.value)
        cbRestorePurchasesList.add(cbRestoreSubscription)
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(RestorePurchaseResource().retrieveStoreSubscription(purchaseToken))
                .thenReturn(
                    ChargebeeResult.Success(
                        CBRestorePurchases(cbRestorePurchasesList)
                    )
                )
            Mockito.verify(RestorePurchaseResource(), Mockito.times(1))
                .retrieveStoreSubscription(purchaseToken)
        }
    }

    @Test
    fun test_retrieveStoreSubscription_failure() {
        val purchaseTransaction = getTransaction(false)
        val purchaseToken = purchaseTransaction.first().purchaseToken
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(RestorePurchaseResource().retrieveStoreSubscription(purchaseToken))
                .thenReturn(
                    ChargebeeResult.Error(
                        error
                    )
                )
            Mockito.verify(RestorePurchaseResource(), Mockito.times(1))
                .retrieveStoreSubscription(purchaseToken)
        }
    }

    @Test
    fun test_validateReceipt_success() {
        val purchaseTransaction = getTransaction(true)
        val params = Params(
            purchaseTransaction.first().purchaseToken,
            purchaseTransaction.first().productId.first(),
            customer,
            Chargebee.channel,
            null
        )
        CBRestorePurchaseManager.validateReceipt(
            params.receipt,
            purchaseTransaction.first().productType
        )
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(params.let { ReceiptResource().validateReceipt(it) }).thenReturn(
                ChargebeeResult.Success(
                    response
                )
            )
            Mockito.verify(ReceiptResource(), Mockito.times(1)).validateReceipt(params)
            Mockito.verify(CBReceiptRequestBody("receipt", "", null, "", null), Mockito.times(1))
                .toCBReceiptReqBody()
        }
    }

    @Test
    fun test_validateReceipt_failure() {
        val purchaseTransaction = getTransaction(false)
        val params = Params(
            purchaseTransaction.first().purchaseToken,
            purchaseTransaction.first().productId.first(),
            customer,
            Chargebee.channel,
            null
        )
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(params.let { ReceiptResource().validateReceipt(it) }).thenReturn(
                ChargebeeResult.Error(
                    error
                )
            )
            Mockito.verify(ReceiptResource(), Mockito.times(1)).validateReceipt(params)
            Mockito.verify(CBReceiptRequestBody("receipt", "", null, "", null), Mockito.times(1))
                .toCBReceiptReqBody()
        }
    }

    @Test
    fun test_syncPurchaseWithChargebee_success() {
        val purchaseTransaction = getTransaction(false)
        val params = Params(
            purchaseTransaction.first().purchaseToken,
            purchaseTransaction.first().productId.first(),
            customer,
            Chargebee.channel,
            null
        )
        CBRestorePurchaseManager.syncPurchaseWithChargebee(purchaseTransaction)
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(params.let { ReceiptResource().validateReceipt(it) }).thenReturn(
                ChargebeeResult.Success(
                    response
                )
            )
            Mockito.verify(ReceiptResource(), Mockito.times(1)).validateReceipt(params)
            Mockito.verify(CBReceiptRequestBody("receipt", "", null, "", null), Mockito.times(1))
                .toCBReceiptReqBody()
        }
    }

    @Test
    fun test_syncPurchaseWithChargebee_failure() {
        val purchaseTransaction = getTransaction(false)
        val params = Params(
            purchaseTransaction.first().purchaseToken,
            purchaseTransaction.first().productId.first(),
            customer,
            Chargebee.channel,
            null
        )
        CBRestorePurchaseManager.syncPurchaseWithChargebee(purchaseTransaction)
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(params.let { ReceiptResource().validateReceipt(it) }).thenReturn(
                ChargebeeResult.Error(
                    error
                )
            )
            Mockito.verify(ReceiptResource(), Mockito.times(1)).validateReceipt(params)
            Mockito.verify(CBReceiptRequestBody("receipt", "", null, "", null), Mockito.times(1))
                .toCBReceiptReqBody()
        }
    }

    private fun getTransaction(isTestingSuccess: Boolean): ArrayList<PurchaseTransaction> {
        storeTransactions.clear()
        val result = if (isTestingSuccess)
            PurchaseTransaction(
                productId = list.toList(),
                purchaseTime = 1682666112774,
                purchaseToken = "fajeooclbamgohgapjeehghm.AO-J1OzxVvoEx7y53c9DsypEKwgcfGw2OrisyQsQ-MG6KiXfJ97nT33Yd5VpbQYxd225QnTAEVdPuLP4YSvZE6LBhsv1rzSlizuBxBTjBWghWguSBBtgp2g",
                productType = "subs"
            )
        else
            PurchaseTransaction(
                productId = list.toList(),
                purchaseTime = 1682666112774,
                purchaseToken = "test data",
                productType = "subs"
            )
        storeTransactions.add(result)
        return storeTransactions
    }

    @Test
    fun test_validateNonSubscriptionReceipt_success() {
        val purchaseTransaction = getTransaction(true)
        val params = Params(
            purchaseTransaction.first().purchaseToken,
            purchaseTransaction.first().productId.first(),
            customer,
            Chargebee.channel,
            OneTimeProductType.CONSUMABLE
        )
        CBRestorePurchaseManager.validateNonSubscriptionReceipt(
            params.receipt,
            purchaseTransaction.first().productId.first()
        )
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(params.let { ReceiptResource().validateReceiptForNonSubscription(it) }).thenReturn(
                ChargebeeResult.Success(
                    response
                )
            )
            Mockito.verify(ReceiptResource(), Mockito.times(1)).validateReceiptForNonSubscription(params)
            Mockito.verify(CBReceiptRequestBody("receipt", "", null, "", OneTimeProductType.CONSUMABLE), Mockito.times(1))
                .toMapNonSubscription()
        }
    }

    @Test
    fun test_validateNonSubscriptionReceipt_failure() {
        val purchaseTransaction = getTransaction(false)
        val params = Params(
            purchaseTransaction.first().purchaseToken,
            purchaseTransaction.first().productId.first(),
            customer,
            Chargebee.channel,
            OneTimeProductType.CONSUMABLE
        )
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(params.let { ReceiptResource().validateReceiptForNonSubscription(it) }).thenReturn(
                ChargebeeResult.Error(
                    error
                )
            )
            Mockito.verify(ReceiptResource(), Mockito.times(1)).validateReceiptForNonSubscription(params)
            Mockito.verify(CBReceiptRequestBody("receipt", "", null, "", null), Mockito.times(1))
                .toMapNonSubscription()
        }
    }
}