package com.daniil.calculator.core

import com.daniil.calculator.convertorscreen.convertor.convertorpanel.custom.AvailableExchange
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.custom.ExchangeResponse
import com.daniil.calculator.convertorscreen.report.ReportRequest
import com.daniil.calculator.settingsscreen.customscreen.ChangeLogData
import com.daniil.csb.SettingsProvider
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

object RetrofitDaniilServerInstance {
    const val BASE_URL = "http://31.134.109.18:70"
    const val TEST_URL = "http://31.134.109.18:7070"

    // Base server
    val api: DaniilServerAPIInterface by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DaniilServerAPIInterface::class.java)
    }

    // Test server
    val devApi: DaniilServerAPIInterface by lazy {
        Retrofit.Builder()
            .baseUrl(TEST_URL).addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DaniilServerAPIInterface::class.java)
    }

}


@Serializable
data class VersionRequest(
    val versionName: String,
    val versionCode: Int,
    val uri: String,
    val whatsNew: String,
)

@Serializable
data class ChangeLogList(
    val changeLogList: List<ChangeLogData>,
)

@Serializable
data class ConvertorsStatus(
    val blocked: Map<String, Int>,
)

@Serializable
data class LogInUser(
    val token: String? = null,
    val version: String,
    val versionCode: Int,
    val os: String,
    val display: String,
    val device: String,
    val brand: String,
    val model: String,
    val user: String
)

@Serializable
data class SignUpUser(
    val token: String? = null,
    val version: String,
    val versionCode: Int,
)

class DaniilServerAPI() : DaniilServerAPIInterface {
    var currentInterface = RetrofitDaniilServerInstance.api
    private fun checkLocale() {
        if (SettingsProvider.getValue<Boolean>("locale_mode").value) error("LocaleModeEnable")

        val useTestServer = SettingsProvider.getValue<Boolean>("use_test_server").value
        currentInterface = when {
            useTestServer -> RetrofitDaniilServerInstance.devApi
            else -> RetrofitDaniilServerInstance.api
        }

    }

    override suspend fun getLastVersion(): Response<VersionRequest> {
        checkLocale()
        return currentInterface.getLastVersion()
    }

    override suspend fun logIn(userData: LogInUser): Response<UserToken?> {
        checkLocale()
        return currentInterface.logIn(userData)
    }


    override suspend fun signUp(userData: SignUpUser): Response<Void> {
        checkLocale()
        return currentInterface.signUp(userData)
    }

    override suspend fun sessionOut(token: UserToken): Response<Void> {
        checkLocale()
        return currentInterface.sessionOut(token)
    }

    override suspend fun unactive(token: UserToken): Response<Void> {
        checkLocale()
        return currentInterface.unactive(token)
    }

    override suspend fun active(token: UserToken): Response<Void> {
        checkLocale()
        return currentInterface.active(token)
    }

    override suspend fun sendReport(report: ReportRequest): Response<Void> {
        checkLocale()
        return currentInterface.sendReport(report)
    }

    override suspend fun getChangeLog(): Response<ChangeLogList> {
        checkLocale()
        return currentInterface.getChangeLog()
    }

    override suspend fun getConvertorsStatus(): Response<ConvertorsStatus> {
        checkLocale()
        return currentInterface.getConvertorsStatus()
    }

    override suspend fun getAvailableExchange(token: UserToken): Response<AvailableExchange> {
        checkLocale()
        return currentInterface.getAvailableExchange(token)
    }

    override suspend fun getExchangeByDate(token: UserToken, date: String): Response<ExchangeResponse> {
        checkLocale()
        return currentInterface.getExchangeByDate(token, date)
    }

    override suspend fun getCurrentExchange(token: UserToken): Response<ExchangeResponse> {
        checkLocale()
        return currentInterface.getCurrentExchange(token)
    }

}

interface DaniilServerAPIInterface {
    @GET("/version")
    suspend fun getLastVersion(): Response<VersionRequest>

    @POST("/logIn")
    suspend fun logIn(@Body userData: LogInUser): Response<UserToken?>

    @POST("/signUp")
    suspend fun signUp(@Body userData: SignUpUser): Response<Void>

    @POST("/signOut")
    suspend fun sessionOut(@Body token: UserToken): Response<Void>

    @POST("/active")
    suspend fun active(@Body token: UserToken): Response<Void>

    @POST("/unactive")
    suspend fun unactive(@Body token: UserToken): Response<Void>

    @POST("/report")
    suspend fun sendReport(@Body report: ReportRequest): Response<Void>

    @GET("/changelog")
    suspend fun getChangeLog(): Response<ChangeLogList>

    @GET("/convertors")
    suspend fun getConvertorsStatus(): Response<ConvertorsStatus>

    @POST("/convertors/currency/get-available")
    suspend fun getAvailableExchange(@Body token: UserToken): Response<AvailableExchange>

    @POST("convertors/currency/get-exchange/{date}")
    suspend fun getExchangeByDate(@Body token: UserToken, @Path("date") date: String): Response<ExchangeResponse>

    @POST("convertors/currency/get-exchange")
    suspend fun getCurrentExchange(@Body token: UserToken): Response<ExchangeResponse>
}

