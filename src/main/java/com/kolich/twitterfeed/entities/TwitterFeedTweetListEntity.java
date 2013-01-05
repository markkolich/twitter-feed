package com.kolich.twitterfeed.entities;

import com.hp.gagawa.java.elements.Br;
import com.hp.gagawa.java.elements.Em;
import com.hp.gagawa.java.elements.Li;
import com.hp.gagawa.java.elements.Text;
import com.hp.gagawa.java.elements.Ul;
import com.kolich.twitter.entities.Tweet;
import com.kolich.twitter.entities.TweetList;
import com.kolich.twitter.entities.TwitterEntity;

public final class TwitterFeedTweetListEntity extends TwitterEntity {
	
	private final TweetList tweets_;
	
	public TwitterFeedTweetListEntity(final TweetList tweets) {
		tweets_ = tweets;
	}

	@Override
	public byte[] getBytes() {
		return tweets_.getBytes();
	}
	
	public String getHtml() {
		final Ul ul = new Ul();
		for(final Tweet t : tweets_.getTweets()) {
			// <li id="tweet-${tweet.id}">${tweet.html}<br /><em>${tweet.createdAt}</em><br /><br /></li>
			final Li li = new Li()
				.setId(t.getId())
				.appendChild(new Text(t.getText()))
				.appendChild(new Br());
			final Em createdAt = new Em().appendText(t.getCreatedAt().toString());
			li.appendChild(createdAt);
			li.appendChild(new Br()).appendChild(new Br());
			ul.appendChild(li);
		}
		return ul.write();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tweets_ == null) ? 0 : tweets_.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TwitterFeedTweetListEntity other = (TwitterFeedTweetListEntity) obj;
		if (tweets_ == null) {
			if (other.tweets_ != null)
				return false;
		} else if (!tweets_.equals(other.tweets_))
			return false;
		return true;
	}
	
}
