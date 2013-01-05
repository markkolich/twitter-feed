package com.kolich.twitterfeed.spring.controllers.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.kolich.havalo.client.service.HavaloClient;
import com.kolich.twitter.TwitterApiConnector;
import com.kolich.twitter.entities.TweetList;
import com.kolich.twitterfeed.entities.TwitterFeedTweetListEntity;
import com.kolich.twitterfeed.spring.controllers.AbstractTwitterFeedAPIController;
import com.kolich.twitterfeed.spring.controllers.TwitterFeedControllerClosure;

@Controller
@RequestMapping(value="feed")
public final class Feed extends AbstractTwitterFeedAPIController {
	
	private static final Logger logger__ = LoggerFactory.getLogger(Feed.class);
	
	private static final String VIEW_NAME = "feed";
	
	@Autowired
	public Feed(final HavaloClient havalo, final TwitterApiConnector twitter) {
		super(logger__, havalo, twitter);
	}
	
	@RequestMapping(method={RequestMethod.GET, RequestMethod.HEAD},
		value="{username}")
	public ModelAndView feed(@PathVariable final String username) {
		return new TwitterFeedControllerClosure<ModelAndView>(
			"GET:/api/feed/" + username, logger__) {
			@Override
			public ModelAndView doit() throws Exception {
				return getModelAndView(VIEW_NAME,
					new TwitterFeedTweetListEntity(new TweetList()));
			}
		}.execute();
	}
		
}
