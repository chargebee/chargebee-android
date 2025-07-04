package com.chargebee.android.billingservice

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.android.billingclient.api.*
import com.chargebee.android.Chargebee
import com.chargebee.android.ErrorDetail
import com.chargebee.android.billingservice.CBCallback.ListProductsCallback
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.CBProductIDResult
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.fixtures.otpProducts
import com.chargebee.android.fixtures.subProducts
import com.chargebee.android.models.CBNonSubscriptionResponse
import com.chargebee.android.models.CBProduct
import com.chargebee.android.models.NonSubscription
import com.chargebee.android.models.PurchaseProductParams
import com.chargebee.android.network.*
import com.chargebee.android.resources.CatalogVersion
import com.chargebee.android.resources.ReceiptResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.instanceOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch
import kotlin.collections.ArrayList

@RunWith(MockitoJUnitRunner::class)
@Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
class BillingClientManagerTest {

    private var billingClientManager: BillingClientManager? = null

    @Mock
    lateinit var billingClient: BillingClient

    private var mContext: Context? = null
    private var callBack: ListProductsCallback<ArrayList<CBProduct>>? = null
    private var callBackPurchase: CBCallback.PurchaseCallback<String>? = null
    private val productIdList = arrayListOf("merchant.pro.android", "merchant.premium.android")
    private var customer = CBCustomer("test", "android", "test", "test@gmail.com")
    private var customerId: String = "test"
    private val purchaseToken = "56sadmnagdjsd"
    private val params = Params(
        "purchaseToken",
        "product.productId",
        customer,
        Chargebee.channel,
        null
    )
    private val receiptDetail = ReceiptDetail("subscriptionId", "customerId", "planId")
    private var callBackOneTimePurchase: CBCallback.OneTimePurchaseCallback? = null
    private val nonSubscriptionDetail = NonSubscription("invoiceId", "customerId", "chargeId")
    private val productDetails = ProductDetails::class.create()


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        Chargebee.configure(
            site = "site-name",
            publishableApiKey = "test_EojsGoGFeHoc3VpGPQDOZGAxYy3d0FF3",
            sdkKey = "cb-wpkheixkuzgxbnt23rzslg724y"
        )

