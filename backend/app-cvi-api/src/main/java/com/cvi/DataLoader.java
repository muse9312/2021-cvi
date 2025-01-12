package com.cvi;

import com.cvi.comment.domain.model.Comment;
import com.cvi.comment.domain.repository.CommentRepository;
import com.cvi.like.domain.model.Like;
import com.cvi.like.domain.repository.LikeRepository;
import com.cvi.post.domain.model.Post;
import com.cvi.post.domain.model.VaccinationType;
import com.cvi.post.domain.repository.PostRepository;
import com.cvi.user.domain.model.AgeRange;
import com.cvi.user.domain.model.SocialProvider;
import com.cvi.user.domain.model.User;
import com.cvi.user.domain.repository.UserRepository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class DataLoader implements CommandLineRunner {

    private static final int USER_COUNT = 50;
    private static final int POST_COUNT = 300;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final Random random = new Random();
    private final EntityManager em;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.findAll().isEmpty()
            && postRepository.findAll().isEmpty()
            && likeRepository.findAll().isEmpty()
            && commentRepository.findAll().isEmpty()) {
            List<User> users = new ArrayList<>();
            for (int i = 0; i < USER_COUNT; i++) {
                User user = User.builder().nickname(String.valueOf(i)).socialId("socialId").socialProvider(SocialProvider.KAKAO).profileUrl("pictureUrl").ageRange(AgeRange.FIFTIES).build();
                users.add(user);

                User updateUser = User.builder().nickname(user.getNickname()).socialId("socialId").socialProvider(SocialProvider.NAVER).profileUrl("pictureUrl").ageRange(AgeRange.FORTIES).shotVerified(true).build();
                user.update(updateUser);
            }
            userRepository.saveAll(users);

            List<Post> posts = new ArrayList<>();
            List<Comment> allComments = new ArrayList<>();
            for (int i = 0; i < POST_COUNT; i++) {
                int randomInt = random.nextInt(USER_COUNT);
                User user = users.get(i % USER_COUNT);
                if (randomInt % 4 == 0) {
                    Post post = Post.builder().content("글" + (i + 1)).vaccinationType(VaccinationType.PFIZER).user(user).build();
                    List<Comment> comments = putLikeAndCommentRandomly(users, USER_COUNT, post, random);
                    allComments.addAll(comments);
                    posts.add(post);
                }
                if (randomInt % 4 == 1) {
                    Post post = Post.builder().content("글" + (i + 1)).vaccinationType(VaccinationType.ASTRAZENECA).user(user).build();
                    posts.add(post);
                }
                if (randomInt % 4 == 2) {
                    Post post = Post.builder().content("글" + (i + 1)).vaccinationType(VaccinationType.JANSSEN).user(user).build();
                    posts.add(post);
                }
                if (randomInt % 4 == 3) {
                    Post post = Post.builder().content("글" + (i + 1)).vaccinationType(VaccinationType.MODERNA).user(user).build();
                    posts.add(post);
                }
            }
            postRepository.saveAll(posts);

            for (Post post : posts) {
                Long id = post.getId();
                Query q = em.createNativeQuery("UPDATE post SET created_at=:created_at WHERE post_id=:post_id");
                q.setParameter("created_at", generateRandomLocalDateTime());
                q.setParameter("post_id", id);
                q.executeUpdate();
            }

            for (Comment comment : allComments) {
                Long id = comment.getId();
                Query q = em.createNativeQuery("UPDATE comment SET created_at=:created_at WHERE comment_id=:comment_id");
                q.setParameter("created_at", generateRandomLocalDateTime());
                q.setParameter("comment_id", id);
                q.executeUpdate();
            }
        }
    }

    private List<Comment> putLikeAndCommentRandomly(List<User> users, int userCount, Post post, Random random) {
        Set<User> userSet = new HashSet<>();
        List<Comment> comments = new ArrayList<>();

        for (int j = 0; j < random.nextInt(30); j++) {
            int randomUser = random.nextInt(userCount);
            userSet.add(users.get(randomUser));
        }

        for (User user : userSet) {
            Like like = Like.builder().user(user).build();
            post.assignLike(like);
            Comment comment = Comment.builder().content("Id:" + user.getId() + " 유저의 댓글").user(user).build();
            comments.add(comment);
            post.assignComment(comment);
            likeRepository.save(like);
        }
        return comments;
    }

    private Timestamp generateRandomLocalDateTime() {
        int randomHour = random.nextInt(24);
        int randomMins = random.nextInt(60);
        int randomDays = random.nextInt(20);
        LocalDateTime localDateTime = LocalDateTime.now().minusHours(randomHour).minusMinutes(randomMins).minusDays(randomDays);
        return Timestamp.valueOf(localDateTime);
    }
}
