package org.unicon.lex.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;

import java.io.IOException;
import java.net.URL;

import static java.lang.String.format;

public class MediaUtil {

    private static final Logger LOGGER = LogManager.getLogger(MediaUtil.class.getName());

    private static final String IMG_ELEMENT = "<img width=\"300\" height=\"200\" src=\"%s\">";

    private static final String AUDIO_ELEMENT = "<audio autoplay controls><source src=\"%s\" type=\"%s\"></audio>";

    private static final String VIDEO_ELEMENT = "<video width=\"300\" height=\"200\" autoplay controls><source src=\"%s\" type=\"%s\"></video>";

    private MediaUtil() {
    }

    public static String createMediaElement(String url, MediaUtil.Type typeFilter) throws IOException {
        String mimeType = new Tika().detect(new URL(url));
        LOGGER.debug("url: {} mimeType: {}", url, mimeType);
        String[] mimeTypeParts = mimeType.split("/");
        String type = mimeTypeParts[0];
        String format = mimeTypeParts[1];

        String htmlElement = null;
        switch (type) {
            case ("image"):
                htmlElement = format(IMG_ELEMENT, url);
                break;
            case ("audio"):
                htmlElement = format(AUDIO_ELEMENT, url, "vorbis".equals(format) ? "audio/ogg" : mimeType);
                break;
            case ("video"):
                htmlElement = format(VIDEO_ELEMENT, url, mimeType);
                break;
            default:
                htmlElement = null;
        }

        if (typeFilter != null && !typeFilter.name().toLowerCase().equals(type)) {
            htmlElement = null;
        }
        return htmlElement;
    }

    public static String createMediaElement(String url) throws IOException {
        return createMediaElement(url, null);
    }

    public enum Type {
        IMAGE, AUDIO, VIDEO;
    }
}
