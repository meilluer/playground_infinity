package ml.docilealligator.infinityforreddit.archive;

class ArcticShiftThing {
    final String id;
    final String author;
    final String title;
    final String selfText;
    final String body;
    final String subreddit;
    final String permalink;
    final String url;
    final String linkFlairText;
    final String linkId;
    final String parentId;
    final long createdUtc;
    final int score;
    final int numComments;
    final boolean over18;
    final boolean spoiler;

    ArcticShiftThing(String id, String author, String title, String selfText, String body, String subreddit,
                     String permalink, String url, String linkFlairText, String linkId, String parentId,
                     long createdUtc, int score, int numComments, boolean over18, boolean spoiler) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.selfText = selfText;
        this.body = body;
        this.subreddit = subreddit;
        this.permalink = permalink;
        this.url = url;
        this.linkFlairText = linkFlairText;
        this.linkId = linkId;
        this.parentId = parentId;
        this.createdUtc = createdUtc;
        this.score = score;
        this.numComments = numComments;
        this.over18 = over18;
        this.spoiler = spoiler;
    }
}
