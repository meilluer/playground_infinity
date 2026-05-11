package ml.docilealligator.infinityforreddit.adapters;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import ml.docilealligator.infinityforreddit.databinding.ItemOfflineSubredditBinding;
import ml.docilealligator.infinityforreddit.offline.OfflineSubreddit;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class OfflineSubredditAdapter extends ListAdapter<OfflineSubreddit, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_ADD = 1;

    private final Context context;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(OfflineSubreddit offlineSubreddit);
        void onDeleteClick(OfflineSubreddit offlineSubreddit);
        void onAddClick();
    }

    public OfflineSubredditAdapter(Context context, OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<OfflineSubreddit>() {
            @Override
            public boolean areItemsTheSame(@NonNull OfflineSubreddit oldItem, @NonNull OfflineSubreddit newItem) {
                return oldItem.getName().equals(newItem.getName());
            }

            @Override
            public boolean areContentsTheSame(@NonNull OfflineSubreddit oldItem, @NonNull OfflineSubreddit newItem) {
                return oldItem.getTotalSize() == newItem.getTotalSize() 
                        && oldItem.getDownloadTime() == newItem.getDownloadTime()
                        && oldItem.getPostLimit() == newItem.getPostLimit();
            }
        });
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return VIEW_TYPE_ADD;
        }
        return VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ADD) {
            View view = LayoutInflater.from(parent.getContext()).inflate(ml.docilealligator.infinityforreddit.R.layout.item_offline_add_button, parent, false);
            return new AddButtonViewHolder(view);
        }
        ItemOfflineSubredditBinding binding = ItemOfflineSubredditBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            ((ViewHolder) holder).bind(getItem(position));
        } else if (holder instanceof AddButtonViewHolder) {
            ((AddButtonViewHolder) holder).bind();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemOfflineSubredditBinding binding;

        public ViewHolder(ItemOfflineSubredditBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(OfflineSubreddit item) {
            binding.textViewSubredditName.setText(item.getName());
            
            String size = Formatter.formatFileSize(context, item.getTotalSize());
            String time = Utils.getElapsedTime(context, item.getDownloadTime());
            
            String info = item.getPostLimit() + " posts • " + size + " • " + time + " • Downloaded";
            binding.textViewInfo.setText(info);

            binding.getRoot().setOnClickListener(v -> listener.onItemClick(item));
            binding.buttonDelete.setOnClickListener(v -> listener.onDeleteClick(item));
        }
    }

    class AddButtonViewHolder extends RecyclerView.ViewHolder {
        public AddButtonViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind() {
            itemView.findViewById(ml.docilealligator.infinityforreddit.R.id.button_add_offline).setOnClickListener(v -> listener.onAddClick());
        }
    }
}
