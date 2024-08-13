package com.fms.springsecurity.dto;

public record LoginResponse(String accessToken, Long expiresIn) {

}
