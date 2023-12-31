package FIS.iLUVit.service;

import FIS.iLUVit.domain.Review;
import FIS.iLUVit.domain.ReviewHeart;
import FIS.iLUVit.domain.User;
import FIS.iLUVit.exception.ReviewErrorResult;
import FIS.iLUVit.exception.ReviewException;
import FIS.iLUVit.exception.UserErrorResult;
import FIS.iLUVit.exception.UserException;
import FIS.iLUVit.repository.ReviewHeartRepository;
import FIS.iLUVit.repository.ReviewRepository;
import FIS.iLUVit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewHeartService {
    private final ReviewHeartRepository reviewHeartRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public Long saveReviewHeart(Long reviewId, Long userId) {
        if (userId == null) {
            throw new ReviewException(ReviewErrorResult.UNAUTHORIZED_USER_ACCESS);
        }
        reviewHeartRepository.findByReviewAndUser(reviewId, userId)
                .ifPresent(m -> {
                    throw new ReviewException(ReviewErrorResult.NO_MORE_THAN_ONE_REVIEW_HEART);
                });
        Review findReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException(ReviewErrorResult.REVIEW_NOT_EXIST));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST));
        ReviewHeart reviewHeart = new ReviewHeart(findReview, user);
        return reviewHeartRepository.save(reviewHeart).getId();
    }

    public void deleteReviewHeart(Long reviewId, Long userId) {
        // 리뷰 좋아요한 데이터가 존재하면 삭제, 존재하지 않는데 삭제 요청을 보내면 Exception 터뜨림
        reviewHeartRepository.findByReviewAndUser(reviewId, userId)
                .ifPresentOrElse(reviewHeartRepository::delete, () -> {
                    throw new ReviewException(ReviewErrorResult.REVIEW_HEART_NOT_EXIST);
                });
    }
}
