<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ page import="static com.kolich.spring.views.AbstractKolichView.VIEW_PAYLOAD" %>
<%@ page import="com.kolich.spring.views.KolichViewSerializable" %>
<%@ page import="com.kolich.twitterfeed.entities.TwitterFeedTweetListEntity" %>

<%
response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
response.setDateHeader("Expires", 0L);
final TwitterFeedTweetListEntity feed = (TwitterFeedTweetListEntity)
	((KolichViewSerializable)request.getAttribute(VIEW_PAYLOAD)).getEntity();
%>

<%= feed.getHtml() %>

<%--
// Gosh, what a mess.  First we have to extract the serializable
// from the request (as inserted by Spring) then need to cast it to
// the appropriate type, in this case a TweetList.
final TweetList tl = (TweetList)((TwitterFeedViewSerializable)request.getAttribute(VIEW_PAYLOAD)).getEntity();
// Binds the List<Tweet> to the page context variable "tweets" so
// it's accessible by JSTL.
pageContext.setAttribute("tweets", tl.getTweets());
<ul>
	<c:forEach items="${tweets}" var="tweet">
		<li id="tweet-${tweet.id}">${tweet.html}<br /><em>${tweet.createdAt}</em><br /><br /></li>
	</c:forEach>
</ul>
--%>