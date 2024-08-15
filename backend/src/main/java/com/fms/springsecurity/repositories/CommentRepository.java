package com.fms.springsecurity.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fms.springsecurity.entities.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByTweetTweetId(Long tweetIdm, Pageable pageable);

}
