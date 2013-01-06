/**
 * Copyright (c) 2012 Mark S. Kolich
 * http://mark.koli.ch
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.kolich.twitterfeed.spring.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.kolich.havalo.client.entities.KeyPair;
import com.kolich.havalo.client.service.HavaloClient;
import com.kolich.http.HttpClient4Closure.HttpFailure;
import com.kolich.http.HttpClient4Closure.HttpResponseEither;
import com.kolich.twitterfeed.exceptions.TwitterFeedException;

public final class TwitterFeedBootstrap implements InitializingBean {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(TwitterFeedBootstrap.class);
	
	private final HavaloClient havalo_;
	
	@Autowired
	public TwitterFeedBootstrap(final HavaloClient havalo) {
		havalo_ = havalo;
	}
		
	@Override
	public void afterPropertiesSet() throws Exception {
		// Validate connectivity to Havalo; if that doesn't work then this
		// web-application is pretty much useless since it relies on the K,V
		// store API for all underlying operations.
		final HttpResponseEither<HttpFailure, KeyPair> authCheck =
			havalo_.authenticate();
		if(authCheck.success()) {
			logger__.debug("Successfully verified connection to " +
				"Havalo API (key=" + authCheck.right().getKey().toString() +
				")");
		} else {
			final HttpFailure failure = authCheck.left();
			throw new TwitterFeedException("Failed to verify connection " +
				"to the Havalo API ... you might be missing a required " +
				".properties file (status=" + failure.getStatusCode() + ")",
					failure.getCause());
		}
	}

}
