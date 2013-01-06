package com.kolich.twitterfeed.spring.controllers.api;

import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static com.kolich.twitter.entities.TwitterEntity.getNewTwitterGsonBuilder;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;

import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.kolich.havalo.client.service.HavaloClient;
import com.kolich.http.HttpClient4Closure.HttpFailure;
import com.kolich.http.HttpClient4Closure.HttpResponseEither;
import com.kolich.http.HttpClient4Closure.HttpSuccess;
import com.kolich.http.helpers.definitions.CustomEntityConverter;
import com.kolich.twitter.entities.TweetList;
import com.kolich.twitterfeed.entities.TwitterFeedTweetListEntity;
import com.kolich.twitterfeed.exceptions.TwitterFeedException;
import com.kolich.twitterfeed.exceptions.havalo.ResourceNotFoundException;
import com.kolich.twitterfeed.spring.controllers.AbstractTwitterFeedAPIController;
import com.kolich.twitterfeed.spring.controllers.TwitterFeedControllerClosure;

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
				final HttpResponseEither<HttpFailure,TwitterFeedTweetListEntity>
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
	
	private HttpResponseEither<HttpFailure,TwitterFeedTweetListEntity> getTweets(
		final String username) {
		return havalo_.getObject(new CustomEntityConverter<TwitterFeedTweetListEntity>() {
			@Override
			public TwitterFeedTweetListEntity convert(final HttpSuccess success)
				throws Exception {
				final TweetList tl = getNewTwitterGsonBuilder().create().fromJson(
					EntityUtils.toString(success.getResponse().getEntity(), UTF_8),
					TweetList.class);
				return new TwitterFeedTweetListEntity(tl);
			}
		}, username);
	}
	
}
