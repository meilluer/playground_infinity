package ml.docilealligator.infinityforreddit.archive;

import android.text.Html;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class ArcticShiftRestorer {
    private final ArcticShiftClient client = new ArcticShiftClient();

    public void restore(Post post, List<Comment> comments) {
        try {
            restorePost(post);
            restoreComments(comments);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restorePost(Post post) throws Exception {
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

    private void restoreComments(List<Comment> comments) throws Exception {
        ArrayList<Comment> deletedComments = new ArrayList<>();
        collectDeletedComments(comments, deletedComments);
        if (deletedComments.isEmpty()) {
            return;
        }

        ArrayList<String> ids = new ArrayList<>();
        for (Comment comment : deletedComments) {
            ids.add(comment.getId());
        }

        Map<String, ArcticShiftThing> archivedComments = client.getCommentsByIds(ids);
        for (Comment comment : deletedComments) {
            ArcticShiftThing archivedComment = archivedComments.get(comment.getId());
            if (archivedComment == null || !isUseful(archivedComment.body)) {
                continue;
            }

            String restoredMarkdown = Utils.modifyMarkdown(Utils.trimTrailingWhitespace(archivedComment.body));
            comment.setCommentMarkdown(restoredMarkdown);
            comment.setCommentRawText(Html.fromHtml(restoredMarkdown).toString());
        }
    }

    private void collectDeletedComments(List<Comment> comments, List<Comment> deletedComments) {
        if (comments == null) {
            return;
        }

        LinkedHashSet<String> seenIds = new LinkedHashSet<>();
        collectDeletedComments(comments, deletedComments, seenIds);
    }

    private void collectDeletedComments(List<Comment> comments, List<Comment> deletedComments, LinkedHashSet<String> seenIds) {
        for (Comment comment : comments) {
            if (comment == null || comment.getPlaceholderType() != Comment.NOT_PLACEHOLDER) {
                continue;
            }

            if (seenIds.add(comment.getId()) && shouldRestore(comment.getCommentRawText())) {
                deletedComments.add(comment);
            }
            collectDeletedComments(comment.getChildren(), deletedComments, seenIds);
        }
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
