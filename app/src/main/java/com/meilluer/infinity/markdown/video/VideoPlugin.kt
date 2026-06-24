package com.meilluer.infinity.markdown.video

import io.noties.markwon.AbstractMarkwonPlugin
import com.meilluer.infinity.markdown.imageandgif.ImageAndGifBlockParser
import com.meilluer.infinity.thing.MediaMetadata
import org.commonmark.parser.Parser

class VideoPlugin: AbstractMarkwonPlugin() {
    private val factory = VideoBlockParser.Factory

    override fun processMarkdown(markdown: String): String {
        return super.processMarkdown(markdown)
    }

    override fun configureParser(builder: Parser.Builder) {
        builder.customBlockParserFactory(factory)
    }

    fun setMediaMetadataMap(mediaMetadataMap: Map<String, MediaMetadata>?) {
        factory.setMediaMetadataMap(mediaMetadataMap)
    }
}
