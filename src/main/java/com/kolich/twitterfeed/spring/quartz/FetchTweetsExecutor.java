package com.kolich.twitterfeed.spring.quartz;

import static com.google.common.net.HttpHeaders.ETAG;
import static com.kolich.twitter.entities.TwitterEntity.getNewTwitterGsonInstance;
import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.kolich.havalo.client.entities.FileObject;
import com.kolich.havalo.client.service.HavaloClient;
import com.kolich.http.common.either.HttpResponseEither;
import com.kolich.http.common.response.HttpFailure;
import com.kolich.twitter.TwitterApiClient;
import com.kolich.twitter.entities.Tweet;
import com.kolich.twitter.entities.TweetList;
import com.kolich.twitterfeed.exceptions.TwitterFeedException;

public final class FetchTweetsExecutor implements Runnable, InitializingBean {
	
	private static final Logger logger__ =
		LoggerFactory.getLogger(FetchTweetsExecutor.class);
	
	private static final int DEFAULT_MAX_TWEETS_PER_USER = 10;
		
	private TwitterApiClient twitter_;
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
		final Thread preFetch = new Thread(this,
			"fetch-tweets-executor-prefetch");
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
					// Worked!  We got something back from the Twitter API.
					List<Tweet> tweets = twitter.right();
					// Only allowed to store X-tweets per user.
					if(tweets.size() > maxTweetsPerUser_) {
						tweets = tweets.subList(0, maxTweetsPerUser_);
					}
					logger__.info("Loaded tweets (user=" + username +
						", tweets=" + tweets.size() + ")");
					// Flush the tweets to Havalo.
					saveTweets(username, tweets);
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
	
	private void saveTweets(final String username, final List<Tweet> tweets) {
		final List<Header> headers = new ArrayList<Header>(1);
		headers.add(new BasicHeader(CONTENT_TYPE, "application/json"));
		final HttpResponseEither<HttpFailure, FileObject> response =
			havalo_.putObject(
				// The actual list of tweets, JSON serialized.
				getBytesUtf8(getNewTwitterGsonInstance().toJson(
					new TweetList(tweets), TweetList.class)),
				// Relevant HTTP request headers.
				headers.toArray(new Header[headers.size()]),
				// The username (object key).
				username);
		if(!response.success()) {
			final HttpFailure failure = response.left();
			throw new TwitterFeedException("Failed to flush tweets " +
				"(user=" + username + ", status=" + failure.getStatusCode() +
					")", failure.getCause());
		} else {
			final FileObject object = response.right();
			logger__.info("Successfully flushed tweets to Havalo (user=" +
				username + ", ETag=" + object.getFirstHeader(ETAG) + ")");
		}
	}
		
	public void setTwitterApiClient(final TwitterApiClient twitter) {
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
