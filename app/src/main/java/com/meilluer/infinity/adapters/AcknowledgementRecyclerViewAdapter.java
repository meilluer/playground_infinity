package com.meilluer.infinity.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.meilluer.infinity.activities.LinkResolverActivity;
import com.meilluer.infinity.activities.SettingsActivity;
import com.meilluer.infinity.databinding.ItemAcknowledgementBinding;
import com.meilluer.infinity.settings.Acknowledgement;

public class AcknowledgementRecyclerViewAdapter extends RecyclerView.Adapter<AcknowledgementRecyclerViewAdapter.AcknowledgementViewHolder> {
    private final ArrayList<Acknowledgement> acknowledgements;
    private final SettingsActivity activity;

    public AcknowledgementRecyclerViewAdapter(SettingsActivity activity, ArrayList<Acknowledgement> acknowledgements) {
        this.activity = activity;
        this.acknowledgements = acknowledgements;
    }

    @NonNull
    @Override
    public AcknowledgementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AcknowledgementViewHolder(ItemAcknowledgementBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AcknowledgementViewHolder holder, int position) {
        Acknowledgement acknowledgement = acknowledgements.get(holder.getBindingAdapterPosition());
        if (acknowledgement != null) {
            holder.binding.nameTextViewItemAcknowledgement.setText(acknowledgement.getName());
            holder.binding.introductionTextViewItemAcknowledgement.setText(acknowledgement.getIntroduction());
            holder.itemView.setOnClickListener(view -> {
                if (activity != null) {
                    Intent intent = new Intent(activity, LinkResolverActivity.class);
                    intent.setData(acknowledgement.getLink());
                    activity.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return acknowledgements == null ? 0 : acknowledgements.size();
    }

    class AcknowledgementViewHolder extends RecyclerView.ViewHolder {
        ItemAcknowledgementBinding binding;

        AcknowledgementViewHolder(@NonNull ItemAcknowledgementBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.nameTextViewItemAcknowledgement.setTextColor(activity.customThemeWrapper.getPrimaryTextColor());
            binding.introductionTextViewItemAcknowledgement.setTextColor(activity.customThemeWrapper.getSecondaryTextColor());

            if (activity.typeface != null) {
                binding.nameTextViewItemAcknowledgement.setTypeface(activity.typeface);
                binding.introductionTextViewItemAcknowledgement.setTypeface(activity.typeface);
            }
        }
    }
}
