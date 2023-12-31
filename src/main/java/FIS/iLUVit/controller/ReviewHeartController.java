package FIS.iLUVit.controller;

import FIS.iLUVit.config.argumentResolver.Login;
import FIS.iLUVit.service.ReviewHeartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("review-heart")
public class ReviewHeartController {

    private final ReviewHeartService reviewHeartService;

    /**
     * COMMON
     */

    /**
     * 작성자: 이창윤
     * 내용: 리뷰 좋아요 등록
    */
    @PostMapping("{reviewId}")
    public Long createReviewHeart(@PathVariable("reviewId") Long reviewId,
                     @Login Long userId) {
        return reviewHeartService.saveReviewHeart(reviewId, userId);
    }

    /**
        작성자: 이창윤
        내용: 리뷰 좋아요 취소
    */
    @DeleteMapping("{reviewId}")
    public void deleteReviewHeart(@PathVariable("reviewId") Long reviewId,
                       @Login Long userId) {
        reviewHeartService.deleteReviewHeart(reviewId, userId);
    }

}
