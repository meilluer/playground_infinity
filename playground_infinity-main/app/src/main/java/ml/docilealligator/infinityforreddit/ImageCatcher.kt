package ml.docilealligator.infinityforreddit.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.utils.APIUtils.USER_AGENT
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

object RedditProfileLoader {

    /**
     * Fetch Reddit user profile image (snoovatar or icon) and load into ImageView.
     *
     * @param context Context for Glide
     * @param username Reddit username
     * @param accessToken OAuth2 access token
     * @param imageView ImageView to load the profile into
     */
    fun loadUserProfile(context: Context, username: String, accessToken: String, imageView: ImageView) {
        val url = "https://oauth.reddit.com/user/$username/about"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "bearer $accessToken")
            .addHeader(
                "User-Agent",
                USER_AGENT
            )
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                // Optionally handle failure
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!response.isSuccessful) return

                    val json = JSONObject(response.body!!.string())
                    val data = json.getJSONObject("data")

                    val snoovatar = data.optString("snoovatar_img", "")
                    val icon = data.optString("icon_img", "")
                    val profileImgUrl = if (snoovatar.isNotEmpty()) snoovatar else icon

                    // Load into ImageView on UI thread
                    imageView.post {
                        Glide.with(context)
                            .load(profileImgUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_current_user_14dp)
                            .error(R.drawable.ic_close_24dp)
                            .into(imageView)
                    }
                }
            }
        })
    }
}

