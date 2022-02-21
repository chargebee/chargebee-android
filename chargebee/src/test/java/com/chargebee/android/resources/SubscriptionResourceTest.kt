package com.chargebee.android.resources

import com.chargebee.android.Chargebee
import com.chargebee.android.ErrorDetail
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.models.SubscriptionDetail
import com.chargebee.android.models.SubscriptionDetailsWrapper
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
class SubscriptionResourceTest {

    @Before
    fun setUp(){
        MockitoAnnotations.initMocks(this)
        Chargebee.configure(
            site = "cb-imay-test",
            publishableApiKey = "test_EojsGoGFeHoc3VpGPQDOZGAxYy3d0FF3",
            sdkKey = "cb-j53yhbfmtfhfhkmhow3ramecom"
        )

    }
    @After
    fun tearDown(){

    }
    @Test
    fun test_subscriptionStatus_success(){

        val subscriptionDetail = SubscriptionDetail("123","item","active","","",
        "","")
        val queryParam = "0000987657"
        val lock = CountDownLatch(1)
        SubscriptionDetail.retrieveSubscription(queryParam) {
            when (it) {
                is ChargebeeResult.Success -> {
                    lock.countDown()
                    System.out.println("List plans :"+it.data)
                    MatcherAssert.assertThat(
                        (it.data),
                        Matchers.instanceOf(SubscriptionDetailsWrapper::class.java)
                    )
                }
                is ChargebeeResult.Error -> {
                    lock.countDown()
                    System.out.println("Error :"+it.exp.message)
                }
            }
        }
        lock.await()

        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(SubscriptionResource().retrieveSubscription(queryParam)).thenReturn(
                ChargebeeResult.Success(
                    subscriptionDetail
                )
            )
            Mockito.verify(SubscriptionResource(), Mockito.times(1)).retrieveSubscription(queryParam)
        }
    }
    @Test
    fun test_subscriptionStatus_error(){
        val exception = CBException(ErrorDetail("Error"))
        val queryParam = "0000987657"
        val lock = CountDownLatch(1)
        SubscriptionDetail.retrieveSubscription(queryParam) {
            when (it) {
                is ChargebeeResult.Success -> {
                    lock.countDown()
                    System.out.println("List plans :"+it.data)
                    MatcherAssert.assertThat(
                        (it.data),
                        Matchers.instanceOf(SubscriptionDetailsWrapper::class.java)
                    )
                }
                is ChargebeeResult.Error -> {
                    lock.countDown()
                    System.out.println("Error :"+it.exp.message)
                }
            }
        }
        lock.await()

        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(SubscriptionResource().retrieveSubscription(queryParam)).thenReturn(
                ChargebeeResult.Error(
                    exception
                )
            )
            Mockito.verify(SubscriptionResource(), Mockito.times(1)).retrieveSubscription(queryParam)
        }
    }
}