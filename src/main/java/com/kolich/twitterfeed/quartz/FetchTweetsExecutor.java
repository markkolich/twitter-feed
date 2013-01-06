package com.kolich.twitterfeed.quartz;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.kolich.havalo.client.service.HavaloClient;
import com.kolich.http.HttpClient4Closure.HttpFailure;
import com.kolich.http.HttpClient4Closure.HttpResponseEither;
import com.kolich.twitter.TwitterApiConnector;
import com.kolich.twitter.entities.Tweet;
import com.kolich.twitterfeed.exceptions.TwitterFeedException;

public final class FetchTweetsExecutor implements Runnable, InitializingBean {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(FetchTweetsExecutor.class);
	
	private static final int DEFAULT_MAX_TWEETS_PER_USER = 10;
		
	private TwitterApiConnector twitter_;
	private HavaloClient havalo_;
	
	/**
	 * The list of usernames to fetch tweets for.
	 */
	private List<String> users_;
	
	/**
	 * The maximum number of tweets to cache for any one user.
	 */
	private int maxTweetsPerUser_ = DEFAULT_MAX_TWEETS_PER_USER;
		
	@Override
	public void afterPropertiesSet() throws Exception {
		// When we start-up, immediately fetch the latest set of
		// tweets from the Twitter API for each user in our list.
		final Thread preFetch = new Thread(this, "tweet-executor-prefetch");
		preFetch.setDaemon(true);
		preFetch.start();
	}
	
	@Override
	public void run() {
		try {
			// For each user in our list, fetch their tweets from the
			// Twitter API, then cache them in Havalo.
			for(final String username : users_) {
				try {
					final HttpResponseEither<HttpFailure, List<Tweet>> twitter =
						twitter_.getTweets(username);
					if(!twitter.success()) {
						final HttpFailure failure = twitter.left();
						throw new TwitterFeedException("Failed to fetch " +
							"tweets (user=" + username + ", status=" +
								failure.getStatusCode() + ")",
									failure.getCause());
					}
					// Worked!
					List<Tweet> tweets = twitter.right();
					// Only allowed to store X-tweets per user.
					if(tweets.size() > maxTweetsPerUser_) {
						tweets = tweets.subList(0, maxTweetsPerUser_);
					}
					logger__.info("Loaded tweets (user=" + username +
						", tweets=" + tweets.size() + ")");
				} catch (TwitterFeedException e) {
					// Somewhat expected error occured, handling.
					logger__.warn("Could not load tweets for user: " +
						username, e);
				} catch (Exception e) {
					// Something else wacky went wrong, give up.
					throw e;
				}
			}
		} catch (Exception e) {
			logger__.error("Failed to fetch tweets, giving up " +
				"until next iteration.", e);
		}
	}
		
	public void setTwitterApiClient(final TwitterApiConnector twitter) {
		twitter_ = twitter;
	}
	
	public void setHavaloClient(final HavaloClient havalo) {
		havalo_ = havalo;
	}
	
	public void setUsers(final List<String> users) {
		users_ = users;
	}
	
	public void setMaxTweetsPerUser(int maxTweetsPerUser) {
		maxTweetsPerUser_ = maxTweetsPerUser;
	}
	
}
