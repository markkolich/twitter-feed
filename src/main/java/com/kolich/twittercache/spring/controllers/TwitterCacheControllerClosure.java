package com.kolich.twittercache.spring.controllers;

import org.slf4j.Logger;

import com.kolich.spring.controllers.KolichControllerClosure;
import com.kolich.twittercache.exceptions.TwitterCacheException;

public abstract class TwitterCacheControllerClosure<T>
	extends KolichControllerClosure<T> {

	public TwitterCacheControllerClosure(String comment, Logger logger) {
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
			throw new TwitterCacheException(e);
		}
	}

}
