package com.meilluer.infinity

import com.meilluer.infinity.post.Post

interface PostModerationActionHandler {
    fun approvePost(post: Post, position: Int)
    fun removePost(post: Post, position: Int, isSpam: Boolean)
    fun toggleSticky(post: Post, position: Int)
    fun toggleLock(post: Post, position: Int)
    fun toggleNSFW(post: Post, position: Int)
    fun toggleSpoiler(post: Post, position: Int)
    fun toggleMod(post: Post, position: Int)
}