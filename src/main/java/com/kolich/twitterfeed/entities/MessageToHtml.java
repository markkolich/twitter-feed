package com.kolich.twitterfeed.entities;

import static java.util.regex.Matcher.quoteReplacement;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.gagawa.java.elements.A;

public class MessageToHtml {
	
	private static final String LINK_TARGET_BLANK = "_blank";
	private static final String LINK_REL_TWITTER = "twitter";
    private static final String TWITTER_HOMEPAGE_URL = "http://twitter.com/%s";
	
	private static final Pattern usernameRegex__ =
		compile("(?<=@)[-a-zA-Z0-9_]+", CASE_INSENSITIVE);
	
	// Precompile the URL extraction pattern for efficiency.
	// This pattern was borrowed from:
	// http://www.codinghorror.com/blog/2008/10/the-problem-with-urls.html
	private static final Pattern urlRegex__ =
		compile("\\(?\\b(https?://|www[.])" +
			"[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]",
				CASE_INSENSITIVE);
	
	public static final String makeHyperlinks(final String message) {
		String html = new String(message);
		Matcher m = null;
		for(final URL url : getUrlsInMessage(html)) {
			try {
				m = compile(url.toString(), CASE_INSENSITIVE).matcher(html);
				while(m.find()) {
					// Matcher.quoteReplacement() is necessary here to escape
					// $ signs (dollar signs) and other characters that are
					// interpretered differently by the regex parser.  In this
					// case, a dollar sign really means a "group" but we don't
					// want it to mean a group, so we use quoteReplacement()
					// to handle this situation.
					html = m.replaceAll(quoteReplacement(
						getUrlAnchor(url.toString())));
				}
			} catch (Exception e) {
				continue;
			}
		}
		// Username replacement.
		m = usernameRegex__.matcher(html);
		while(m.find()) {
			try {
				final String screenName = m.group();
				final String screenNameAt = String.format("@%s", screenName);
				// Create a matcher that looks for instances of the username
				// with the preceeding @ on Twitter usernames.
				final Matcher q = Pattern.compile(screenNameAt,
					CASE_INSENSITIVE).matcher(html);
				final String anchor = getUserAnchor(screenName, screenNameAt);
				while(q.find()) {
					try {
                        html = q.replaceAll(anchor);
					} catch (Exception e) {
                        throw e;
					}
				}
			} catch (Exception e) {
				continue;
			}
        }
		return html;
	}
	
	private static final List<URL> getUrlsInMessage(final String message) {
		final List<URL> urls = new ArrayList<URL>();
		final Matcher m = urlRegex__.matcher(message);
		while(m.find()) {
			try {
				urls.add(new URL(m.group()));
			} catch (MalformedURLException e) {
				// Do nothing, if it's malformed just move on silently.
			}
		}
		return urls;
	}
	
	private static final String getUrlAnchor(final String href) {
		return new A().setHref(href).setTarget(LINK_TARGET_BLANK)
			.appendText(href).write();
	}
	
	private static final String getUserAnchor(final String screenName,
		final String screenNameAt) {
		return new A().setHref(String.format(TWITTER_HOMEPAGE_URL, screenName))
			// Target is a blank page.
			.setTarget(LINK_TARGET_BLANK)
			// The "rel" attribute of the link is how an app can identify
			// that this link is one that points to a Twitter user.
			.setRel(LINK_REL_TWITTER)
			// Add the @username to the text of the anchor.
			.appendText(screenNameAt).write();
	}
	
}
