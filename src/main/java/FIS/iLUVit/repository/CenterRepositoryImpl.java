package FIS.iLUVit.repository;

import FIS.iLUVit.controller.dto.CenterInfoDto;
import FIS.iLUVit.controller.dto.CenterRecommendDto;
import FIS.iLUVit.controller.dto.QCenterInfoDto;
import FIS.iLUVit.controller.dto.QCenterRecommendDto;
import FIS.iLUVit.domain.embeddable.Area;
import FIS.iLUVit.domain.embeddable.Theme;
import FIS.iLUVit.domain.enumtype.KindOf;
import FIS.iLUVit.repository.dto.CenterAndDistancePreview;
import FIS.iLUVit.repository.dto.CenterPreview;
import FIS.iLUVit.repository.dto.QCenterAndDistancePreview;
import FIS.iLUVit.repository.dto.QCenterPreview;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.stream.Collectors;

import static FIS.iLUVit.domain.QCenter.center;
import static FIS.iLUVit.domain.QPrefer.prefer;
import static FIS.iLUVit.domain.QReview.review;

@AllArgsConstructor
public class CenterRepositoryImpl extends CenterQueryMethod implements CenterRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Slice<CenterPreview> findByFilter(List<Area> areas, Theme theme, Integer interestedAge, KindOf kindOf, Pageable pageable){
        List<CenterPreview> content = jpaQueryFactory.select(new QCenterPreview(center, review.score.avg()))
                .from(center)
                .leftJoin(center.reviews, review)
                .where(areasIn(areas)
                        .and(kindOfEq(kindOf))
                        .and(themeEq(theme))
                        .and(interestedAgeEq(interestedAge)))
                .orderBy(center.score.desc(), center.id.asc())
                .groupBy(center)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if(content.size() > pageable.getPageSize()){
            content.remove(pageable.getPageSize());
            hasNext = true;
        }
        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public List<CenterAndDistancePreview> findByMapFilter(double longitude, double latitude, Theme theme, Integer interestedAge, KindOf kindOf, Integer distance) {
        double latitude_l = latitude - 0.009 * distance;
        double latitude_h = latitude + 0.009 * distance;
        double longitude_l = longitude - 0.009 * distance;
        double longitude_h = longitude + 0.009 * distance;

        List<CenterAndDistancePreview> result = jpaQueryFactory.select(new QCenterAndDistancePreview(center, review.score.avg()))
                .from(center)
                .leftJoin(center.reviews, review)
                .where(center.latitude.between(latitude_l, latitude_h)
                        .and(center.longitude.between(longitude_l, longitude_h))
                        .and(themeEq(theme))
                        .and(interestedAgeEq(interestedAge))
                        .and(kindOfEq(kindOf)))
                .groupBy(center)
                .fetch();

        return result.stream()
                .filter(centerAndDistancePreview ->
                        centerAndDistancePreview.calculateDistance(longitude, latitude) < distance)
                .collect(Collectors.toList());
    }

    @Override
    public List<CenterAndDistancePreview> findByMapFilter(double longitude, double latitude, Integer distance) {
        double latitude_l = latitude - 0.01 * distance;
        double latitude_h = latitude + 0.01 * distance;
        double longitude_l = longitude - 0.01 * distance;
        double longitude_h = longitude + 0.01 * distance;

        List<CenterAndDistancePreview> result = jpaQueryFactory.select(new QCenterAndDistancePreview(center, review.score.avg()))
                .from(center)
                .leftJoin(center.reviews, review)
                .where(center.latitude.between(latitude_l, latitude_h)
                        .and(center.longitude.between(longitude_l, longitude_h)))
                .groupBy(center)
                .fetch();

        return result.stream()
                .filter(centerAndDistancePreview ->
                        centerAndDistancePreview.calculateDistance(longitude, latitude) < distance)
                .collect(Collectors.toList());
    }

    @Override
    public List<CenterRecommendDto> findRecommendCenter(Theme theme, Pageable pageable) {
        return jpaQueryFactory.select(new QCenterRecommendDto(center.id, center.profileImagePath))
                .from(center)
                .where(themeEq(theme))
                .orderBy(center.score.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public Slice<CenterInfoDto> findForSignup(String sido, String sigungu, String centerName, Pageable pageable) {
        List<CenterInfoDto> content = jpaQueryFactory.select(new QCenterInfoDto(center.id, center.name, center.address))
                .from(center)
                .where(areaEq(sido, sigungu)
                        ,(centerNameEq(centerName)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            hasNext = true;
            content.remove(pageable.getPageSize());
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public Slice<CenterInfoDto> findCenterForAddChild(String sido, String sigungu, String centerName, Pageable pageable) {
        List<CenterInfoDto> content = jpaQueryFactory.select(new QCenterInfoDto(center.id, center.name, center.address))
                .from(center)
                .where(center.signed.eq(true)
                        ,(areaEq(sido, sigungu))
                        ,(centerNameEq(centerName)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize()+1)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            hasNext = true;
            content.remove(pageable.getPageSize());
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public Slice<CenterPreview> findByPrefer(Long userId, Pageable pageable) {
        List<CenterPreview> content = jpaQueryFactory.select(new QCenterPreview(center, review.score.avg()))
                .from(center)
                .join(center.prefers, prefer).on(prefer.parent.id.eq(userId))
                .leftJoin(center.reviews, review)
                .groupBy(center)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize()+1)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            hasNext = true;
            content.remove(pageable.getPageSize());
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

}
