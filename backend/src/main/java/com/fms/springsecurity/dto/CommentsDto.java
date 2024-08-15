package com.fms.springsecurity.dto;

import java.util.List;

public record CommentsDto(
        List<CommentsItemDto> commentsItens,
        int page,
        int pageSize,
        int totalPages,
        long totalElments) {

}
