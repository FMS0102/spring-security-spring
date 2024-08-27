package com.fms.springsecurity.controllers;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fms.springsecurity.dto.CommentsDto;
import com.fms.springsecurity.dto.CommentsItemDto;
import com.fms.springsecurity.dto.CreateCommentDto;
import com.fms.springsecurity.entities.Comment;
import com.fms.springsecurity.entities.Like;
import com.fms.springsecurity.entities.Role;
import com.fms.springsecurity.repositories.CommentRepository;
import com.fms.springsecurity.repositories.LikeRepository;
import com.fms.springsecurity.repositories.TweetRepository;
import com.fms.springsecurity.repositories.UserRepository;

@RestController
public class CommentController {

    private final UserRepository userRepository;
    private final TweetRepository tweetRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public CommentController(UserRepository userRepository, TweetRepository tweetRepository,
            CommentRepository commentRepository, LikeRepository likeRepository) {
        this.userRepository = userRepository;
        this.tweetRepository = tweetRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
    }

    @GetMapping("/tweets/{id}/comments")
    public ResponseEntity<CommentsDto> comments(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, @PathVariable("id") long tweetId) {

        var comments = commentRepository
                .findByTweetTweetId(tweetId, PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimestamp"))
                .map(comment -> new CommentsItemDto(comment.getTweet().getTweetId(), comment.getCommentId(),
                        comment.getComment(),
                        comment.getUser().getUsername(), comment.getLikes()));

        return ResponseEntity.ok(
                new CommentsDto(comments.getContent(), page, pageSize, comments.getTotalPages(),
                        comments.getTotalElements()));

    }

    @PostMapping("/tweets/{id}/comments")
    public ResponseEntity<Void> createComment(@RequestBody CreateCommentDto dto, @PathVariable("id") Long tweetId,
            JwtAuthenticationToken token) {

        var user = userRepository.findById(UUID.fromString(token.getName()));
        var tweet = tweetRepository.findById(tweetId);

        var comment = new Comment();
        comment.setUser(user.get());
        comment.setTweet(tweet.get());
        comment.setComment(dto.comment());
        comment.setLikes(dto.like());

        commentRepository.save(comment);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tweets/{id}/comments/{commentsId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("id") long tweetId,
            @PathVariable("commentsId") long commentId,
            JwtAuthenticationToken token) {

        var user = userRepository.findById(UUID.fromString(token.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        var tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tweet não encontrado"));

        var comment = tweet.getcomments().stream()
                .filter(res -> res.getCommentId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentário não encontrado"));

        var isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if (isAdmin || comment.getUser().getUserId().equals(UUID.fromString(token.getName()))) {
            if (comment.getLikes() > 0) {
                likeRepository.deleteByCommentId(commentId);
            }

            commentRepository.deleteById(commentId);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/tweets/{id}/comments/{commentsId}")
    public ResponseEntity<Void> likeComment(@PathVariable("id") long tweetId,
            @PathVariable("commentsId") long commentId,
            JwtAuthenticationToken token) {
        var tweet = tweetRepository.findById(tweetId);
        var user = userRepository.findById(UUID.fromString(token.getName()));
        var comment = commentRepository.findById(commentId);

        if (!likeRepository.existsByCommentAndUser(comment.get(), user.get())) {
            var like = new Like();
            like.setTweet(tweet.get());
            like.setUser(user.get());
            like.setComment(comment.get());

            likeRepository.save(like);

            comment.get().setLikes(comment.get().getLikes() + 1);
            commentRepository.save(comment.get());
        }

        return ResponseEntity.ok().build();

    }

}
