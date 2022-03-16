package com.chargebee.android.resources

import com.chargebee.android.Chargebee
import com.chargebee.android.ErrorDetail
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.models.Plan
import com.chargebee.android.models.PlansWrapper
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
import org.mockito.Mockito.times
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.CountDownLatch

@RunWith(MockitoJUnitRunner::class)
class PlanResourceTest {

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
    fun test_retrievePlansList_success(){
        val plan = Plan(
            "id", "name", "invoice", 123, 123, "", "",
            12, 23, "", false, false, "false", false,
            9, false, "app_store", 7, "", "", false, "", false, false
        )

        val queryParam = arrayOf("Standard", "app_store")
        val lock = CountDownLatch(1)
        Plan.retrieveAllPlans(queryParam) {
            when (it) {
                is ChargebeeResult.Success -> {
                    lock.countDown()
                    System.out.println("List plans :"+it.data)
                    MatcherAssert.assertThat(
                        (it.data),
                        Matchers.instanceOf(PlansWrapper::class.java)
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
            Mockito.`when`(PlanResource().retrieveAllPlans(queryParam)).thenReturn(
                ChargebeeResult.Success(
                    plan
                )
            )
            Mockito.verify(PlanResource(), times(1)).retrieveAllPlans(queryParam)
        }
    }
    @Test
    fun test_retrievePlansList_error(){
        val exception = CBException(ErrorDetail("Error"))
        val queryParam = arrayOf("Standard", "app_store")
        val lock = CountDownLatch(1)
        Plan.retrieveAllPlans(queryParam) {
            when (it) {
                is ChargebeeResult.Success -> {
                    lock.countDown()
                    System.out.println("List plans :"+it.data)
                    MatcherAssert.assertThat(
                        (it.data),
                        Matchers.instanceOf(PlansWrapper::class.java)
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
            Mockito.`when`(PlanResource().retrieveAllPlans(queryParam)).thenReturn(
                ChargebeeResult.Error(
                    exception
                )
            )
            Mockito.verify(PlanResource(), times(1)).retrieveAllPlans(queryParam)
        }
    }
    @Test
    fun test_retrievePlan_success(){
        val plan = Plan(
            "id", "name", "invoice", 123, 123, "", "",
            12, 23, "", false, false, "false", false,
            9, false, "app_store", 7, "", "", false, "", false, false
        )

        val queryParam = "Standard"
        val lock = CountDownLatch(1)
        Plan.retrievePlan(queryParam) {
            when (it) {
                is ChargebeeResult.Success -> {
                    lock.countDown()
                    System.out.println("List plans :"+it.data)
                    MatcherAssert.assertThat(
                        (it.data),
                        Matchers.instanceOf(PlansWrapper::class.java)
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
            Mockito.`when`(PlanResource().retrievePlan(queryParam)).thenReturn(
                ChargebeeResult.Success(
                    plan
                )
            )
            Mockito.verify(PlanResource(), times(1)).retrievePlan(queryParam)
        }
    }
    @Test
    fun test_retrievePlan_error(){
        val exception = CBException(ErrorDetail("Error"))
        val queryParam = "Standard"
        val lock = CountDownLatch(1)
        Plan.retrievePlan(queryParam) {
            when (it) {
                is ChargebeeResult.Success -> {
                    lock.countDown()
                    System.out.println("List plans :"+it.data)
                    MatcherAssert.assertThat(
                        (it.data),
                        Matchers.instanceOf(PlansWrapper::class.java)
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
            Mockito.`when`(PlanResource().retrievePlan(queryParam)).thenReturn(
                ChargebeeResult.Error(
                    exception
                )
            )
            Mockito.verify(PlanResource(), times(1)).retrievePlan(queryParam)
        }
    }
}