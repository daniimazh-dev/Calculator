package com.daniil.calculator.core

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import com.daniil.calculator.currentVersionCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class UserToken(
    val token: String
)

object UserDataManager {
    var token = MutableStateFlow<String?>(null)
    const val USER_DATA_FILE = "user_data_locale.json"
    private val server = DaniilServerAPI()

    private fun getAppData(context: Context): LogInUser {
        val version = try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "Unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "Unknown"
        }
        val display = Build.DISPLAY
        val device = Build.DEVICE
        val brand = Build.BRAND
        val model = Build.MODEL
        val user = Build.USER
        val os = Build.VERSION.RELEASE + "(API " + Build.VERSION.SDK_INT + ")"
        return LogInUser(
            token = null,
            version = version,
            versionCode = currentVersionCode,
            os = os,
            display = display,
            device = device,
            brand = brand,
            model = model,
            user = user
        )
    }


    private suspend fun singUp(context: Context) = withContext(Dispatchers.IO) {
        try {
            val logData = getAppData(context)
            val response = server.signUp(SignUpUser(token = token.value!!, version = logData.version, versionCode = currentVersionCode))
            when(response.code()) {
                200 -> {}

                403 -> {
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "Forbidden", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    token.value = logIn(context)
                    val file = File(context.filesDir, USER_DATA_FILE)
                    token.value?.let {
                        file.writeText(Json.encodeToString(UserToken(it)))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    private suspend fun logIn(context: Context): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = server.logIn(getAppData(context))
            return@withContext response.body()?.token
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    suspend fun sessionOut() = withContext(Dispatchers.IO) {
        try {
            server.sessionOut(UserToken(token.value ?: return@withContext))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun unactive() = withContext(Dispatchers.IO) {
        try {
            server.unactive(UserToken(token.value ?: return@withContext))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    suspend fun active() = withContext(Dispatchers.IO) {
        try {
            server.active(UserToken(token.value ?: return@withContext))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadUserData(context: Context) = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, USER_DATA_FILE)
            if (!file.exists()) {
                token.value = logIn(context)
                token.value?.let {
                    file.createNewFile()
                    file.writeText(Json.encodeToString(UserToken(it)))
                }
                return@withContext
            }

            val json = file.bufferedReader().use { it.readText() }
            token.value = Json.decodeFromString<UserToken>(json).token
            singUp(context)

        } catch (e: Exception) {
            e.printStackTrace()
            token.value = logIn(context)
            val file = File(context.filesDir, USER_DATA_FILE)
            token.value?.let {
                file.writeText(Json.encodeToString(UserToken(it)))
            }
        }
    }

    fun dropToken(context: Context) {
        File(context.filesDir, USER_DATA_FILE).delete()
        token.value = null
    }

}