        billingClientManager = callBack?.let {
            BillingClientManager(
                ApplicationProvider.getApplicationContext()
            )
        }
    }
    @After
    fun tearDown(){
        mContext = null
        billingClientManager = null
        callBack = null
        callBackPurchase = null
        billingClient.endConnection()
    }

    @Test
    fun test_retrieveProducts_success(){
        val productIdList = arrayListOf("merchant.pro.android", "merchant.premium.android")

        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(mContext?.let {
                CBPurchase.retrieveProducts(
                    it,
                    productIdList,
                    object : ListProductsCallback<ArrayList<CBProduct>> {
                        override fun onSuccess(productDetails: ArrayList<CBProduct>) {
                            println("List products :$productDetails")
                            assertThat(
                                productDetails,
                                instanceOf(CBProduct::class.java)
                            )
                        }

                        override fun onError(error: CBException) {
                            println("Error in retrieving all items :${error.message}")
                        }
                    })
            }).thenReturn(Unit)

            Mockito.`when`(billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS))
            verify(billingClient, times(1))
                ?.queryPurchasesAsync(BillingClient.SkuType.SUBS)
        }
    }

    @Test
    fun test_retrieveProducts_error(){
        val productIdList = arrayListOf("")
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(mContext?.let {
                CBPurchase.retrieveProducts(
                    it,
                    productIdList,
                    object : ListProductsCallback<ArrayList<CBProduct>> {
                        override fun onSuccess(productDetails: ArrayList<CBProduct>) {
                            println("List products :$productDetails")
                            assertThat(
                                productDetails,
                                instanceOf(CBProduct::class.java)
                            )
                        }

                        override fun onError(error: CBException) {
                            println("Error in retrieving all items :${error.message}")
                        }
                    })
            }).thenReturn(Unit)
        }
    }

    @Test
    fun test_retrieveProductIds_success(){
        val queryParam = arrayOf("100")

        val IDs =  java.util.ArrayList<String>()
        IDs.add("")
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(CBPurchase.retrieveProductIdentifiers(queryParam) {
                when (it) {
                    is CBProductIDResult.ProductIds -> {
                        assertThat(it,instanceOf(CBProductIDResult::class.java))
                    }
                    is CBProductIDResult.Error -> {
                        println(" Error ${it.exp.message}")
                    }
                }
            }).thenReturn(Unit)

        }
    }
    @Test
    fun test_retrieveProductIdsList_success(){
        val queryParam = arrayOf("100")

        Chargebee.version = CatalogVersion.V2.value
        val productsIds =  java.util.ArrayList<String>()
        productsIds.add("")
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(CBPurchase.retrieveProductIDList(queryParam) {
                when (it) {
                    is CBProductIDResult.ProductIds -> {
                        assertThat(it,instanceOf(CBProductIDResult::class.java))
                    }
                    is CBProductIDResult.Error -> {
                        println(" Error ${it.exp.message}")
                    }
                }
            }).thenReturn(Unit)

        }
    }
    @Test
    fun test_retrieveProductIdsListV1_success(){
        val queryParam = arrayOf("100")

        Chargebee.version = CatalogVersion.V1.value
        val IDs =  java.util.ArrayList<String>()
        IDs.add("")
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(CBPurchase.retrieveProductIDList(queryParam) {
                when (it) {
                    is CBProductIDResult.ProductIds -> {
                        assertThat(it,instanceOf(CBProductIDResult::class.java))
                    }
                    is CBProductIDResult.Error -> {
                        println(" Error ${it.exp.message}")
                    }
                }
            }).thenReturn(Unit)

        }
    }
    @Test
    fun test_retrieveProductIdsListUnknown_success(){
        val queryParam = arrayOf("100")

        Chargebee.version = CatalogVersion.Unknown.value
        val IDs =  java.util.ArrayList<String>()
        IDs.add("")
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(CBPurchase.retrieveProductIDList(queryParam) {
                when (it) {
                    is CBProductIDResult.ProductIds -> {
                        assertThat(it,instanceOf(CBProductIDResult::class.java))
                    }
                    is CBProductIDResult.Error -> {
                        println(" Error ${it.exp.message}")
                    }
                }
            }).thenReturn(Unit)

        }
    }

    @Test
    fun test_retrieveProductIds_error(){
        val queryParam = arrayOf("0")

        val productsIds =  java.util.ArrayList<String>()
        productsIds.add("")
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(CBPurchase.retrieveProductIdentifiers(queryParam) {
                when (it) {
                    is CBProductIDResult.ProductIds -> {
                        assertThat(it,instanceOf(CBProductIDResult::class.java))
                    }
                    is CBProductIDResult.Error -> {
                        println(" Error ${it.exp.message} response code: ${it.exp.httpStatusCode}")
                    }
                }
            }).thenReturn(Unit)

        }
    }
    @Test
    fun test_purchaseProduct_success(){
         val jsonDetails = "{\"productId\":\"merchant.premium.android\",\"type\":\"subs\",\"title\":\"Premium Plan (Chargebee Example)\",\"name\":\"Premium Plan\",\"price\":\"₹2,650.00\",\"price_amount_micros\":2650000000,\"price_currency_code\":\"INR\",\"description\":\"Every 6 Months\",\"subscriptionPeriod\":\"P6M\",\"skuDetailsToken\":\"AEuhp4J0KiD1Bsj3Yq2mHPBRNHUBdzs4nTJY3PWRR8neE-22MJNssuDzH2VLFKv35Ov8\"}"

        val skuDetails = SkuDetails(jsonDetails)

        val lock = CountDownLatch(1)
        CoroutineScope(Dispatchers.IO).launch {
            val purchaseProductParams = PurchaseProductParams(subProducts)
            CBPurchase.purchaseProduct(
                purchaseProductParams,null,
                object : CBCallback.PurchaseCallback<String> {
                    override fun onError(error: CBException) {
                        lock.countDown()
                        println(" Error :  ${error.message} response code: ${error.httpStatusCode}")
                    }

                    override fun onSuccess(result: ReceiptDetail, status: Boolean) {
                        lock.countDown()
                        assertThat(result,instanceOf(ReceiptDetail::class.java))
                    }
                })

            Mockito.`when`(callBackPurchase?.let {
                billingClientManager?.purchase(purchaseProductParams, it)
            }).thenReturn(Unit)
            callBackPurchase?.let {
                verify(billingClientManager, times(1))?.purchase(purchaseProductParams,purchaseCallBack = it)
            }
        }
        lock.await()
    }
    @Test
    fun test_purchaseProduct_error(){
        val jsonDetails = "{\"productId\":\"merchant.premium.android\",\"type\":\"subs\",\"title\":\"Premium Plan (Chargebee Example)\",\"name\":\"Premium Plan\",\"price\":\"₹2,650.00\",\"price_amount_micros\":2650000000,\"price_currency_code\":\"INR\",\"description\":\"Every 6 Months\",\"subscriptionPeriod\":\"P6M\",\"skuDetailsToken\":\"AEuhp4J0KiD1Bsj3Yq2mHPBRNHUBdzs4nTJY3PWRR8neE-22MJNssuDzH2VLFKv35Ov8\"}"

        val skuDetails = SkuDetails(jsonDetails)
        CoroutineScope(Dispatchers.IO).launch {
            val purchaseProductParams = PurchaseProductParams(subProducts)
            CBPurchase.purchaseProduct(
                purchaseProductParams,null,
                object : CBCallback.PurchaseCallback<String> {

                    override fun onSuccess(result: ReceiptDetail, status: Boolean) {
                        assertThat(result, instanceOf(ReceiptDetail::class.java))
                    }

                    override fun onError(error: CBException) {
                        println(" Error :  ${error.message} response code: ${error.httpStatusCode}")
                    }
                })
        }
    }
    @Test
    fun test_validateReceipt_success(){
        val purchaseToken = "56sadmnagdjsd"
        val lock = CountDownLatch(1)
        CoroutineScope(Dispatchers.IO).launch {
            CBPurchase.validateReceipt(purchaseToken, "productId") {
                when (it) {
                    is ChargebeeResult.Success -> {
                        lock.countDown()
                        assertThat(it, instanceOf(CBReceiptResponse::class.java))
                    }
                    is ChargebeeResult.Error -> {
                        lock.countDown()
                        println(" Error :  ${it.exp.message}")
                    }
                }
            }
        }
        lock.await()
        val response = CBReceiptResponse(receiptDetail)
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(params.let { ReceiptResource().validateReceipt(it) }).thenReturn(
                ChargebeeResult.Success(
                    response
                )
            )
            verify(ReceiptResource(), times(1)).validateReceipt(params)
            verify(CBReceiptRequestBody("receipt","",null,"", null), times(1)).toCBReceiptReqBody()
        }
    }
    @Test
    fun test_validateReceipt_error(){
        val purchaseToken = "56sadmnagdjsd"
        CoroutineScope(Dispatchers.IO).launch {
            CBPurchase.validateReceipt(purchaseToken, "products") {
                when (it) {
                    is ChargebeeResult.Success -> {
                        assertThat(it, instanceOf(CBReceiptResponse::class.java))
                    }
                    is ChargebeeResult.Error -> {
                        println(" Error :  ${it.exp.message} response code: ${it.exp.httpStatusCode}")
                    }
                }
            }
        }
        val exception = CBException(ErrorDetail("Error"))
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(params.let { ReceiptResource().validateReceipt(it) }).thenReturn(
                ChargebeeResult.Error(
                    exception
                )
            )
            verify(ReceiptResource(), times(1)).validateReceipt(params)
            verify(CBReceiptRequestBody("receipt","",null,"", null), times(1)).toCBReceiptReqBody()
        }
    }

    @Test
    fun test_purchaseProductWithEmptyCBCustomer_success(){
        val customer = CBCustomer("","","","")
        val jsonDetails = "{\"productId\":\"merchant.premium.android\",\"type\":\"subs\",\"title\":\"Premium Plan (Chargebee Example)\",\"name\":\"Premium Plan\",\"price\":\"₹2,650.00\",\"price_amount_micros\":2650000000,\"price_currency_code\":\"INR\",\"description\":\"Every 6 Months\",\"subscriptionPeriod\":\"P6M\",\"skuDetailsToken\":\"AEuhp4J0KiD1Bsj3Yq2mHPBRNHUBdzs4nTJY3PWRR8neE-22MJNssuDzH2VLFKv35Ov8\"}"

        val skuDetails = SkuDetails(jsonDetails)
        val lock = CountDownLatch(1)
        CoroutineScope(Dispatchers.IO).launch {
            val purchaseProductParams = PurchaseProductParams(subProducts)
            CBPurchase.purchaseProduct(
                purchaseProductParams,customer,
                object : CBCallback.PurchaseCallback<String> {
                    override fun onError(error: CBException) {
                        lock.countDown()
                        println(" Error :  ${error.message} response code: ${error.httpStatusCode}")
                    }

                    override fun onSuccess(result: ReceiptDetail, status: Boolean) {
                        lock.countDown()
                        assertThat(result,instanceOf(ReceiptDetail::class.java))
                    }
                })

            Mockito.`when`(callBackPurchase?.let {
                billingClientManager?.purchase(purchaseProductParams, it)
            }).thenReturn(Unit)
            callBackPurchase?.let {
                verify(billingClientManager, times(1))?.purchase(purchaseProductParams,purchaseCallBack = it)
            }
        }
        lock.await()
    }

    @Test
    fun test_purchaseProductWithCBCustomer_success(){
        val jsonDetails = "{\"productId\":\"merchant.premium.android\",\"type\":\"subs\",\"title\":\"Premium Plan (Chargebee Example)\",\"name\":\"Premium Plan\",\"price\":\"₹2,650.00\",\"price_amount_micros\":2650000000,\"price_currency_code\":\"INR\",\"description\":\"Every 6 Months\",\"subscriptionPeriod\":\"P6M\",\"skuDetailsToken\":\"AEuhp4J0KiD1Bsj3Yq2mHPBRNHUBdzs4nTJY3PWRR8neE-22MJNssuDzH2VLFKv35Ov8\"}"

        val skuDetails = SkuDetails(jsonDetails)
        val lock = CountDownLatch(1)
        CoroutineScope(Dispatchers.IO).launch {
            val purchaseProductParams = PurchaseProductParams(subProducts)
            CBPurchase.purchaseProduct(
                purchaseProductParams,customer,
                object : CBCallback.PurchaseCallback<String> {
                    override fun onError(error: CBException) {
                        lock.countDown()
                        println(" Error :  ${error.message} response code: ${error.httpStatusCode}")
                    }

                    override fun onSuccess(result: ReceiptDetail, status: Boolean) {
                        lock.countDown()
                        assertThat(result,instanceOf(ReceiptDetail::class.java))
                    }
                })

            Mockito.`when`(callBackPurchase?.let {
                billingClientManager?.purchase(purchaseProductParams, it)
            }).thenReturn(Unit)
            callBackPurchase?.let {
                verify(billingClientManager, times(1))?.purchase(purchaseProductParams,purchaseCallBack = it)
            }
        }
        lock.await()
    }
    @Test
    fun test_purchaseProductWithCBCustomer_error(){
        val jsonDetails = "{\"productId\":\"merchant.premium.android\",\"type\":\"subs\",\"title\":\"Premium Plan (Chargebee Example)\",\"name\":\"Premium Plan\",\"price\":\"₹2,650.00\",\"price_amount_micros\":2650000000,\"price_currency_code\":\"INR\",\"description\":\"Every 6 Months\",\"subscriptionPeriod\":\"P6M\",\"skuDetailsToken\":\"AEuhp4J0KiD1Bsj3Yq2mHPBRNHUBdzs4nTJY3PWRR8neE-22MJNssuDzH2VLFKv35Ov8\"}"

        val skuDetails = SkuDetails(jsonDetails)
        CoroutineScope(Dispatchers.IO).launch {
            val purchaseProductParams = PurchaseProductParams(subProducts)
            CBPurchase.purchaseProduct(
                purchaseProductParams,customer,
                object : CBCallback.PurchaseCallback<String> {

                    override fun onSuccess(result: ReceiptDetail, status: Boolean) {
                        assertThat(result, instanceOf(ReceiptDetail::class.java))
                    }

                    override fun onError(error: CBException) {
                        println(" Error :  ${error.message} response code: ${error.httpStatusCode}")
                    }
                })
        }
    }

    @Test
    fun test_validateReceiptWithChargebee_success() {
        val response = CBReceiptResponse(receiptDetail)
        CoroutineScope(Dispatchers.IO).launch {
            CBPurchase.validateReceipt(
                purchaseToken = "purchaseToken",
                productId = "productId"
            ) {
                when (it) {
                    is ChargebeeResult.Success -> {
                        assertThat(it, instanceOf(ReceiptDetail::class.java))
                    }
                    is ChargebeeResult.Error -> {
                        println(" Error :  ${it.exp}")
                    }
                }
            }
        }
            CoroutineScope(Dispatchers.IO).launch {
                Mockito.`when`(params.let { ReceiptResource().validateReceipt(it) }).thenReturn(
                    ChargebeeResult.Success(
                        response
                    )
                )
                verify(ReceiptResource(), times(1)).validateReceipt(params)
                verify(CBReceiptRequestBody("receipt", "", null, "", null), times(1)).toCBReceiptReqBody()

            }
        }

    @Test
    fun test_validateReceiptWithChargebee_error() {
        val exception = CBException(ErrorDetail("Error"))
        CoroutineScope(Dispatchers.IO).launch {
            CBPurchase.validateReceipt(
                purchaseToken = "purchaseToken",
                productId = "productId"
            ) {
                when (it) {
                    is ChargebeeResult.Success -> {
                        assertThat(it, instanceOf(ReceiptDetail::class.java))
                    }
                    is ChargebeeResult.Error -> {
                        println(" Error :  ${it.exp}")
                    }
                }
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(params.let { ReceiptResource().validateReceipt(it) }).thenReturn(
                ChargebeeResult.Error(
                    exception
                )
            )
            verify(ReceiptResource(), times(1)).validateReceipt(params)
            verify(CBReceiptRequestBody("receipt", "", null, "", null), times(1)).toCBReceiptReqBody()
        }
    }

    @Test
    fun test_purchaseNonSubscriptionProduct_success(){
        val lock = CountDownLatch(1)
        CoroutineScope(Dispatchers.IO).launch {
            CBPurchase.purchaseNonSubscriptionProduct(
                product = otpProducts,
                productType = OneTimeProductType.CONSUMABLE,
                callback = object : CBCallback.OneTimePurchaseCallback {
                    override fun onError(error: CBException) {
                        lock.countDown()
                        assertThat(error, instanceOf(CBException::class.java))
                        println(" Error :  ${error.message} response code: ${error.httpStatusCode}")
                    }

                    override fun onSuccess(result: NonSubscription, status: Boolean) {
                        lock.countDown()
                        assertThat(result, instanceOf(NonSubscription::class.java))
                    }
                })

            Mockito.`when`(callBackOneTimePurchase?.let {
                billingClientManager?.purchaseNonSubscriptionProduct(otpProducts, it)
            }).thenReturn(Unit)
            callBackOneTimePurchase?.let {
                verify(billingClientManager, times(1))?.purchaseNonSubscriptionProduct(
                    otpProducts,
                    oneTimePurchaseCallback = it
                )
            }
        }
        lock.await()
    }

    @Test
    fun test_purchaseNonSubscriptionProduct_error(){
        val lock = CountDownLatch(1)
        CoroutineScope(Dispatchers.IO).launch {
            CBPurchase.purchaseNonSubscriptionProduct(
                product = otpProducts,
                productType = OneTimeProductType.CONSUMABLE,
                callback = object : CBCallback.OneTimePurchaseCallback {
                    override fun onError(error: CBException) {
                        lock.countDown()
                        assertThat(error, instanceOf(CBException::class.java))
                    }

                    override fun onSuccess(result: NonSubscription, status: Boolean) {
                        lock.countDown()
                        assertThat(result, instanceOf(NonSubscription::class.java))
                    }
                })

            Mockito.`when`(callBackOneTimePurchase?.let {
                billingClientManager?.purchaseNonSubscriptionProduct(otpProducts, it)
            }).thenReturn(Unit)
            callBackOneTimePurchase?.let {
                verify(billingClientManager, times(1))?.purchaseNonSubscriptionProduct(
                    otpProducts,
                    oneTimePurchaseCallback = it
                )
            }
        }
        lock.await()
    }

    @Test
    fun test_validateNonSubscriptionReceipt_success(){
        val purchaseToken = "56sadmnagdjsd"
        val lock = CountDownLatch(1)
        CoroutineScope(Dispatchers.IO).launch {
            CBPurchase.validateNonSubscriptionReceipt(purchaseToken, "productId") {
                when (it) {
                    is ChargebeeResult.Success -> {
                        lock.countDown()
                        assertThat(it, instanceOf(CBNonSubscriptionResponse::class.java))
                    }
                    is ChargebeeResult.Error -> {
                        lock.countDown()
                        println(" Error :  ${it.exp.message}")
                    }
                }
            }
        }
        lock.await()
        val response = CBNonSubscriptionResponse(nonSubscriptionDetail)
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(params.let { ReceiptResource().validateReceiptForNonSubscription(it) }).thenReturn(
                ChargebeeResult.Success(
                    response
                )
            )
            verify(ReceiptResource(), times(1)).validateReceiptForNonSubscription(params)
            verify(CBReceiptRequestBody("receipt","",null,"", null), times(1)).toMapNonSubscription()
        }
    }

    @Test
    fun test_validateNonSubscriptionReceipt_error(){
        val purchaseToken = "56sadmnagdjsd"
        CoroutineScope(Dispatchers.IO).launch {
            CBPurchase.validateNonSubscriptionReceipt(purchaseToken, "products") {
                when (it) {
                    is ChargebeeResult.Success -> {
                        assertThat(it, instanceOf(CBNonSubscriptionResponse::class.java))
                    }
                    is ChargebeeResult.Error -> {
                        println(" Error :  ${it.exp.message} response code: ${it.exp.httpStatusCode}")
                    }
                }
            }
        }
        val exception = CBException(ErrorDetail("Error"))
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(params.let { ReceiptResource().validateReceiptForNonSubscription(it) }).thenReturn(
                ChargebeeResult.Error(
                    exception
                )
            )
            verify(ReceiptResource(), times(1)).validateReceiptForNonSubscription(params)
            verify(CBReceiptRequestBody("receipt","",null,"", null), times(1)).toMapNonSubscription()
        }
    }
}