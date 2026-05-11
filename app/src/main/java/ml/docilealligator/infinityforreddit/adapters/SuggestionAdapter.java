package ml.docilealligator.infinityforreddit.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.utils.Suggestion;

public class SuggestionAdapter extends ArrayAdapter<Suggestion> {

    private final Context context;
    private final List<Suggestion> suggestions;

    public SuggestionAdapter(Context context, List<Suggestion> suggestions) {
        super(context, R.layout.suggestion_item, suggestions);
        this.context = context;
        this.suggestions = suggestions;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View rowView = inflater.inflate(R.layout.suggestion_item, parent, false);

        ImageView imageView = rowView.findViewById(R.id.suggestion_icon);
        TextView textView = rowView.findViewById(R.id.suggestion_name);

        textView.setText(suggestions.get(position).getName());
        Glide.with(context)
                .load(suggestions.get(position).getIconUrl())
                .error(R.drawable.subreddit_default_icon)
                .into(imageView);

        return rowView;
    }
}
