package com.kolich.twitterfeed.exceptions.havalo;

import com.kolich.twitterfeed.exceptions.TwitterFeedException;

public final class ResourceNotFoundException extends TwitterFeedException {

	private static final long serialVersionUID = 2729923808652499511L;
	
	public ResourceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ResourceNotFoundException(Throwable cause) {
		super(cause);
	}
	
	public ResourceNotFoundException(String message) {
		super(message);
	}

}
