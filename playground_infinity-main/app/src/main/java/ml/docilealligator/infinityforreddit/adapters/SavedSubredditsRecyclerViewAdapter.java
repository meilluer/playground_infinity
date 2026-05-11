package ml.docilealligator.infinityforreddit.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import pl.droidsonroids.gif.GifImageView;

public class SavedSubredditsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final BaseActivity activity;
    private List<SubredditData> subreddits;
    private final RequestManager glide;
    private final CustomThemeWrapper customThemeWrapper;
    private final ItemLongClickListener itemLongClickListener;

    public SavedSubredditsRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper,
                                                    ItemLongClickListener itemLongClickListener) {
        this.activity = activity;
        glide = Glide.with(activity);
        this.customThemeWrapper = customThemeWrapper;
        this.subreddits = new ArrayList<>();
        this.itemLongClickListener = itemLongClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubredditViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subreddit_listing, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SubredditViewHolder) {
            SubredditData subredditData = subreddits.get(position);
            glide.load(subredditData.getIconUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(((SubredditViewHolder) holder).iconImageView);
            ((SubredditViewHolder) holder).subredditNameTextView.setText(subredditData.getName());
            ((SubredditViewHolder) holder).subscriberCountTextView.setText(activity.getString(R.string.subscribers_number_detail, subredditData.getNSubscribers()));
        }
    }

    @Override
    public int getItemCount() {
        return subreddits == null ? 0 : subreddits.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof SubredditViewHolder) {
            glide.clear(((SubredditViewHolder) holder).iconImageView);
        }
    }

    public void setSubreddits(List<SubredditData> subreddits) {
        this.subreddits = subreddits;
        notifyDataSetChanged();
    }

    class SubredditViewHolder extends RecyclerView.ViewHolder {

        GifImageView iconImageView;
        TextView subredditNameTextView;
        TextView subscriberCountTextView;
        ImageView subscribeImageView;

        public SubredditViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.subreddit_icon_gif_image_view_item_subreddit_listing);
            subredditNameTextView = itemView.findViewById(R.id.subreddit_name_text_view_item_subreddit_listing);
            subscriberCountTextView = itemView.findViewById(R.id.subscriber_count_text_view_item_subreddit_listing);
            subscribeImageView = itemView.findViewById(R.id.subscribe_image_view_item_subreddit_listing);

            // Hide elements not needed for this adapter
            subscribeImageView.setVisibility(View.GONE);
            // Assuming item_subreddit_listing might have a checkbox, hide it if present
            if (itemView.findViewById(R.id.checkbox_item_subreddit_listing) != null) {
                itemView.findViewById(R.id.checkbox_item_subreddit_listing).setVisibility(View.GONE);
            }

            subredditNameTextView.setTextColor(customThemeWrapper.getPrimaryTextColor());
            subscriberCountTextView.setTextColor(customThemeWrapper.getSecondaryTextColor());

            if (activity.typeface != null) {
                subredditNameTextView.setTypeface(activity.typeface);
                subscriberCountTextView.setTypeface(activity.typeface);
            }

            itemView.setOnClickListener(view -> {
                SubredditData subredditData = subreddits.get(getBindingAdapterPosition());
                Intent intent = new Intent(activity, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, subredditData.getName());
                activity.startActivity(intent);
            });

            itemView.setOnLongClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    itemLongClickListener.onItemLongClick(subreddits.get(position));
                    return true;
                }
                return false;
            });
        }
    }

    public interface ItemLongClickListener {
        void onItemLongClick(SubredditData subredditData);
    }
}
