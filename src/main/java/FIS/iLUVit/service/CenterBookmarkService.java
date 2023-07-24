package FIS.iLUVit.service;

import FIS.iLUVit.domain.Center;
import FIS.iLUVit.domain.Parent;
import FIS.iLUVit.domain.Prefer;
import FIS.iLUVit.domain.Review;
import FIS.iLUVit.exception.PreferErrorResult;
import FIS.iLUVit.exception.PreferException;
import FIS.iLUVit.exception.UserErrorResult;
import FIS.iLUVit.exception.UserException;
import FIS.iLUVit.repository.CenterRepository;
import FIS.iLUVit.repository.ParentRepository;
import FIS.iLUVit.repository.CenterBookmarkRepository;
import FIS.iLUVit.dto.center.CenterPreviewDto;
import FIS.iLUVit.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CenterBookmarkService {
    private final CenterRepository centerRepository;
    private final CenterBookmarkRepository centerBookmarkRepository;
    private final ParentRepository parentRepository;
    private final ReviewRepository reviewRepository;

    /**
     *  유저가 즐겨찾기한 시설을 조회합니다
     */
    public Slice<CenterPreviewDto> findCentersByCenterBookmark(Long userId, Pageable pageable) {
        Parent parent = parentRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST));

        List<Prefer> centerBookmarks = centerBookmarkRepository.findByParent(parent);

        List<CenterPreviewDto> centerPreviewDtos = new ArrayList<>();

        centerBookmarks.forEach((centerBookmark)->{
            List<Review> reviews = reviewRepository.findByCenter(centerBookmark.getCenter());

            // Review 객체들의 score 필드의 평균 계산
            double averageScore = reviews.stream()
                    .mapToDouble(Review::getScore)
                    .average()
                    .orElse(0.0); // 만약 리뷰가 없는 경우 0.0을 반환

            centerPreviewDtos.add(new CenterPreviewDto(centerBookmark.getCenter(), averageScore));
        });

        boolean hasNext = false;
        if (centerBookmarks.size() > pageable.getPageSize()) {
            hasNext = true;
            centerBookmarks.remove(pageable.getPageSize());
        }

        return new SliceImpl<>(centerPreviewDtos, pageable, hasNext);
    }


    /**
     *   작성자: 이승범
     *   작성내용: 해당 시설을 시설 즐겨찾기에 등록합니다
     */
    public Prefer saveCenterBookmark(Long userId, Long centerId) {

        centerBookmarkRepository.findByUserIdAndCenterId(userId, centerId)
                .ifPresent(prefer -> {
                    throw new PreferException(PreferErrorResult.ALREADY_PREFER);
                });

        try {
            Parent parent = parentRepository.getById(userId);
            Center center = centerRepository.getById(centerId);
            Prefer prefer = Prefer.createPrefer(parent, center);
            centerBookmarkRepository.saveAndFlush(prefer);
            return prefer;
        } catch (DataIntegrityViolationException e) {
            throw new PreferException(PreferErrorResult.NOT_VALID_CENTER);
        }
    }

    /**
     *   작성자: 이승범
     *   작성내용: 해당 시설의 시설 즐겨찾기를 해제합니다
     */
    public void deleteCenterBookmark(Long userId, Long centerId) {
        Prefer deletedPrefer = centerBookmarkRepository.findByUserIdAndCenterId(userId, centerId)
                .orElseThrow(() -> new PreferException(PreferErrorResult.NOT_VALID_CENTER));

        centerBookmarkRepository.delete(deletedPrefer);
    }

}
