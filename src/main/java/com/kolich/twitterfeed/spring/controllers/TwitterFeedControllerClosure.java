package com.kolich.twitterfeed.spring.controllers;

import org.slf4j.Logger;

import com.kolich.spring.controllers.KolichControllerClosure;
import com.kolich.twitterfeed.exceptions.TwitterFeedException;

public abstract class TwitterFeedControllerClosure<T>
	extends KolichControllerClosure<T> {

	public TwitterFeedControllerClosure(String comment, Logger logger) {
		super(comment, logger);
	}
	
	@Override
	public T execute() {
		try {
			return doit();
		} catch (IllegalArgumentException e) {
			logger_.debug(comment_, e);
			throw e;
		} catch (Exception e) {
			logger_.debug(comment_, e);
			throw new TwitterFeedException(e);
		}
	}

}
