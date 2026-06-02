package ml.docilealligator.infinityforreddit.adapters;

import android.content.Intent;
import android.net.Uri;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.recycler.MarkwonAdapter;
import ml.docilealligator.infinityforreddit.subreddit.Rule;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.ViewImageOrGifActivity;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.SwipeLockInterface;
import ml.docilealligator.infinityforreddit.customviews.SwipeLockLinearLayoutManager;
import ml.docilealligator.infinityforreddit.customviews.slidr.widget.SliderPanel;
import ml.docilealligator.infinityforreddit.databinding.ItemRuleBinding;
import ml.docilealligator.infinityforreddit.markdown.EmoteCloseBracketInlineProcessor;
import ml.docilealligator.infinityforreddit.markdown.EmotePlugin;
import ml.docilealligator.infinityforreddit.markdown.EvenBetterLinkMovementMethod;
import ml.docilealligator.infinityforreddit.activities.ViewVideoActivity;
import ml.docilealligator.infinityforreddit.markdown.ImageAndGifEntry;
import ml.docilealligator.infinityforreddit.markdown.ImageAndGifPlugin;
import android.content.SharedPreferences;
import ml.docilealligator.infinityforreddit.markdown.MarkdownUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class RulesRecyclerViewAdapter extends RecyclerView.Adapter<RulesRecyclerViewAdapter.RuleViewHolder> {
    private final BaseActivity activity;
    private final EmoteCloseBracketInlineProcessor emoteCloseBracketInlineProcessor;
    private final EmotePlugin emotePlugin;
    private final ImageAndGifPlugin imageAndGifPlugin;
    private final ImageAndGifEntry imageAndGifEntry;
    private final Markwon markwon;
    @Nullable
    private final SliderPanel sliderPanel;
    private ArrayList<Rule> rules;
    private final int mPrimaryTextColor;

    public RulesRecyclerViewAdapter(@NonNull BaseActivity activity,
                                    @NonNull CustomThemeWrapper customThemeWrapper,
                                    @Nullable SliderPanel sliderPanel, String subredditName) {
        this.activity = activity;
        this.sliderPanel = sliderPanel;
        mPrimaryTextColor = customThemeWrapper.getPrimaryTextColor();
        int spoilerBackgroundColor = mPrimaryTextColor | 0xFF000000;
        MarkwonPlugin miscPlugin = new AbstractMarkwonPlugin() {
            @Override
            public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
                if (activity.typeface != null) {
                    textView.setTypeface(activity.typeface);
                }

                textView.setTextColor(mPrimaryTextColor);
            }

            @Override
            public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                builder.linkResolver((view, link) -> {
                    Intent intent = new Intent(activity, LinkResolverActivity.class);
                    Uri uri = Uri.parse(link);
                    intent.setData(uri);
                    activity.startActivity(intent);
                });
            }

            @Override
            public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                builder.linkColor(customThemeWrapper.getLinkColor());
            }
        };
        EvenBetterLinkMovementMethod.OnLinkLongClickListener onLinkLongClickListener = (textView, url) -> {
            if (!activity.isDestroyed() && !activity.isFinishing()) {
                UrlMenuBottomSheetFragment urlMenuBottomSheetFragment = UrlMenuBottomSheetFragment.newInstance(url);
                urlMenuBottomSheetFragment.show(activity.getSupportFragmentManager(), null);
            }
            return true;
        };
        emoteCloseBracketInlineProcessor = new EmoteCloseBracketInlineProcessor();
        emotePlugin = EmotePlugin.create(activity, SharedPreferencesUtils.EMBEDDED_MEDIA_ALL,
                mediaMetadata -> {
                    Intent imageIntent = new Intent(activity, ViewImageOrGifActivity.class);
                    if (mediaMetadata.isGIF) {
                        imageIntent.putExtra(ViewImageOrGifActivity.EXTRA_GIF_URL_KEY, mediaMetadata.original.url);
                    } else {
                        imageIntent.putExtra(ViewImageOrGifActivity.EXTRA_IMAGE_URL_KEY, mediaMetadata.original.url);
                    }
                    imageIntent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, subredditName);
                    imageIntent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, mediaMetadata.fileName);
                    activity.startActivity(imageIntent);
                });
        imageAndGifPlugin = new ImageAndGifPlugin();
        imageAndGifEntry = new ImageAndGifEntry(activity,
                Glide.with(activity), SharedPreferencesUtils.EMBEDDED_MEDIA_ALL,
                mediaMetadata -> {
                    boolean isVideo = !mediaMetadata.isGIF && ((mediaMetadata.e != null && (mediaMetadata.e.equalsIgnoreCase("video") || mediaMetadata.e.equalsIgnoreCase("RedditVideo")))
                            || (mediaMetadata.original != null && mediaMetadata.original.mp4Url != null));
                    Intent imageIntent;
                    if (isVideo) {
                        imageIntent = new Intent(activity, ViewVideoActivity.class);
                        imageIntent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_DIRECT);
                        SharedPreferences shp = activity.getDefaultSharedPreferences();
                        boolean dataSaving = false;
                        String dataSavingModeString = shp.getString(SharedPreferencesUtils.DATA_SAVING_MODE, SharedPreferencesUtils.DATA_SAVING_MODE_OFF);
                        if (dataSavingModeString.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ALWAYS)) {
                            dataSaving = true;
                        } else if (dataSavingModeString.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ONLY_ON_CELLULAR_DATA)) {
                            dataSaving = Utils.getConnectedNetwork(activity) == Utils.NETWORK_TYPE_CELLULAR;
                        }
                        String videoUrl = null;
                        if (dataSaving && mediaMetadata.downscaled != null && mediaMetadata.downscaled.mp4Url != null) {
                            videoUrl = mediaMetadata.downscaled.mp4Url;
                        } else if (mediaMetadata.original != null) {
                            videoUrl = mediaMetadata.original.mp4Url != null ? mediaMetadata.original.mp4Url : mediaMetadata.original.url;
                        }
                        imageIntent.setData(Uri.parse(videoUrl));
                    } else {
                        imageIntent = new Intent(activity, ViewImageOrGifActivity.class);
                        if (mediaMetadata.isGIF) {
                            imageIntent.putExtra(ViewImageOrGifActivity.EXTRA_GIF_URL_KEY, mediaMetadata.original.url);
                        } else {
                            imageIntent.putExtra(ViewImageOrGifActivity.EXTRA_IMAGE_URL_KEY, mediaMetadata.original.url);
                        }
                    }
                    imageIntent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, subredditName);
                    imageIntent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, mediaMetadata.fileName);
                    activity.startActivity(imageIntent);
                });
        markwon = MarkdownUtils.createFullRedditMarkwon(activity,
                miscPlugin, emoteCloseBracketInlineProcessor, emotePlugin, imageAndGifPlugin, mPrimaryTextColor,
                spoilerBackgroundColor, onLinkLongClickListener);
    }

    @NonNull
    @Override
    public RuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RuleViewHolder(ItemRuleBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RuleViewHolder holder, int position) {
        Rule rule = rules.get(holder.getBindingAdapterPosition());
        holder.binding.shortNameTextViewItemRule.setText(rule.getShortName());
        if (rule.getDescriptionHtml() == null) {
            holder.binding.descriptionMarkwonViewItemRule.setVisibility(View.GONE);
        } else {
            holder.markwonAdapter.setMarkdown(markwon, rule.getDescriptionHtml());
            //noinspection NotifyDatasetChanged
            holder.markwonAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return rules == null ? 0 : rules.size();
    }

    @Override
    public void onViewRecycled(@NonNull RuleViewHolder holder) {
        super.onViewRecycled(holder);
        holder.binding.descriptionMarkwonViewItemRule.setVisibility(View.VISIBLE);
    }

    public void changeDataset(ArrayList<Rule> rules) {
        this.rules = rules;
        notifyDataSetChanged();
    }

    public void setDataSavingMode(boolean dataSavingMode) {
        emotePlugin.setDataSavingMode(dataSavingMode);
        imageAndGifEntry.setDataSavingMode(dataSavingMode);
    }

    class RuleViewHolder extends RecyclerView.ViewHolder {
        ItemRuleBinding binding;
        @NonNull
        final MarkwonAdapter markwonAdapter;

        RuleViewHolder(@NonNull ItemRuleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.shortNameTextViewItemRule.setTextColor(mPrimaryTextColor);

            if (activity.typeface != null) {
                binding.shortNameTextViewItemRule.setTypeface(activity.typeface);
            }
            markwonAdapter = MarkdownUtils.createCustomTablesAndImagesAdapter(activity, imageAndGifEntry);
            SwipeLockLinearLayoutManager swipeLockLinearLayoutManager = new SwipeLockLinearLayoutManager(activity,
                    new SwipeLockInterface() {
                @Override
                public void lockSwipe() {
                    if (sliderPanel != null) {
                        sliderPanel.lock();
                    }
                }

                @Override
                public void unlockSwipe() {
                    if (sliderPanel != null) {
                        sliderPanel.unlock();
                    }
                }
            });
            binding.descriptionMarkwonViewItemRule.setLayoutManager(swipeLockLinearLayoutManager);
            binding.descriptionMarkwonViewItemRule.setAdapter(markwonAdapter);
        }
    }
}
