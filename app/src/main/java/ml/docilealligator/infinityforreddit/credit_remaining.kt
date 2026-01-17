package ml.docilealligator.infinityforreddit

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class credit_remaining {
    fun apicheck(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val apiKey = preferences.getString("elevenlabs_api_key", "") ?: ""

        if (apiKey.isEmpty()) {
            Toast.makeText(context, "Please enter an API key first", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(context, "Checking credits...", Toast.LENGTH_SHORT).show()
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api.elevenlabs.io/v1/user/subscription")
            .get()
            .addHeader("Accept", "application/json")
            .addHeader("xi-api-key", apiKey)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Failed to check credits: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, "Error: ${it.code}. Check your API key.", Toast.LENGTH_LONG).show()
                        }
                        return
                    }

                    val responseData = it.body?.string()
                    if (responseData != null) {
                        try {
                            val json = JSONObject(responseData)
                            val characterCount = json.getInt("character_count")
                            val characterLimit = json.getInt("character_limit")
                            val remaining = characterLimit - characterCount
                            val percentage = if (characterLimit > 0) {
                                (characterCount.toDouble() / characterLimit.toDouble()) * 100
                            } else {
                                0.0
                            }

                            Handler(Looper.getMainLooper()).post {
                                MaterialAlertDialogBuilder(context, R.style.MaterialAlertDialogTheme)
                                    .setTitle("ElevenLabs Credits")
                                    .setMessage("Remaining Credits: $remaining\nCredits Used: ${String.format("%.2f", percentage)}%")
                                    .setPositiveButton("OK", null)
                                    .show()
                            }
                        } catch (e: Exception) {
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(context, "Failed to parse response", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        })
    }
}
