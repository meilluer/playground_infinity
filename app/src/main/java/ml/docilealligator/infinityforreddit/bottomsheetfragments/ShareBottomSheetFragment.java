package ml.docilealligator.infinityforreddit.bottomsheetfragments;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.AsyncTask;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SaveMemoryCenterInisdeDownsampleStrategy;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentShareLinkBottomSheetBinding;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.utils.ShareScreenshotUtilsKt;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShareBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {
    public static final String EXTRA_POST_LINK = "EPL";
    public static final String EXTRA_MEDIA_LINK = "EML";
    public static final String EXTRA_MEDIA_TYPE = "EMT";
    public static final String EXTRA_POST = "EP";

    private BaseActivity activity;

    public ShareBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentShareLinkBottomSheetBinding binding = FragmentShareLinkBottomSheetBinding.inflate(inflater, container, false);

        String postLink = getArguments().getString(EXTRA_POST_LINK);
        String mediaLink = getArguments().containsKey(EXTRA_MEDIA_LINK) ? getArguments().getString(EXTRA_MEDIA_LINK) : null;
        Post post = getArguments().getParcelable(EXTRA_POST);

        binding.postLinkTextViewShareLinkBottomSheetFragment.setText(postLink);

        if (mediaLink != null) {
            binding.mediaLinkTextViewShareLinkBottomSheetFragment.setVisibility(View.VISIBLE);
            binding.shareMediaLinkTextViewShareLinkBottomSheetFragment.setVisibility(View.VISIBLE);
            binding.copyMediaLinkTextViewShareLinkBottomSheetFragment.setVisibility(View.VISIBLE);

            binding.mediaLinkTextViewShareLinkBottomSheetFragment.setText(mediaLink);

            int mediaType = getArguments().getInt(EXTRA_MEDIA_TYPE);
            switch (mediaType) {
                case Post.IMAGE_TYPE:
                    binding.shareMediaLinkTextViewShareLinkBottomSheetFragment.setText(R.string.share_image_link);
                    binding.copyMediaLinkTextViewShareLinkBottomSheetFragment.setText(R.string.copy_image_link);
                    binding.shareMediaLinkTextViewShareLinkBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(
                            activity.getDrawable(R.drawable.ic_image_day_night_24dp), null, null, null);
                    break;
                case Post.GIF_TYPE:
                    binding.shareMediaLinkTextViewShareLinkBottomSheetFragment.setText(R.string.share_gif_link);
                    binding.copyMediaLinkTextViewShareLinkBottomSheetFragment.setText(R.string.copy_gif_link);
                    binding.shareMediaLinkTextViewShareLinkBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(
                            activity.getDrawable(R.drawable.ic_image_day_night_24dp), null, null, null);
                    break;
                case Post.VIDEO_TYPE:
                    binding.shareMediaLinkTextViewShareLinkBottomSheetFragment.setText(R.string.share_video_link);
                    binding.copyMediaLinkTextViewShareLinkBottomSheetFragment.setText(R.string.copy_video_link);
                    binding.shareMediaLinkTextViewShareLinkBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(
                            activity.getDrawable(R.drawable.ic_video_day_night_24dp), null, null, null);
                    break;
                case Post.LINK_TYPE:
                case Post.NO_PREVIEW_LINK_TYPE:
                    binding.shareMediaLinkTextViewShareLinkBottomSheetFragment.setText(R.string.share_link);
                    binding.copyMediaLinkTextViewShareLinkBottomSheetFragment.setText(R.string.copy_link);
                    break;
            }

            binding.shareMediaLinkTextViewShareLinkBottomSheetFragment.setOnClickListener(view -> {
                shareLink(mediaLink);
                dismiss();
            });
            binding.copyMediaLinkTextViewShareLinkBottomSheetFragment.setOnClickListener(view -> {
                copyLink(mediaLink);
                dismiss();
            });
        }

        binding.sharePostLinkTextViewShareLinkBottomSheetFragment.setOnClickListener(view -> {
            shareLink(postLink);
            dismiss();
        });
        binding.copyPostLinkTextViewShareLinkBottomSheetFragment.setOnClickListener(view -> {
            copyLink(postLink);
            dismiss();
        });

        if (post != null) {
            binding.shareAsImageTextViewShareLinkBottomSheetFragment.setVisibility(View.VISIBLE);

            binding.shareAsImageTextViewShareLinkBottomSheetFragment.setOnClickListener(view -> {
                ShareScreenshotUtilsKt.sharePostAsScreenshot(
                        activity,
                        post,
                        activity.customThemeWrapper,
                        activity.getResources().getConfiguration().locale,
                        activity.getDefaultSharedPreferences().getString(SharedPreferencesUtils.TIME_FORMAT_KEY,
                                SharedPreferencesUtils.TIME_FORMAT_DEFAULT_VALUE),
                        new SaveMemoryCenterInisdeDownsampleStrategy(
                                Integer.parseInt(activity.getDefaultSharedPreferences()
                                        .getString(SharedPreferencesUtils.POST_FEED_MAX_RESOLUTION, "5000000"))));
                dismiss();
            });
        }

        if (post != null && post.getPostType() == Post.VIDEO_TYPE) {
            binding.shareVideoButton.setVisibility(View.VISIBLE);
            binding.shareVideoButton.setOnClickListener(view -> {
                new ShareVideoTask(post).execute();
                dismiss();
            });
        }

        if (activity.typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), activity.typeface);
        }
        return binding.getRoot();
    }

    private void shareLink(String link) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, link);
            activity.startActivity(Intent.createChooser(intent, getString(R.string.share)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.no_activity_found_for_share, Toast.LENGTH_SHORT).show();
        }
    }

    private void copyLink(String link) {
        activity.copyLink(link);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (BaseActivity) context;
    }

    private class ShareVideoTask extends AsyncTask<Void, Void, Uri> {
        private Post post;

        ShareVideoTask(Post post) {
            this.post = post;
        }

        @Override
        protected Uri doInBackground(Void... voids) {
            File externalCacheDirectory = activity.getExternalCacheDir();
            if (externalCacheDirectory == null) {
                return null;
            }

            String fileNameWithoutExtension = post.getId();
            String videoFilePath = externalCacheDirectory.getAbsolutePath() + "/" + fileNameWithoutExtension + "-video.mp4";
            String audioFilePath = externalCacheDirectory.getAbsolutePath() + "/" + fileNameWithoutExtension + "-audio.mp4";
            String outputFilePath = externalCacheDirectory.getAbsolutePath() + "/" + fileNameWithoutExtension + "-merged.mp4";

            try {
                // Download video
                HttpURLConnection videoConnection = (HttpURLConnection) new URL(post.getVideoDownloadUrl()).openConnection();
                videoConnection.connect();
                InputStream videoInput = videoConnection.getInputStream();
                FileOutputStream videoOutput = new FileOutputStream(videoFilePath);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = videoInput.read(buffer)) != -1) {
                    videoOutput.write(buffer, 0, bytesRead);
                }
                videoOutput.close();
                videoInput.close();

                // Download audio (if available)
                String audioUrl = getAudioUrl(post.getVideoDownloadUrl());
                if (audioUrl != null) {
                    HttpURLConnection audioConnection = (HttpURLConnection) new URL(audioUrl).openConnection();
                    audioConnection.connect();
                    InputStream audioInput = audioConnection.getInputStream();
                    FileOutputStream audioOutput = new FileOutputStream(audioFilePath);
                    while ((bytesRead = audioInput.read(buffer)) != -1) {
                        audioOutput.write(buffer, 0, bytesRead);
                    }
                    audioOutput.close();
                    audioInput.close();

                    // Mux video and audio
                    if (muxVideoAndAudio(videoFilePath, audioFilePath, outputFilePath)) {
                        return FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", new File(outputFilePath));
                    }
                } else {
                    // If no audio, just return the video file
                    return FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", new File(videoFilePath));
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Clean up temporary files
                new File(videoFilePath).delete();
                new File(audioFilePath).delete();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Uri videoUri) {
            if (videoUri != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("video/mp4");
                shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                activity.startActivity(Intent.createChooser(shareIntent, "Share Video"));
            } else {
                Toast.makeText(activity, "Failed to prepare video for sharing", Toast.LENGTH_SHORT).show();
            }
        }

        private String getAudioUrl(String videoUrl) {
            // This is a simplified way to get audio URL. In a real scenario, you'd parse DASH manifest.
            // For Reddit videos, audio often has a similar URL structure.
            // Example: video.mp4 -> audio.mp4
            String audioUrl = null;
            int lastSlash = videoUrl.lastIndexOf('/');
            if (lastSlash != -1) {
                String baseUrl = videoUrl.substring(0, lastSlash);
                // Try common audio suffixes
                String[] possibleAudioUrlSuffices = new String[]{"/DASH_audio.mp4", "/DASH_AUDIO_128.mp4", "/audio.mp4"};
                for (String suffix : possibleAudioUrlSuffices) {
                    try {
                        HttpURLConnection connection = (HttpURLConnection) new URL(baseUrl + suffix).openConnection();
                        connection.setRequestMethod("HEAD"); // Check if URL exists
                        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            audioUrl = baseUrl + suffix;
                            break;
                        }
                    } catch (IOException e) {
                        // Ignore and try next suffix
                    }
                }
            }
            return audioUrl;
        }

        private boolean muxVideoAndAudio(String videoFilePath, String audioFilePath, String outputFilePath) {
            try {
                File file = new File(outputFilePath);
                file.createNewFile();
                MediaExtractor videoExtractor = new MediaExtractor();
                videoExtractor.setDataSource(videoFilePath);
                MediaMuxer muxer = new MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

                videoExtractor.selectTrack(0);
                MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
                int videoTrack = muxer.addTrack(videoFormat);

                boolean sawEOS = false;
                int offset = 100;
                int sampleSize = 4096 * 1024;
                ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
                ByteBuffer audioBuf = ByteBuffer.allocate(sampleSize);
                MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();

                videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

                MediaExtractor audioExtractor = new MediaExtractor();
                MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
                int audioTrack = -1;
                if (audioFilePath != null) {
                    audioExtractor.setDataSource(audioFilePath);
                    audioExtractor.selectTrack(0);
                    MediaFormat audioFormat = audioExtractor.getTrackFormat(0);
                    audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                    audioTrack = muxer.addTrack(audioFormat);
                }

                muxer.start();

                while (!sawEOS) {
                    videoBufferInfo.offset = offset;
                    videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);

                    if (videoBufferInfo.size < 0) {
                        sawEOS = true;
                        videoBufferInfo.size = 0;
                    } else {
                        videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                        videoBufferInfo.flags = videoExtractor.getSampleFlags();
                        muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
                        videoExtractor.advance();
                    }
                }

                if (audioFilePath != null) {
                    boolean sawEOS2 = false;
                    while (!sawEOS2) {
                        audioBufferInfo.offset = offset;
                        audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);

                        if (audioBufferInfo.size < 0) {
                            sawEOS2 = true;
                            audioBufferInfo.size = 0;
                        } else {
                            audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                            audioBufferInfo.flags = audioExtractor.getSampleFlags();
                            muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
                            audioExtractor.advance();
                        }
                    }
                }

                muxer.stop();
                muxer.release();
                return true;
            } catch (IllegalArgumentException | IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
