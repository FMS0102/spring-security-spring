package com.fms.springsecurity.dto;

import com.fms.springsecurity.entities.Tweet;
import com.fms.springsecurity.entities.User;

public record LikeDto(Tweet tweet, User user) {

}
