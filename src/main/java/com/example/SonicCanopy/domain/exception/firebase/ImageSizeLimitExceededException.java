package com.example.SonicCanopy.domain.exception.firebase;

import org.slf4j.helpers.MessageFormatter;

public class ImageSizeLimitExceededException extends RuntimeException {
	
	public ImageSizeLimitExceededException(String message) {
        super(message);
    }
	
    public ImageSizeLimitExceededException(String message, Object... args) {
        super(MessageFormatter.arrayFormat(message, args).getMessage());
    }
}
