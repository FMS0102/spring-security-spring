package com.fms.springsecurity.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fms.springsecurity.entities.Comment;
import com.fms.springsecurity.entities.Like;
import com.fms.springsecurity.entities.Tweet;
import com.fms.springsecurity.entities.User;

public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByTweetAndUser(Tweet tweet, User user);
    boolean existsByCommentAndUser(Comment comment, User user);

    @Modifying
    @Query("DELETE FROM Like l WHERE l.comment.id = :commentId")
    void deleteByCommentId(@Param("commentId") Long commentId);
}
