package com.meilluer.infinity.markdown.giphygif;

import org.commonmark.node.CustomBlock;

import com.meilluer.infinity.thing.GiphyGif;

public class GiphyGifBlock extends CustomBlock {
    public GiphyGif giphyGif;

    public GiphyGifBlock(GiphyGif giphyGif) {
        this.giphyGif = giphyGif;
    }
}
