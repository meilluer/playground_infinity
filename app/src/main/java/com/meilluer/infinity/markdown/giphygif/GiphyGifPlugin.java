package com.meilluer.infinity.markdown.giphygif;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.commonmark.parser.Parser;

import java.util.List;

import io.noties.markwon.AbstractMarkwonPlugin;
import com.meilluer.infinity.thing.GiphyGif;
import com.meilluer.infinity.thing.UploadedImage;

public class GiphyGifPlugin extends AbstractMarkwonPlugin {
    private final GiphyGifBlockParser.Factory factory;

    public GiphyGifPlugin(@Nullable GiphyGif giphyGif, @Nullable List<UploadedImage> uploadedImages) {
        this.factory = new GiphyGifBlockParser.Factory(giphyGif, uploadedImages);
    }

    @NonNull
    @Override
    public String processMarkdown(@NonNull String markdown) {
        return super.processMarkdown(markdown);
    }

    @Override
    public void configureParser(@NonNull Parser.Builder builder) {
        builder.customBlockParserFactory(factory);
    }
}
