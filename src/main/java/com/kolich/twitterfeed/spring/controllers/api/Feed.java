package com.kolich.twitterfeed.spring.controllers.api;

import com.kolich.common.functional.either.Either;
import com.kolich.havalo.client.service.HavaloClient;
import com.kolich.http.blocking.helpers.definitions.CustomEntityConverter;
import com.kolich.http.common.response.HttpFailure;
import com.kolich.http.common.response.HttpSuccess;
import com.kolich.twitter.entities.TweetList;
import com.kolich.twitterfeed.entities.TwitterFeedTweetListEntity;
import com.kolich.twitterfeed.exceptions.TwitterFeedException;
import com.kolich.twitterfeed.exceptions.havalo.ResourceNotFoundException;
import com.kolich.twitterfeed.spring.controllers.AbstractTwitterFeedAPIController;
import com.kolich.twitterfeed.spring.controllers.TwitterFeedControllerClosure;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static com.kolich.twitter.entities.TwitterEntity.getNewTwitterGsonInstance;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;

@Controller
@RequestMapping(value="feed")
public final class Feed extends AbstractTwitterFeedAPIController {
	
	private static final Logger logger__ = LoggerFactory.getLogger(Feed.class);
	
	private static final String VIEW_NAME = "feed";
	
	private final HavaloClient havalo_;
	
	@Autowired
	public Feed(final HavaloClient havalo) {
		super(logger__);
		havalo_ = havalo;
	}
	
	@RequestMapping(method={RequestMethod.GET, RequestMethod.HEAD},
		value="{username}")
	public ModelAndView feed(@PathVariable final String username) {
		return new TwitterFeedControllerClosure<ModelAndView>(
			"GET:/api/feed/" + username, logger__) {
			@Override
			public ModelAndView doit() throws Exception {
				// Attempt to load the users' tweets from the Havalo K,V store.				
				final Either<HttpFailure, TwitterFeedTweetListEntity>
					tweets = getTweets(username);
				// Check if we got a list of tweets back.  If so, serve
				// 'em up.  If not, throw the right exception so the
				// controller closure renders the right "error" view.
				if(!tweets.success()) {
					final HttpFailure failure = tweets.left();
					if(failure.getStatusCode() == SC_NOT_FOUND) {
						throw new ResourceNotFoundException("No cached tweets " +
							"available for user (user=" + username + ", status=" +
							failure.getStatusCode() + ")", failure.getCause());
					} else {
						throw new TwitterFeedException("Failed to load tweets " +
							"from Havalo (user=" + username + ", status=" +
							failure.getStatusCode() + ")", failure.getCause());
					}
				}
				return getModelAndView(VIEW_NAME, tweets.right());
			}
		}.execute();
	}
	
	private Either<HttpFailure,TwitterFeedTweetListEntity> getTweets(
		final String username) {
		return havalo_.getObject(
			new CustomEntityConverter<HttpFailure,TwitterFeedTweetListEntity>() {
			@Override
			public TwitterFeedTweetListEntity success(final HttpSuccess success)
				throws Exception {
				final TweetList tl = getNewTwitterGsonInstance().fromJson(
					EntityUtils.toString(success.getEntity(), UTF_8),
					TweetList.class);
				return new TwitterFeedTweetListEntity(tl);
			}
			@Override
			public HttpFailure failure(final HttpFailure failure) {
				return failure;
			}
		}, username);
	}
	
}
