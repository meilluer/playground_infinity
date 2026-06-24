package com.meilluer.infinity.events;

import java.util.ArrayList;

import com.meilluer.infinity.readpost.ReadPostsListInterface;
import com.meilluer.infinity.thing.SortType;
import com.meilluer.infinity.post.Post;
import com.meilluer.infinity.postfilter.PostFilter;

public class ProvidePostListToViewPostDetailActivityEvent {
    public long postFragmentId;
    public ArrayList<Post> posts;
    public int postType;
    public String subredditName;
    public String concatenatedSubredditNames;
    public String username;
    public String userWhere;
    public String multiPath;
    public String query;
    public String trendingSource;
    public PostFilter postFilter;
    public SortType sortType;
    public ReadPostsListInterface readPostsList;

    public ProvidePostListToViewPostDetailActivityEvent(long postFragmentId, ArrayList<Post> posts, int postType,
                                                        String subredditName, String concatenatedSubredditNames,
                                                        String username, String userWhere,
                                                        String multiPath, String query, String trendingSource,
                                                        PostFilter postFilter, SortType sortType, ReadPostsListInterface readPostsList) {
        this.postFragmentId = postFragmentId;
        this.posts = posts;
        this.postType = postType;
        this.subredditName = subredditName;
        this.concatenatedSubredditNames = concatenatedSubredditNames;
        this.username = username;
        this.userWhere = userWhere;
        this.multiPath = multiPath;
        this.query = query;
        this.trendingSource = trendingSource;
        this.postFilter = postFilter;
        this.sortType = sortType;
        this.readPostsList = readPostsList;
    }
}
