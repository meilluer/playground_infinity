package ml.docilealligator.infinityforreddit.archive;

import android.text.Html;
import android.text.TextUtils;

import java.util.ArrayList;
import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class ArcticShiftUserListingFetcher {
    private final ArcticShiftClient client = new ArcticShiftClient();
    private String lastDebugInfo = "";
    private String lastError = "";

    public String getLastDebugInfo() {
        return lastDebugInfo;
    }

    public String getLastError() {
        return lastError;
    }

    public ArrayList<Post> fetchPosts(String author) {
        ArrayList<Post> posts = new ArrayList<>();
        lastError = "";
        lastDebugInfo = "";
        try {
            java.util.List<ArcticShiftThing> things = client.searchPostsByAuthor(author);
            lastDebugInfo = client.lastDebugInfo;
            int skipped = 0;
            for (ArcticShiftThing thing : things) {
                Post post = toPost(thing);
                if (post != null) {
                    posts.add(post);
                } else {
                    skipped++;
                }
            }
            lastDebugInfo += "Converted: " + posts.size() + ", Skipped (null title/id): " + skipped + "\n";
        } catch (Exception e) {
            lastError = e.getClass().getSimpleName() + ": " + e.getMessage();
            lastDebugInfo = client.lastDebugInfo;
            e.printStackTrace();
        }
        return posts;
    }

    public ArrayList<Comment> fetchComments(String author) {
        ArrayList<Comment> comments = new ArrayList<>();
        lastError = "";
        lastDebugInfo = "";
        try {
            java.util.List<ArcticShiftThing> things = client.searchCommentsByAuthor(author);
            lastDebugInfo = client.lastDebugInfo;
            int skipped = 0;
            for (ArcticShiftThing thing : things) {
                Comment comment = toComment(thing);
                if (comment != null) {
                    comments.add(comment);
                } else {
                    skipped++;
                }
            }
            lastDebugInfo += "Converted: " + comments.size() + ", Skipped (null body/id): " + skipped + "\n";
        } catch (Exception e) {
            lastError = e.getClass().getSimpleName() + ": " + e.getMessage();
            lastDebugInfo = client.lastDebugInfo;
            e.printStackTrace();
        }
        return comments;
    }

    private Post toPost(ArcticShiftThing thing) {
        if (thing == null || TextUtils.isEmpty(thing.id) || TextUtils.isEmpty(thing.title)) {
            return null;
        }

        String subreddit = usefulOrDefault(thing.subreddit, "unknown");
        String author = usefulOrDefault(thing.author, "[deleted]");
        String permalink = relativePermalink(thing.permalink, "/r/" + subreddit + "/comments/" + thing.id + "/");
        Post post = new Post(thing.id, fullname("t3_", thing.id), subreddit, "r/" + subreddit,
                author, "", "", thing.createdUtc * 1000, thing.title, permalink, thing.score,
                Post.TEXT_TYPE, 0, thing.numComments, 100, usefulOrDefault(thing.linkFlairText, ""),
                false, thing.spoiler, thing.over18, false, true, false, false,
                false, false, null, null);

        String selfText = Utils.modifyMarkdown(Utils.trimTrailingWhitespace(usefulOrDefault(thing.selfText, "")));
        post.setSelfText(selfText);
        post.setSelfTextPlain(Html.fromHtml(selfText).toString());
        post.setSelfTextPlainTrimmed(trimPlainText(post.getSelfTextPlain()));
        if (!TextUtils.isEmpty(thing.url)) {
            post.setUrl(thing.url);
        }
        return post;
    }

    private Comment toComment(ArcticShiftThing thing) {
        if (thing == null || TextUtils.isEmpty(thing.id) || TextUtils.isEmpty(thing.body)) {
            return null;
        }

        String body = Utils.modifyMarkdown(Utils.trimTrailingWhitespace(thing.body));
        String subreddit = usefulOrDefault(thing.subreddit, "unknown");
        String author = usefulOrDefault(thing.author, "[deleted]");
        String linkId = normalizeFullname("t3_", thing.linkId);
        String parentId = normalizeFullname("t1_", thing.parentId);
        String permalink = relativePermalink(thing.permalink, "/r/" + subreddit + "/comments/" + stripPrefix(linkId) + "/_/" + thing.id + "/");

        return new Comment(thing.id, fullname("t1_", thing.id), author, "", "", "",
                null, thing.createdUtc * 1000, body, Html.fromHtml(body).toString(), linkId,
                subreddit, parentId, thing.score, Comment.VOTE_TYPE_NO_VOTE, false, null,
                permalink, 0, false, false, false, false, false, 0, null);
    }

    private String fullname(String prefix, String id) {
        if (TextUtils.isEmpty(id)) {
            return prefix;
        }
        return id.startsWith("t1_") || id.startsWith("t3_") ? id : prefix + id;
    }

    private String normalizeFullname(String prefix, String id) {
        if (TextUtils.isEmpty(id)) {
            return prefix;
        }
        return fullname(prefix, stripPrefix(id));
    }

    private String stripPrefix(String id) {
        if (!TextUtils.isEmpty(id) && id.length() > 3 && id.charAt(2) == '_') {
            return id.substring(3);
        }
        return id;
    }

    private String relativePermalink(String permalink, String fallback) {
        if (TextUtils.isEmpty(permalink)) {
            return fallback;
        }
        if (permalink.startsWith("https://www.reddit.com")) {
            return permalink.substring("https://www.reddit.com".length());
        }
        if (permalink.startsWith("http://www.reddit.com")) {
            return permalink.substring("http://www.reddit.com".length());
        }
        return permalink.startsWith("/") ? permalink : fallback;
    }

    private String usefulOrDefault(String value, String fallback) {
        return TextUtils.isEmpty(value) ? fallback : value;
    }

    private String trimPlainText(String text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }

        String trimmedText = Utils.trimTrailingWhitespace(text);
        return trimmedText.length() > 250 ? trimmedText.substring(0, 250) : trimmedText;
    }
}
