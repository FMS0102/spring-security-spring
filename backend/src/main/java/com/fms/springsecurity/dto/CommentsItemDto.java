package com.fms.springsecurity.dto;

public record CommentsItemDto(Long tweetId, Long commentId, String comment, String username, int like) {

}
