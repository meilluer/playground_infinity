package ml.docilealligator.infinityforreddit.markdown.video

import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.URLSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Tracks
import androidx.media3.common.TrackSelectionOverride
import androidx.recyclerview.widget.RecyclerView
import com.google.common.collect.ImmutableList
import io.noties.markwon.Markwon
import io.noties.markwon.recycler.MarkwonAdapter
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import ml.docilealligator.infinityforreddit.SaveMemoryCenterInisdeDownsampleStrategy
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.activities.BaseActivity
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity
import ml.docilealligator.infinityforreddit.activities.ViewVideoActivity
import ml.docilealligator.infinityforreddit.adapters.CommentsRecyclerViewAdapter
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment
import ml.docilealligator.infinityforreddit.databinding.MarkdownVideoBlockBinding
import ml.docilealligator.infinityforreddit.managers.VideoMuteManager
import ml.docilealligator.infinityforreddit.thing.MediaMetadata
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils
import ml.docilealligator.infinityforreddit.videoautoplay.ExoCreator
import ml.docilealligator.infinityforreddit.videoautoplay.ExoPlayerViewHelper
import ml.docilealligator.infinityforreddit.videoautoplay.Playable
import ml.docilealligator.infinityforreddit.videoautoplay.ToroPlayer
import ml.docilealligator.infinityforreddit.videoautoplay.ToroUtil
import ml.docilealligator.infinityforreddit.videoautoplay.media.PlaybackInfo
import ml.docilealligator.infinityforreddit.videoautoplay.widget.Container
import kotlin.math.min

