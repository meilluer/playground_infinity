package ml.docilealligator.infinityforreddit.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ml.docilealligator.infinityforreddit.R;

public class SettingsSearchFragment extends Fragment {

    public interface OnSearchResultClickListener {
        void onSearchResultClick(SearchResult result);
    }

    private RecyclerView recyclerView;
    private SearchResultAdapter adapter;
    private OnSearchResultClickListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_search, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_search_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchResultAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        if (getActivity() instanceof OnSearchResultClickListener) {
            listener = (OnSearchResultClickListener) getActivity();
        }
        return view;
    }

    public void displayResults(List<SearchResult> results) {
        adapter.setResults(results);
    }

    private class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

        private List<SearchResult> results;

        public SearchResultAdapter(List<SearchResult> results) {
            this.results = results;
        }

        public void setResults(List<SearchResult> results) {
            this.results = results;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SearchResult result = results.get(position);
            holder.text1.setText(result.getTitle());
            holder.text2.setText(result.getSummary());
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSearchResultClick(result);
                }
            });
        }

        @Override
        public int getItemCount() {
            return results.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public android.widget.TextView text1;
            public android.widget.TextView text2;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }

    public static class SearchResult {
        private final CharSequence title;
        private final CharSequence summary;
        private final String fragmentClass;
        private final String preferenceKey;

        public SearchResult(CharSequence title, CharSequence summary, String fragmentClass, String preferenceKey) {
            this.title = title;
            this.summary = summary;
            this.fragmentClass = fragmentClass;
            this.preferenceKey = preferenceKey;
        }

        public CharSequence getTitle() {
            return title;
        }

        public CharSequence getSummary() {
            return summary;
        }

        public String getFragmentClass() {
            return fragmentClass;
        }

        public String getPreferenceKey() {
            return preferenceKey;
        }
    }
}
