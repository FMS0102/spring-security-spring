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

import com.fms.springsecurity.dto.CreateTweetDto;
import com.fms.springsecurity.dto.FeedDto;
import com.fms.springsecurity.dto.FeedItemDto;
import com.fms.springsecurity.entities.Like;
import com.fms.springsecurity.entities.Role;
import com.fms.springsecurity.entities.Tweet;
import com.fms.springsecurity.repositories.LikeRepository;
import com.fms.springsecurity.repositories.TweetRepository;
import com.fms.springsecurity.repositories.UserRepository;

@RestController
public class TweetController {

    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    public TweetController(TweetRepository tweetRepository, UserRepository userRepository,
            LikeRepository likeRepository) {
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
    }

    @GetMapping("/feed")
    public ResponseEntity<FeedDto> feed(@RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        var tweets = tweetRepository.findAll(PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimestamp"))
                .map(tweet -> new FeedItemDto(
                        tweet.getTweetId(),
                        tweet.getContent(),
                        tweet.getUser().getUsername(),
                        tweet.getLikes(),
                        tweet.getcomments().size()));

        return ResponseEntity.ok(
                new FeedDto(tweets.getContent(), page, pageSize, tweets.getTotalPages(), tweets.getTotalElements()));
    }

    @PostMapping("/tweets")
    public ResponseEntity<Void> createTweet(@RequestBody CreateTweetDto dto, JwtAuthenticationToken token) {
        var user = userRepository.findById(UUID.fromString(token.getName()));

        var tweet = new Tweet();
        tweet.setUser(user.get());
        tweet.setContent(dto.content());

        tweetRepository.save(tweet);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tweets/{id}")
    public ResponseEntity<Void> deleteTweet(@PathVariable("id") Long tweetId, JwtAuthenticationToken token) {

        var user = userRepository.findById(UUID.fromString(token.getName()));
        var tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var isAdmin = user.get().getRoles()
                .stream().anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if (isAdmin || tweet.getUser().getUserId().equals(UUID.fromString(token.getName()))) {
            tweetRepository.deleteById(tweetId);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/tweets/{id}")
    public ResponseEntity<Void> likeTweet(@PathVariable("id") long tweetId, JwtAuthenticationToken token) {
        var tweet = tweetRepository.findById(tweetId);
        var user = userRepository.findById(UUID.fromString(token.getName()));

        if (!likeRepository.existsByTweetAndUser(tweet.get(), user.get())) {
            var like = new Like();
            like.setTweet(tweet.get());
            like.setUser(user.get());

            likeRepository.save(like);

            tweet.get().setLikes(tweet.get().getLikes() + 1);
            tweetRepository.save(tweet.get());
        }

        return ResponseEntity.ok().build();

    }

}
