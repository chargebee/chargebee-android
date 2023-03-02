package com.chargebee.android.resources

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.android.billingclient.api.*
import com.chargebee.android.Chargebee
import com.chargebee.android.ErrorDetail
import com.chargebee.android.billingservice.BillingClientManager
import com.chargebee.android.billingservice.CBCallback
import com.chargebee.android.billingservice.CBCallback.ListProductsCallback
import com.chargebee.android.billingservice.CBPurchase
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.CBProductIDResult
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.models.CBProduct
import com.chargebee.android.network.*
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
class BillingClientManagerTest  {

    private var billingClientManager: BillingClientManager? = null
    @Mock
    lateinit var billingClient: BillingClient

    @Mock
    lateinit var skuDetails: SkuDetails

    private var mContext: Context? = null
    private var callBack : ListProductsCallback<ArrayList<CBProduct>>? = null
    private var callBackPurchase : CBCallback.PurchaseCallback<String>? = null
    private val productIdList = arrayListOf("merchant.pro.android", "merchant.premium.android")
    private var customer = CBCustomer("test","android","test","test@gmail.com")
    private var customerId: String = "test"


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
                ApplicationProvider.getApplicationContext(),
                BillingClient.SkuType.SUBS,
                productIdList, it
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
            val skuType = CBPurchase.SkuType.SUBS
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
            Mockito.`when`(CBPurchase.retrieveProductIdentifers(queryParam) {
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
            Mockito.`when`(CBPurchase.retrieveProductIdentifers(queryParam) {
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
        // val jsonDetails = "{\"productId\":\"merchant.premium.android\",\"type\":\"subs\",\"title\":\"Premium Plan (Chargebee Example)\",\"name\":\"Premium Plan\",\"price\":\"₹2,650.00\",\"price_amount_micros\":2650000000,\"price_currency_code\":\"INR\",\"description\":\"Every 6 Months\",\"subscriptionPeriod\":\"P6M\",\"skuDetailsToken\":\"AEuhp4J0KiD1Bsj3Yq2mHPBRNHUBdzs4nTJY3PWRR8neE-22MJNssuDzH2VLFKv35Ov8\"}"

        val products = CBProduct("","","", skuDetails,true)
        val lock = CountDownLatch(1)
        CoroutineScope(Dispatchers.IO).launch {
            CBPurchase.purchaseProduct(
                products,"",
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
                billingClientManager?.purchase(products, it)
            }).thenReturn(Unit)
            callBackPurchase?.let {
                verify(billingClientManager, times(1))?.purchase(products,purchaseCallBack = it)
            }
        }
        lock.await()

    }
    @Test
    fun test_purchaseProduct_error(){
        val jsonDetails = "{\"productId\":\"merchant.premium.android\",\"type\":\"subs\",\"title\":\"Premium Plan (Chargebee Example)\",\"name\":\"Premium Plan\",\"price\":\"₹2,650.00\",\"price_amount_micros\":2650000000,\"price_currency_code\":\"INR\",\"description\":\"Every 6 Months\",\"subscriptionPeriod\":\"P6M\",\"skuDetailsToken\":\"AEuhp4J0KiD1Bsj3Yq2mHPBRNHUBdzs4nTJY3PWRR8neE-22MJNssuDzH2VLFKv35Ov8\"}"

        val products = CBProduct("","","", skuDetails,true)
        CoroutineScope(Dispatchers.IO).launch {
            CBPurchase.purchaseProduct(
                products,"",
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
        val jsonDetails = "{\"productId\":\"merchant.premium.android\",\"type\":\"subs\",\"title\":\"Premium Plan (Chargebee Example)\",\"name\":\"Premium Plan\",\"price\":\"₹2,650.00\",\"price_amount_micros\":2650000000,\"price_currency_code\":\"INR\",\"description\":\"Every 6 Months\",\"subscriptionPeriod\":\"P6M\",\"skuDetailsToken\":\"AEuhp4J0KiD1Bsj3Yq2mHPBRNHUBdzs4nTJY3PWRR8neE-22MJNssuDzH2VLFKv35Ov8\"}"

        val products = CBProduct("merchant.premium.android","Premium Plan (Chargebee Example)","₹2,650.00", skuDetails,true)
        val lock = CountDownLatch(1)
        CoroutineScope(Dispatchers.IO).launch {
            CBPurchase.validateReceipt(purchaseToken, products) {
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

        val params = Params(
            purchaseToken,
            products.productId,
            customer,
            Chargebee.channel
        )
        val receiptDetail = ReceiptDetail("subscriptionId","customerId","planId")
        val response = CBReceiptResponse(receiptDetail)

        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(params.let { ReceiptResource().validateReceipt(it) }).thenReturn(
                ChargebeeResult.Success(
                    response
                )
            )
            verify(ReceiptResource(), times(1)).validateReceipt(params)
            verify(CBReceiptRequestBody("receipt","",null,""), times(1)).toCBReceiptReqBody()
        }
    }
    @Test
    fun test_validateReceipt_error(){
        val purchaseToken = "56sadmnagdjsd"
        val jsonDetails = "{\"productId\":\"merchant.premium.test.android\",\"type\":\"subs\",\"title\":\"Premium Plan (Chargebee Example)\",\"name\":\"Premium Plan\",\"price\":\"₹2,650.00\",\"price_amount_micros\":2650000000,\"price_currency_code\":\"INR\",\"description\":\"Every 6 Months\",\"subscriptionPeriod\":\"P6M\",\"skuDetailsToken\":\"AEuhp4J0KiD1Bsj3Yq2mHPBRNHUBdzs4nTJY3PWRR8neE-22MJNssuDzH2VLFKv35Ov8\"}"

        // val skuDetails: SkuDetails? = null
        val products = CBProduct("merchant.premium.test.android","Premium Plan (Chargebee Example)","₹2,650.00", skuDetails,true)
        CoroutineScope(Dispatchers.IO).launch {
            CBPurchase.validateReceipt(purchaseToken, products) {
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

        val params = Params(
            purchaseToken,
            products.productId,
            customer,
            Chargebee.channel
        )
        val exception = CBException(ErrorDetail("Error"))
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(params.let { ReceiptResource().validateReceipt(it) }).thenReturn(
                ChargebeeResult.Error(
                    exception
                )
            )
            verify(ReceiptResource(), times(1)).validateReceipt(params)
            verify(CBReceiptRequestBody("receipt","",null,""), times(1)).toCBReceiptReqBody()
        }
    }

    @Test
    fun test_purchaseProductWithEmptyCBCustomer_success(){
        val customer = CBCustomer("","","","")
        val products = CBProduct("","","", skuDetails,true)
        val lock = CountDownLatch(1)
        CoroutineScope(Dispatchers.IO).launch {
            CBPurchase.purchaseProduct(
                products,customer,
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
                billingClientManager?.purchase(products, it)
            }).thenReturn(Unit)
            callBackPurchase?.let {
                verify(billingClientManager, times(1))?.purchase(products,purchaseCallBack = it)
            }
        }
        lock.await()
    }

    @Test
    fun test_purchaseProductWithCBCustomer_success(){
        val products = CBProduct("","","", skuDetails,true)
        val lock = CountDownLatch(1)
        CoroutineScope(Dispatchers.IO).launch {
            CBPurchase.purchaseProduct(
                products,customer,
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
                billingClientManager?.purchase(products, it)
            }).thenReturn(Unit)
            callBackPurchase?.let {
                verify(billingClientManager, times(1))?.purchase(products,purchaseCallBack = it)
            }
        }
        lock.await()
    }
    @Test
    fun test_purchaseProductWithCBCustomer_error(){
        val products = CBProduct("","","", skuDetails,true)
        CoroutineScope(Dispatchers.IO).launch {
            CBPurchase.purchaseProduct(
                products,customer,
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
}