class VideoEntry(
    private val baseActivity: BaseActivity,
    embeddedMediaType: Int,
    private val exoCreator: ExoCreator?,
    private val autoplayVideo: Boolean,
    private val autoplayNsfwVideos: Boolean,
    private val isNSFW: Boolean,
    private var nonDataSavingModeDefaultResolution: Int = 0,
    private var dataSavingModeDefaultResolution: Int = 0,
    private var dataSavingMode: Boolean = false,
    private val videoMuteManager: VideoMuteManager?,
    private val onItemClickListener: OnItemClickListener
): MarkwonAdapter.Entry<VideoBlock, VideoEntry.Holder>() {

    constructor(
        baseActivity: BaseActivity,
        embeddedMediaType: Int,
        onItemClickListener: OnItemClickListener
    ) : this(
        baseActivity,
        embeddedMediaType,
        null,
        false,
        false,
        false,
        0,
        0,
        false,
        null,
        onItemClickListener
    )
    private val saveMemoryCenterInsideDownsampleStrategy: SaveMemoryCenterInisdeDownsampleStrategy
    private val colorAccent: Int
    private val primaryTextColor: Int
    private val postContentColor: Int
    private val linkColor: Int
    private val canShowImage: Boolean
    private val canShowGif: Boolean

    init {
        val sharedPreferences = baseActivity.getDefaultSharedPreferences()
        this.saveMemoryCenterInsideDownsampleStrategy = SaveMemoryCenterInisdeDownsampleStrategy(
            sharedPreferences.getString(
                SharedPreferencesUtils.POST_FEED_MAX_RESOLUTION,
                "5000000"
            )!!.toInt()
        )
        colorAccent = baseActivity.getCustomThemeWrapper().getColorAccent()
        primaryTextColor = baseActivity.getCustomThemeWrapper().getPrimaryTextColor()
        postContentColor = baseActivity.getCustomThemeWrapper().getPostContentColor()
        linkColor = baseActivity.getCustomThemeWrapper().getLinkColor()
        canShowImage = SharedPreferencesUtils.canShowImage(embeddedMediaType)
        canShowGif = SharedPreferencesUtils.canShowGif(embeddedMediaType)
    }

    override fun createHolder(inflater: LayoutInflater, parent: ViewGroup): Holder {
        return Holder(MarkdownVideoBlockBinding.inflate(inflater, parent, false))
    }

    override fun bindHolder(markwon: Markwon, holder: Holder, node: VideoBlock) {
        holder.videoBlock = node

        if (node.mediaMetadata.caption != null) {
            holder.binding.captionTextViewMarkdownVideoBlock.visibility = View.VISIBLE
            holder.binding.captionTextViewMarkdownVideoBlock.text = node.mediaMetadata.caption
        }

        if (dataSavingMode) {
            showVideoAsUrl(holder, node)
        } else {
            if ((autoplayVideo && !isNSFW) || (autoplayNsfwVideos && isNSFW)) {
                holder.binding.playerViewMarkdownVideoBlock.visibility = View.VISIBLE
                holder.binding.previewPlayIconMarkdownVideoBlock.visibility = View.GONE
            } else {
                showVideoAsUrl(holder, node)
            }
        }
    }

    private fun showVideoAsUrl(holder: Holder, node: VideoBlock) {
        holder.binding.playerViewMarkdownVideoBlock.visibility = View.GONE
        holder.binding.previewPlayIconMarkdownVideoBlock.visibility = View.VISIBLE
    }

    override fun onViewRecycled(holder: Holder) {
        super.onViewRecycled(holder)
        holder.binding.errorLoadingVideoImageViewMarkdownVideoBlock.visibility = View.GONE
        holder.binding.captionTextViewMarkdownVideoBlock.visibility = View.GONE
        holder.binding.captionTextViewMarkdownVideoBlock.gravity = Gravity.CENTER_HORIZONTAL
        holder.release()
    }

    inner class Holder(
        val binding: MarkdownVideoBlockBinding
    ) : MarkwonAdapter.Holder(binding.getRoot()), ToroPlayer {
        var videoBlock: VideoBlock? = null
        var container: Container? = null
        var helper: ExoPlayerViewHelper? = null
        var volume = 0f
        var isManuallyPaused = false
        val playDrawable: Drawable
        val pauseDrawable: Drawable
        var setDefaultResolutionAlready: Boolean = false

        val muteButton: ImageView? = binding.playerViewMarkdownVideoBlock.findViewById<ImageView>(R.id.mute_exo_playback_control_view)
        val fullscreenButton: ImageView? = binding.playerViewMarkdownVideoBlock.findViewById<ImageView>(R.id.fullscreen_exo_playback_control_view)
        val playPauseButton: ImageView? = binding.playerViewMarkdownVideoBlock.findViewById<ImageView>(R.id.exo_play)

        init {
            playDrawable = AppCompatResources.getDrawable(baseActivity, R.drawable.ic_play_arrow_24dp)!!
            pauseDrawable = AppCompatResources.getDrawable(baseActivity, R.drawable.ic_pause_24dp)!!

            binding.captionTextViewMarkdownVideoBlock.setTextColor(postContentColor)
            binding.captionTextViewMarkdownVideoBlock.setLinkTextColor(linkColor)

            if (baseActivity.contentTypeface != null) {
                binding.captionTextViewMarkdownVideoBlock.setTypeface(baseActivity.contentTypeface)
            }

            binding.frameLayoutMarkdownVideoBlock.setOnClickListener {
                onItemClickListener.onItemClick(videoBlock?.mediaMetadata, -1L)
            }

            binding.captionTextViewMarkdownVideoBlock.movementMethod = BetterLinkMovementMethod.newInstance()
                .setOnLinkClickListener { _, url: String ->
                    val intent = Intent(baseActivity, LinkResolverActivity::class.java)
                    intent.setData(url.toUri())
                    baseActivity.startActivity(intent)
                    true
                }
                .setOnLinkLongClickListener { _, url: String ->
                    val urlMenuBottomSheetFragment = UrlMenuBottomSheetFragment.newInstance(url)
                    urlMenuBottomSheetFragment.show(
                        baseActivity.supportFragmentManager,
                        urlMenuBottomSheetFragment.tag
                    )
                    true
                }

            muteButton?.setOnClickListener {
                helper?.let { helper ->
                    if (helper.volume != 0f) {
                        muteButton.setImageDrawable(AppCompatResources.getDrawable(baseActivity, R.drawable.ic_mute_24dp))
                        helper.volume = 0f
                        volume = 0f
                    } else {
                        muteButton.setImageDrawable(AppCompatResources.getDrawable(baseActivity, R.drawable.ic_unmute_24dp))
                        helper.volume = 1f
                        volume = 1f
                    }
                }
            }

            fullscreenButton?.setOnClickListener {
                val resumePosition = helper?.latestPlaybackInfo?.resumePosition ?: -1L
                onItemClickListener.onItemClick(videoBlock?.mediaMetadata, resumePosition)
            }

            playPauseButton?.setOnClickListener {
                if (isPlaying) {
                    pause()
                    isManuallyPaused = true
                } else {
                    isManuallyPaused = false
                    play()
                }
            }
        }

        override fun getPlayerView(): View {
            return binding.playerViewMarkdownVideoBlock
        }

        override fun getCurrentPlaybackInfo(): PlaybackInfo {
            return helper?.latestPlaybackInfo ?: PlaybackInfo()
        }

        override fun initialize(
            container: Container,
            playbackInfo: PlaybackInfo
        ) {
            videoBlock?.mediaMetadata?.original?.url?.let { url ->
                if (this.container == null) {
                    this.container = container
                }
                if (helper == null) {
                    helper = if (exoCreator != null) {
                        ExoPlayerViewHelper(this, url.toUri(), null, exoCreator)
                    } else {
                        ExoPlayerViewHelper(this, url.toUri())
                    }
                    helper?.addEventListener(object : Playable.EventListener {
                        override fun onTracksChanged(tracks: Tracks) {
                            val trackGroups = tracks.groups
                            if (!trackGroups.isEmpty()) {
                                if (!setDefaultResolutionAlready) {
                                    var desiredResolution = 0
                                    if (dataSavingMode) {
                                        if (dataSavingModeDefaultResolution > 0) {
                                            desiredResolution = dataSavingModeDefaultResolution
                                        }
                                    } else if (nonDataSavingModeDefaultResolution > 0) {
                                        desiredResolution = nonDataSavingModeDefaultResolution
                                    }

                                    if (desiredResolution > 0) {
                                        var trackSelectionOverride: TrackSelectionOverride? = null
                                        var bestTrackIndex = -1
                                        var bestResolution = -1
                                        var worstResolution = Int.MAX_VALUE
                                        var worstTrackIndex = -1
                                        var bestTrackGroup: Tracks.Group? = null
                                        var worstTrackGroup: Tracks.Group? = null
                                        for (trackGroup in tracks.groups) {
                                            if (trackGroup.type == C.TRACK_TYPE_VIDEO) {
                                                for (trackIndex in 0 until trackGroup.length) {
                                                    val trackResolution = min(
                                                        trackGroup.getTrackFormat(trackIndex).height,
                                                        trackGroup.getTrackFormat(trackIndex).width
                                                    )
                                                    if (trackResolution in (bestResolution + 1)..desiredResolution) {
                                                        bestTrackIndex = trackIndex
                                                        bestResolution = trackResolution
                                                        bestTrackGroup = trackGroup
                                                    }
                                                    if (trackResolution < worstResolution) {
                                                        worstTrackIndex = trackIndex
                                                        worstResolution = trackResolution
                                                        worstTrackGroup = trackGroup
                                                    }
                                                }
                                            }
                                        }

                                        if (bestTrackIndex != -1 && bestTrackGroup != null) {
                                            trackSelectionOverride = TrackSelectionOverride(
                                                bestTrackGroup.mediaTrackGroup,
                                                ImmutableList.of(bestTrackIndex)
                                            )
                                        } else if (worstTrackIndex != -1 && worstTrackGroup != null) {
                                            trackSelectionOverride = TrackSelectionOverride(
                                                worstTrackGroup.mediaTrackGroup,
                                                ImmutableList.of(worstTrackIndex)
                                            )
                                        }

                                        if (trackSelectionOverride != null) {
                                            helper?.player?.let { player ->
                                                player.trackSelectionParameters =
                                                    player.trackSelectionParameters
                                                        .buildUpon()
                                                        .addOverride(trackSelectionOverride)
                                                        .build()
                                            }
                                        }
                                    }
                                    setDefaultResolutionAlready = true
                                }

                                var hasAudio = false
                                for (trackGroup in tracks.groups) {
                                    if (trackGroup.type == C.TRACK_TYPE_AUDIO) {
                                        hasAudio = true
                                        break
                                    }
                                }

                                 if (hasAudio) {
                                     videoMuteManager?.getMasterMutingOption()?.let {
                                         volume = if (it) 0f else 1f
                                     }
                                     helper?.volume = volume
                                     muteButton?.let { button ->
                                         button.visibility = View.VISIBLE
                                         button.setImageDrawable(
                                             AppCompatResources.getDrawable(
                                                 baseActivity,
                                                 if (volume == 0f) R.drawable.ic_mute_24dp else R.drawable.ic_unmute_24dp
                                             )
                                         )
                                     }
                                 } else {
                                     muteButton?.visibility = View.GONE
                                 }
                            }
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            binding.errorLoadingVideoImageViewMarkdownVideoBlock.visibility = View.VISIBLE
                        }
                    })
                }

                val commentViewHolder = getCommentViewHolder(container)
                if (commentViewHolder != null) {
                    commentViewHolder.activeVideoHolder = this@Holder
                }

                helper?.initialize(container, playbackInfo)
            }
        }

        private fun getCommentViewHolder(container: RecyclerView): CommentsRecyclerViewAdapter.CommentBaseViewHolder? {
            var current: View? = itemView
            while (current != null && current != container) {
                val parent = current.parent
                if (parent == container) {
                    val holder = container.getChildViewHolder(current)
                    if (holder is CommentsRecyclerViewAdapter.CommentBaseViewHolder) {
                        return holder
                    }
                    break
                }
                current = parent as? View
            }
            return null
        }

        override fun play() {
            helper?.let { helper ->
                if (!isPlaying && isManuallyPaused) {
                    helper.play()
                    pause()
                    helper.volume = volume
                } else {
                    helper.play()
                }
                playPauseButton?.setImageDrawable(pauseDrawable)
                baseActivity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        override fun pause() {
            helper?.let { helper ->
                helper.pause()
                playPauseButton?.setImageDrawable(playDrawable)
                baseActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        override fun isPlaying(): Boolean {
            return helper?.isPlaying ?: false
        }

        override fun release() {
            if (helper != null) {
                helper?.release()
                helper = null
            }
            container = null
        }

        override fun wantsToPlay(): Boolean {
            val resources = baseActivity.resources
            val startAutoplayVisibleAreaOffset = if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
                baseActivity.getDefaultSharedPreferences().getInt(SharedPreferencesUtils.START_AUTOPLAY_VISIBLE_AREA_OFFSET_PORTRAIT, 75) / 100.0
            } else {
                baseActivity.getDefaultSharedPreferences().getInt(SharedPreferencesUtils.START_AUTOPLAY_VISIBLE_AREA_OFFSET_LANDSCAPE, 50) / 100.0
            }
            
            val parentHolder = container?.let { getCommentViewHolder(it) }
            val viewToEvaluate = parentHolder?.itemView ?: itemView
            
            return autoplayVideo && visibleAreaOffset(viewToEvaluate) >= startAutoplayVisibleAreaOffset
        }

        override fun getPlayerOrder(): Int {
            return absoluteAdapterPosition
        }

        private fun visibleAreaOffset(view: View): Float {
            if (view.parent == null) {
                return 0f
            }

            val drawRect = Rect()
            view.getDrawingRect(drawRect)
            val drawArea = drawRect.width() * drawRect.height()
            val visibleRect = Rect()
            val visible = view.getGlobalVisibleRect(visibleRect, Point())

            return if (visible && drawArea > 0) {
                visibleRect.height() * visibleRect.width() / drawArea.toFloat()
            } else {
                0f
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(mediaMetadata: MediaMetadata?, playbackPosition: Long)
    }
}
