package com.meilluer.infinity.comment;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import com.meilluer.infinity.thing.SortType;
import com.meilluer.infinity.account.Account;
import com.meilluer.infinity.apis.RedditAPI;
import com.meilluer.infinity.commentfilter.CommentFilter;
import com.meilluer.infinity.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchComment {
    public static void fetchComments(Executor executor, Handler handler, Retrofit retrofit,
                                     @Nullable String accessToken, @NonNull String accountName, String article,
                                     String commentId, SortType.Type sortType, String contextNumber,
                                     boolean expandChildren, CommentFilter commentFilter, int autoCollapseThreshold,
                                     FetchCommentListener fetchCommentListener) {
        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> comments;
        if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            if (commentId == null) {
                comments = api.getPostAndCommentsById(article, sortType, null);
            } else {
                comments = api.getPostAndCommentsSingleThreadById(article, commentId, sortType, contextNumber);
            }
        } else {
            if (commentId == null) {
                comments = api.getPostAndCommentsByIdOauth(article, sortType, null, APIUtils.getOAuthHeader(accessToken));
            } else {
                comments = api.getPostAndCommentsSingleThreadByIdOauth(article, commentId, sortType, contextNumber,
                        APIUtils.getOAuthHeader(accessToken));
            }
        }

        comments.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    ParseComment.parseComment(executor, handler, response.body(),
                            expandChildren, commentFilter, autoCollapseThreshold,
                            new ParseComment.ParseCommentListener() {
                                @Override
                                public void onParseCommentSuccess(ArrayList<Comment> topLevelComments,
                                                                  ArrayList<Comment> expandedComments,
                                                                  String parentId, ArrayList<String> moreChildrenIds) {
                                    fetchCommentListener.onFetchCommentSuccess(expandedComments, parentId,
                                            moreChildrenIds);
                                }

                                @Override
                                public void onParseCommentFailed() {
                                    fetchCommentListener.onFetchCommentFailed();
                                }
                            });
                } else {
                    fetchCommentListener.onFetchCommentFailed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchCommentListener.onFetchCommentFailed();
            }
        });
    }

    public static void fetchMoreComment(Executor executor, Handler handler, Retrofit retrofit,
                                        @Nullable String accessToken, @NonNull String accountName,
                                        ArrayList<String> allChildren,
                                        boolean expandChildren, int autoCollapseThreshold, String postFullName,
                                        SortType.Type sortType,
                                        FetchMoreCommentListener fetchMoreCommentListener) {
        if (allChildren == null) {
            return;
        }

        String childrenIds = String.join(",", allChildren);

        if (childrenIds.isEmpty()) {
            return;
        }

        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> moreComments;
        if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            moreComments = api.moreChildren(postFullName, childrenIds, sortType);
        } else {
            moreComments = api.moreChildrenOauth(postFullName, childrenIds,
                    sortType, APIUtils.getOAuthHeader(accessToken));
        }

        moreComments.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    ParseComment.parseMoreComment(executor, handler, response.body(),
                            expandChildren, autoCollapseThreshold, new ParseComment.ParseCommentListener() {
                                @Override
                                public void onParseCommentSuccess(ArrayList<Comment> topLevelComments,
                                                                  ArrayList<Comment> expandedComments,
                                                                  String parentId, ArrayList<String> moreChildrenIds) {
                                    fetchMoreCommentListener.onFetchMoreCommentSuccess(
                                            topLevelComments, expandedComments, moreChildrenIds);
                                }

                                @Override
                                public void onParseCommentFailed() {
                                    fetchMoreCommentListener.onFetchMoreCommentFailed();
                                }
                            });
                } else {
                    fetchMoreCommentListener.onFetchMoreCommentFailed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchMoreCommentListener.onFetchMoreCommentFailed();
            }
        });
    }

    public interface FetchCommentListener {
        void onFetchCommentSuccess(ArrayList<Comment> expandedComments, String parentId, ArrayList<String> children);

        void onFetchCommentFailed();
    }

    public interface FetchMoreCommentListener {
        void onFetchMoreCommentSuccess(ArrayList<Comment> topLevelComments,
                                       ArrayList<Comment> expandedComments,
                                       ArrayList<String> moreChildrenIds);

        void onFetchMoreCommentFailed();
    }
}
