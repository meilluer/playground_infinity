package ml.docilealligator.infinityforreddit.subreddit;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SavedSubredditsManager {

    private static final String FILE_NAME = "saved_subreddits.json";
    private File file;
    private Gson gson;

    public SavedSubredditsManager(Context context) {
        file = new File(context.getFilesDir(), FILE_NAME);
        gson = new Gson();
    }

    public void saveSubreddit(SubredditData subredditData) {
        List<SubredditData> savedSubreddits = getSavedSubreddits();
        if (!savedSubreddits.contains(subredditData)) {
            savedSubreddits.add(subredditData);
            saveSubredditsToFile(savedSubreddits);
        }
    }

    public List<SubredditData> getSavedSubreddits() {
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<List<SubredditData>>() {}.getType();
            List<SubredditData> savedSubreddits = gson.fromJson(reader, type);
            return savedSubreddits != null ? savedSubreddits : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void removeSubreddit(SubredditData subredditData) {
        List<SubredditData> savedSubreddits = getSavedSubreddits();
        savedSubreddits.remove(subredditData);
        saveSubredditsToFile(savedSubreddits);
    }

    private void saveSubredditsToFile(List<SubredditData> subreddits) {
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(subreddits, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}