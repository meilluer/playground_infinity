package ml.docilealligator.infinityforreddit.utils;

public class Suggestion {
    private String name;
    private String iconUrl;

    public Suggestion(String name, String iconUrl) {
        this.name = name;
        this.iconUrl = iconUrl;
    }

    public String getName() {
        return name;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}
