package com.fms.springsecurity.dto;

public record FeedItemDto(Long tweetId, String content, String username, int like, int comments) {

}
