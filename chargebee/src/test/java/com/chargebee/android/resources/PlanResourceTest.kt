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
    private var queryParam = arrayOf("5")
    private var lock = CountDownLatch(1)
    private var exception = CBException(ErrorDetail("Error"))
    private var planId = "Standard"
    @Before
    fun setUp(){
        MockitoAnnotations.initMocks(this)
        Chargebee.configure(
            site = "site-name",
            publishableApiKey = "api-key",
            sdkKey = "sdk-key"
        )

    }
    @After
    fun tearDown(){
        lock = CountDownLatch(0)
    }
    @Test
    fun test_retrievePlansList_success(){
        Chargebee.retrieveAllPlans(queryParam) {
            when (it) {
                is ChargebeeResult.Success -> {
                    lock.countDown()
                    print("List plans :"+it.data)
                    MatcherAssert.assertThat(
                        (it.data),
                        Matchers.instanceOf(PlansWrapper::class.java)
                    )
                }
                is ChargebeeResult.Error -> {
                    lock.countDown()
                    print("Error :"+it.exp.message)
                }
            }
        }
        lock.await()
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(PlanResource().retrieveAllPlans(queryParam)).thenReturn(
                ChargebeeResult.Success(
                    ""
                )
            )
            Mockito.verify(PlanResource(), times(1)).retrieveAllPlans(queryParam)
        }
    }
    @Test
    fun test_retrievePlansList_error(){
        Chargebee.retrieveAllPlans(queryParam) {
            when (it) {
                is ChargebeeResult.Success -> {
                    lock.countDown()
                    print("List plans :"+it.data)
                    MatcherAssert.assertThat(
                        (it.data),
                        Matchers.instanceOf(PlansWrapper::class.java)
                    )
                }
                is ChargebeeResult.Error -> {
                    lock.countDown()
                    print("Error :"+it.exp.message)
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
        Chargebee.retrievePlan(planId) {
            when (it) {
                is ChargebeeResult.Success -> {
                    lock.countDown()
                    print("Plan Detail :"+it.data)
                    MatcherAssert.assertThat(
                        (it.data),
                        Matchers.instanceOf(PlansWrapper::class.java)
                    )
                }
                is ChargebeeResult.Error -> {
                    lock.countDown()
                    print("Error :"+it.exp.message)
                }
            }
        }
        lock.await()
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(PlanResource().retrievePlan(planId)).thenReturn(
                ChargebeeResult.Success(
                    ""
                )
            )
            Mockito.verify(PlanResource(), times(1)).retrievePlan(planId)
        }
    }
    @Test
    fun test_retrievePlan_error(){
        Chargebee.retrievePlan(planId) {
            when (it) {
                is ChargebeeResult.Success -> {
                    lock.countDown()
                    print("List plans :"+it.data)
                    MatcherAssert.assertThat(
                        (it.data),
                        Matchers.instanceOf(PlansWrapper::class.java)
                    )
                }
                is ChargebeeResult.Error -> {
                    lock.countDown()
                    print("Error :"+it.exp.message)
                }
            }
        }
        lock.await()
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(PlanResource().retrievePlan(planId)).thenReturn(
                ChargebeeResult.Error(
                    exception
                )
            )
            Mockito.verify(PlanResource(), times(1)).retrievePlan(planId)
        }
    }

    @Test
    fun test_retrievePlansListWithNoParams(){
        val exception = CBException(ErrorDetail("Error"))
        val queryParam = arrayOf("")
        val lock = CountDownLatch(1)
        Chargebee.retrieveAllPlans() {
            when (it) {
                is ChargebeeResult.Success -> {
                    lock.countDown()
                    print("List plans :"+it.data)
                    MatcherAssert.assertThat(
                        (it.data),
                        Matchers.instanceOf(PlansWrapper::class.java)
                    )
                }
                is ChargebeeResult.Error -> {
                    lock.countDown()
                    print("Error :"+it.exp.message)
                }
            }
        }
        lock.await()
        CoroutineScope(Dispatchers.IO).launch {
            Mockito.`when`(PlanResource().retrieveAllPlans(queryParam)).thenReturn(
                ChargebeeResult.Success(
                    ""
                )
            )
            Mockito.verify(PlanResource(), times(1)).retrieveAllPlans(queryParam)
            Mockito.verify(Chargebee.retrieveAllPlans(){}, times(1))
        }
    }
}