package ml.docilealligator.infinityforreddit.subreddit;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class SavedSubredditsManager {

    private static final String FILE_NAME = "saved_subreddits.json";
    private File file;
    private Gson gson;

    public SavedSubredditsManager(Context context) {
        file = new File(context.getFilesDir(), FILE_NAME);
        gson = new Gson();
    }

    public void saveSubreddit(SubredditData subredditData) {
        Set<SubredditData> savedSubreddits = getSavedSubreddits();
        savedSubreddits.add(subredditData);
        saveSubredditsToFile(savedSubreddits);
    }

    public Set<SubredditData> getSavedSubreddits() {
        if (!file.exists()) {
            return new HashSet<>();
        }
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Set<SubredditData>>() {}.getType();
            Set<SubredditData> savedSubreddits = gson.fromJson(reader, type);
            return savedSubreddits != null ? savedSubreddits : new HashSet<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new HashSet<>();
        }
    }

    public void removeSubreddit(SubredditData subredditData) {
        Set<SubredditData> savedSubreddits = getSavedSubreddits();
        savedSubreddits.remove(subredditData);
        saveSubredditsToFile(savedSubreddits);
    }

    private void saveSubredditsToFile(Set<SubredditData> subreddits) {
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(subreddits, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}