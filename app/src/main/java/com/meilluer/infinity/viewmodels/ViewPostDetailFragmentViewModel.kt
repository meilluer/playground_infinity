package com.meilluer.infinity.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.meilluer.infinity.SingleLiveEvent
import com.meilluer.infinity.apis.RedditAPI
import com.meilluer.infinity.moderation.ModerationEvent
import com.meilluer.infinity.moderation.ModerationEvent.ApproveFailed
import com.meilluer.infinity.moderation.ModerationEvent.Approved
import com.meilluer.infinity.moderation.ModerationEvent.DistinguishAsModFailed
import com.meilluer.infinity.moderation.ModerationEvent.DistinguishedAsMod
import com.meilluer.infinity.moderation.ModerationEvent.LockFailed
import com.meilluer.infinity.moderation.ModerationEvent.Locked
import com.meilluer.infinity.moderation.ModerationEvent.MarkAsSpamFailed
import com.meilluer.infinity.moderation.ModerationEvent.MarkNSFWFailed
import com.meilluer.infinity.moderation.ModerationEvent.MarkSpoilerFailed
import com.meilluer.infinity.moderation.ModerationEvent.MarkedAsSpam
import com.meilluer.infinity.moderation.ModerationEvent.MarkedNSFW
import com.meilluer.infinity.moderation.ModerationEvent.MarkedSpoiler
import com.meilluer.infinity.moderation.ModerationEvent.RemoveFailed
import com.meilluer.infinity.moderation.ModerationEvent.SetStickyPost
import com.meilluer.infinity.moderation.ModerationEvent.SetStickyPostFailed
import com.meilluer.infinity.moderation.ModerationEvent.UndistinguishAsModFailed
import com.meilluer.infinity.moderation.ModerationEvent.UndistinguishedAsMod
import com.meilluer.infinity.moderation.ModerationEvent.UnlockFailed
import com.meilluer.infinity.moderation.ModerationEvent.Unlocked
import com.meilluer.infinity.moderation.ModerationEvent.UnmarkNSFWFailed
import com.meilluer.infinity.moderation.ModerationEvent.UnmarkSpoilerFailed
import com.meilluer.infinity.moderation.ModerationEvent.UnmarkedNSFW
import com.meilluer.infinity.moderation.ModerationEvent.UnmarkedSpoiler
import com.meilluer.infinity.moderation.ModerationEvent.UnsetStickyPost
import com.meilluer.infinity.moderation.ModerationEvent.UnsetStickyPostFailed
import com.meilluer.infinity.post.Post
import com.meilluer.infinity.utils.APIUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class ViewPostDetailFragmentViewModel(
    private val oauthRetrofit: Retrofit,
    private val accessToken: String?
) : ViewModel() {
    val moderationEventLiveData: SingleLiveEvent<ModerationEvent> = SingleLiveEvent()

    fun approvePost(post: Post, position: Int) {
        val params: MutableMap<String, String> = HashMap()
        params[APIUtils.ID_KEY] = post.fullName
        oauthRetrofit.create(RedditAPI::class.java)
            .approveThing(APIUtils.getOAuthHeader(accessToken), params)
            .enqueue(object : Callback<String?> {
                override fun onResponse(call: Call<String?>, response: Response<String?>) {
                    if (response.isSuccessful) {
                        moderationEventLiveData.postValue(Approved(post, position))
                    } else {
                        moderationEventLiveData.postValue(ApproveFailed(post, position))
                    }
                }

                override fun onFailure(call: Call<String?>, throwable: Throwable) {
                    moderationEventLiveData.postValue(ApproveFailed(post, position))
                }
            })
    }

    fun removePost(post: Post, position: Int, isSpam: Boolean) {
        val params: MutableMap<String, String> = HashMap()
        params[APIUtils.ID_KEY] = post.fullName
        params[APIUtils.SPAM_KEY] = isSpam.toString()
        oauthRetrofit.create(RedditAPI::class.java)
            .removeThing(APIUtils.getOAuthHeader(accessToken), params)
            .enqueue(object : Callback<String?> {
                override fun onResponse(call: Call<String?>, response: Response<String?>) {
                    if (response.isSuccessful) {
                        moderationEventLiveData.postValue(
                            if (isSpam) MarkedAsSpam(
                                post,
                                position
                            ) else ModerationEvent.Removed(post, position)
                        )
                    } else {
                        moderationEventLiveData.postValue(
                            if (isSpam) MarkAsSpamFailed(
                                post,
                                position
                            ) else RemoveFailed(post, position)
                        )
                    }
                }

                override fun onFailure(call: Call<String?>, throwable: Throwable) {
                    moderationEventLiveData.postValue(
                        if (isSpam) MarkAsSpamFailed(
                            post,
                            position
                        ) else RemoveFailed(post, position)
                    )
                }
            })
    }

    fun toggleSticky(post: Post, position: Int) {
        val params: MutableMap<String, String> = HashMap()
        params[APIUtils.ID_KEY] = post.fullName
        params[APIUtils.STATE_KEY] = (!post.isStickied).toString()
        params[APIUtils.API_TYPE_KEY] = APIUtils.API_TYPE_JSON
        oauthRetrofit.create(RedditAPI::class.java)
            .toggleStickyPost(APIUtils.getOAuthHeader(accessToken), params)
            .enqueue(object : Callback<String?> {
                override fun onResponse(call: Call<String?>, response: Response<String?>) {
                    if (response.isSuccessful) {
                        post.setIsStickied(!post.isStickied)
                        moderationEventLiveData.postValue(
                            if (post.isStickied) SetStickyPost(
                                post,
                                position
                            ) else UnsetStickyPost(post, position)
                        )
                    } else {
                        moderationEventLiveData.postValue(
                            if (post.isStickied) UnsetStickyPostFailed(
                                post,
                                position
                            ) else SetStickyPostFailed(post, position)
                        )
                    }
                }

                override fun onFailure(call: Call<String?>, throwable: Throwable) {
                    moderationEventLiveData.postValue(
                        if (post.isStickied) UnsetStickyPostFailed(
                            post,
                            position
                        ) else SetStickyPostFailed(post, position)
                    )
                }
            })
    }

    fun toggleLock(post: Post, position: Int) {
        val params: MutableMap<String, String> = HashMap()
        params[APIUtils.ID_KEY] = post.fullName
        val call: Call<String> = if (post.isLocked) oauthRetrofit.create(
            RedditAPI::class.java
        ).unLockThing(APIUtils.getOAuthHeader(accessToken), params) else oauthRetrofit.create(
            RedditAPI::class.java
        ).lockThing(APIUtils.getOAuthHeader(accessToken), params)
        call.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if (response.isSuccessful) {
                    post.setIsLocked(!post.isLocked)
                    moderationEventLiveData.postValue(
                        if (post.isLocked) Locked(
                            post,
                            position
                        ) else Unlocked(post, position)
                    )
                } else {
                    moderationEventLiveData.postValue(
                        if (post.isLocked) UnlockFailed(
                            post,
                            position
                        ) else LockFailed(post, position)
                    )
                }
            }

            override fun onFailure(call: Call<String?>, throwable: Throwable) {
                moderationEventLiveData.postValue(
                    if (post.isLocked) UnlockFailed(
                        post,
                        position
                    ) else LockFailed(post, position)
                )
            }
        })
    }

    fun toggleNSFW(post: Post, position: Int) {
        val params: MutableMap<String, String> = HashMap()
        params[APIUtils.ID_KEY] = post.fullName
        val call: Call<String> = if (post.isNSFW) oauthRetrofit.create(
            RedditAPI::class.java
        ).unmarkNSFW(APIUtils.getOAuthHeader(accessToken), params) else oauthRetrofit.create(
            RedditAPI::class.java
        ).markNSFW(APIUtils.getOAuthHeader(accessToken), params)
        call.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if (response.isSuccessful) {
                    post.isNSFW = !post.isNSFW
                    moderationEventLiveData.postValue(
                        if (post.isNSFW) MarkedNSFW(
                            post,
                            position
                        ) else UnmarkedNSFW(post, position)
                    )
                } else {
                    moderationEventLiveData.postValue(
                        if (post.isNSFW) UnmarkNSFWFailed(
                            post,
                            position
                        ) else MarkNSFWFailed(post, position)
                    )
                }
            }

            override fun onFailure(call: Call<String?>, throwable: Throwable) {
                moderationEventLiveData.postValue(
                    if (post.isNSFW) UnmarkNSFWFailed(
                        post,
                        position
                    ) else MarkNSFWFailed(post, position)
                )
            }
        })
    }

    fun toggleSpoiler(post: Post, position: Int) {
        val params: MutableMap<String, String> = HashMap()
        params[APIUtils.ID_KEY] = post.fullName
        val call: Call<String> = if (post.isSpoiler) oauthRetrofit.create(
            RedditAPI::class.java
        ).unmarkSpoiler(
            APIUtils.getOAuthHeader(accessToken),
            params
        ) else oauthRetrofit.create(
            RedditAPI::class.java
        ).markSpoiler(APIUtils.getOAuthHeader(accessToken), params)
        call.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if (response.isSuccessful) {
                    post.isSpoiler = !post.isSpoiler
                    moderationEventLiveData.postValue(
                        if (post.isSpoiler) MarkedSpoiler(
                            post,
                            position
                        ) else UnmarkedSpoiler(post, position)
                    )
                } else {
                    moderationEventLiveData.postValue(
                        if (post.isSpoiler) UnmarkSpoilerFailed(
                            post,
                            position
                        ) else MarkSpoilerFailed(post, position)
                    )
                }
            }

            override fun onFailure(call: Call<String?>, throwable: Throwable) {
                moderationEventLiveData.postValue(
                    if (post.isSpoiler) UnmarkSpoilerFailed(
                        post,
                        position
                    ) else MarkSpoilerFailed(post, position)
                )
            }
        })
    }

    fun toggleMod(post: Post, position: Int) {
        val params: MutableMap<String, String> = HashMap()
        params[APIUtils.ID_KEY] = post.fullName
        params[APIUtils.HOW_KEY] = if (post.isModerator) APIUtils.HOW_NO else APIUtils.HOW_YES
        oauthRetrofit.create(RedditAPI::class.java)
            .toggleDistinguishedThing(APIUtils.getOAuthHeader(accessToken), params)
            .enqueue(object : Callback<String?> {
                override fun onResponse(call: Call<String?>, response: Response<String?>) {
                    if (response.isSuccessful) {
                        post.setIsModerator(!post.isModerator)
                        moderationEventLiveData.postValue(
                            if (post.isModerator) DistinguishedAsMod(
                                post,
                                position
                            ) else UndistinguishedAsMod(post, position)
                        )
                    } else {
                        moderationEventLiveData.postValue(
                            if (post.isModerator) UndistinguishAsModFailed(
                                post,
                                position
                            ) else DistinguishAsModFailed(post, position)
                        )
                    }
                }

                override fun onFailure(call: Call<String?>, throwable: Throwable) {
                    moderationEventLiveData.postValue(
                        if (post.isModerator) UndistinguishAsModFailed(
                            post,
                            position
                        ) else DistinguishAsModFailed(post, position)
                    )
                }
            })
    }

    companion object {
        fun provideFactory(oauthRetrofit: Retrofit, accessToken: String?) : ViewModelProvider.Factory {
            return object: ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return ViewPostDetailFragmentViewModel(
                        oauthRetrofit, accessToken
                    ) as T
                }
            }
        }
    }
}