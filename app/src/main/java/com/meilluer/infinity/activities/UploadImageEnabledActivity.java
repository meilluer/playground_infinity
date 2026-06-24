package com.meilluer.infinity.activities;

import com.meilluer.infinity.thing.UploadedImage;

public interface UploadImageEnabledActivity {
    void uploadImage();
    void captureImage();
    void insertImageUrl(UploadedImage uploadedImage);
}
