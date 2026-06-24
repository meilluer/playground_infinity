package com.meilluer.infinity.markdown.imageandgif;

import org.commonmark.node.CustomBlock;

import com.meilluer.infinity.thing.MediaMetadata;

public class ImageAndGifBlock extends CustomBlock {
    public MediaMetadata mediaMetadata;

    public ImageAndGifBlock(MediaMetadata mediaMetadata) {
        this.mediaMetadata = mediaMetadata;
    }
}
