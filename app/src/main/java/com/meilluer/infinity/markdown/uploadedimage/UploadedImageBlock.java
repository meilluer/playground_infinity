package com.meilluer.infinity.markdown.uploadedimage;

import org.commonmark.node.CustomBlock;

import com.meilluer.infinity.thing.UploadedImage;

public class UploadedImageBlock extends CustomBlock {
    public UploadedImage uploadeImage;

    public UploadedImageBlock(UploadedImage uploadeImage) {
        this.uploadeImage = uploadeImage;
    }
}
