package com.meilluer.infinity.adapters;

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
import com.meilluer.infinity.subreddit.Rule;
import com.meilluer.infinity.activities.BaseActivity;
import com.meilluer.infinity.activities.LinkResolverActivity;
import com.meilluer.infinity.activities.ViewImageOrGifActivity;
import com.meilluer.infinity.bottomsheetfragments.UrlMenuBottomSheetFragment;
import com.meilluer.infinity.customtheme.CustomThemeWrapper;
import com.meilluer.infinity.customviews.SwipeLockInterface;
import com.meilluer.infinity.customviews.SwipeLockLinearLayoutManager;
import com.meilluer.infinity.customviews.slidr.widget.SliderPanel;
import com.meilluer.infinity.databinding.ItemRuleBinding;
import com.meilluer.infinity.markdown.emote.EmoteCloseBracketInlineProcessor;
import com.meilluer.infinity.markdown.emote.EmotePlugin;
import com.meilluer.infinity.markdown.EvenBetterLinkMovementMethod;
import com.meilluer.infinity.markdown.imageandgif.ImageAndGifEntry;
import com.meilluer.infinity.markdown.imageandgif.ImageAndGifPlugin;
import com.meilluer.infinity.markdown.MarkdownUtils;
import com.meilluer.infinity.utils.SharedPreferencesUtils;

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
                });
        imageAndGifPlugin = new ImageAndGifPlugin();
        imageAndGifEntry = new ImageAndGifEntry(activity,
                Glide.with(activity), SharedPreferencesUtils.EMBEDDED_MEDIA_ALL,
                mediaMetadata -> {
                    Intent imageIntent = new Intent(activity, ViewImageOrGifActivity.class);
                    if (mediaMetadata.isGIF) {
                        imageIntent.putExtra(ViewImageOrGifActivity.EXTRA_GIF_URL_KEY, mediaMetadata.original.url);
                    } else {
                        imageIntent.putExtra(ViewImageOrGifActivity.EXTRA_IMAGE_URL_KEY, mediaMetadata.original.url);
                    }
                    imageIntent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, subredditName);
                    imageIntent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, mediaMetadata.fileName);
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
