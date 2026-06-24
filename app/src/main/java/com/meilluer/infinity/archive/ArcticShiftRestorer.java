package com.meilluer.infinity.archive;

import android.text.Html;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.meilluer.infinity.comment.Comment;
import com.meilluer.infinity.post.Post;
import com.meilluer.infinity.utils.Utils;

public class ArcticShiftRestorer {
    private final ArcticShiftClient client = new ArcticShiftClient();

    public void restorePost(Post post) {
        try {
            restoreDeletedPost(post);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean restoreComment(Comment comment) {
        try {
            return restoreDeletedComment(comment);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean canRestoreComment(Comment comment) {
        return comment != null
                && comment.getPlaceholderType() == Comment.NOT_PLACEHOLDER
                && (shouldRestore(comment.getCommentRawText()) || shouldRestore(comment.getCommentMarkdown()));
    }

    private void restoreDeletedPost(Post post) throws Exception {
        if (post == null || !shouldRestore(post.getSelfTextPlain()) && !shouldRestore(post.getSelfText())) {
            return;
        }

        Map<String, ArcticShiftThing> archivedPosts = client.getPostsByIds(singleton(post.getId()));
        ArcticShiftThing archivedPost = archivedPosts.get(post.getId());
        if (archivedPost == null) {
            return;
        }

        if (isUseful(archivedPost.title) && shouldRestore(post.getTitle())) {
            post.setTitle(archivedPost.title);
        }
        if (isUseful(archivedPost.selfText)) {
            String restoredMarkdown = Utils.modifyMarkdown(Utils.trimTrailingWhitespace(archivedPost.selfText));
            post.setSelfText(restoredMarkdown);
            post.setSelfTextPlain(Html.fromHtml(restoredMarkdown).toString());
            post.setSelfTextPlainTrimmed(trimPlainText(post.getSelfTextPlain()));
        }
    }

    private boolean restoreDeletedComment(Comment comment) throws Exception {
        if (!canRestoreComment(comment)) {
            return false;
        }

        Map<String, ArcticShiftThing> archivedComments = client.getCommentsByIds(singleton(comment.getId()));
        ArcticShiftThing archivedComment = archivedComments.get(comment.getId());
        if (archivedComment == null || !isUseful(archivedComment.body)) {
            return false;
        }

        if (isUseful(archivedComment.author)) {
            comment.setAuthor(archivedComment.author);
        }

        String restoredMarkdown = Utils.modifyMarkdown(Utils.trimTrailingWhitespace(archivedComment.body));
        comment.setCommentMarkdown(restoredMarkdown);
        comment.setCommentRawText(Html.fromHtml(restoredMarkdown).toString());
        return true;
    }

    private boolean shouldRestore(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }

        String normalized = text.trim().toLowerCase();
        return normalized.equals("[deleted]") || normalized.equals("[removed]");
    }

    private boolean isUseful(String text) {
        return !TextUtils.isEmpty(text) && !shouldRestore(text);
    }

    private String trimPlainText(String text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }

        String trimmedText = Utils.trimTrailingWhitespace(text);
        return trimmedText.length() > 250 ? trimmedText.substring(0, 250) : trimmedText;
    }

    private List<String> singleton(String id) {
        ArrayList<String> ids = new ArrayList<>(1);
        ids.add(id);
        return ids;
    }
}
