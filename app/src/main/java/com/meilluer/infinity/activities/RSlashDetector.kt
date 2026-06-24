package com.meilluer.infinity.activities

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.meilluer.infinity.adapters.SubredditAutocompleteRecyclerViewAdapter
import com.meilluer.infinity.apis.RedditAPI
import com.meilluer.infinity.subreddit.ParseSubredditData
import com.meilluer.infinity.subreddit.SubredditData
import com.meilluer.infinity.utils.APIUtils
import com.meilluer.infinity.utils.SharedPreferencesUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.ArrayList
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class RSlashDetector(
    private val context: Context,
    private val editText: EditText,
    private val recyclerView: RecyclerView,
    private val adapter: SubredditAutocompleteRecyclerViewAdapter,
    private val retrofit: Retrofit,
    private val accessToken: String,
    private val nsfw: Boolean
) {
    private var subredditAutocompleteCall: Call<String>? = null
    private val handler = android.os.Handler()
    private val executor: Executor = Executors.newSingleThreadExecutor()

    private var isSuggestionSelected = false

    fun startListening() {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    val rSlashIndex = it.lastIndexOf("r/")
                    if (rSlashIndex != -1) {
                        val query = it.substring(rSlashIndex + 2)
                        if (query.isNotEmpty() && !isSuggestionSelected) {
                            fetchSubredditSuggestions(query)
                        } else {
                            recyclerView.visibility = View.GONE
                            if (isSuggestionSelected) {
                                isSuggestionSelected = false
                            }
                        }
                    } else {
                        recyclerView.visibility = View.GONE
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun setSuggestionSelected() {
        isSuggestionSelected = true
    }

    private fun fetchSubredditSuggestions(query: String) {
        subredditAutocompleteCall?.cancel()
        subredditAutocompleteCall = retrofit.create(RedditAPI::class.java).subredditAutocomplete(
            APIUtils.getOAuthHeader(accessToken),
            query,
            nsfw
        )
        subredditAutocompleteCall?.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        ParseSubredditData.parseSubredditListingData(
                            executor,
                            handler,
                            it,
                            nsfw,
                            object : ParseSubredditData.ParseSubredditListingDataListener {
                                override fun onParseSubredditListingDataSuccess(
                                    subredditData: ArrayList<SubredditData>,
                                    after: String
                                ) {
                                    adapter.setSubreddits(subredditData)
                                    recyclerView.visibility = View.VISIBLE
                                }

                                override fun onParseSubredditListingDataFail() {
                                    recyclerView.visibility = View.GONE
                                }
                            })
                    }
                } else {
                    recyclerView.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                recyclerView.visibility = View.GONE
            }
        })
    }
}