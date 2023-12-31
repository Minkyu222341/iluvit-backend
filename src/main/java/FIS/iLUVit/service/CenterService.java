package FIS.iLUVit.service;

import FIS.iLUVit.domain.*;
import FIS.iLUVit.domain.embeddable.Location;
import FIS.iLUVit.dto.center.*;
import FIS.iLUVit.domain.embeddable.Score;
import FIS.iLUVit.domain.embeddable.Theme;
import FIS.iLUVit.domain.enumtype.KindOf;
import FIS.iLUVit.exception.*;
import FIS.iLUVit.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CenterService {

    private static final double EARTH_RADIUS_KM = 6371.0; // 지구 반지름 (단위: km)
    private final CenterRepository centerRepository;
    private final ImageService imageService;
    private final ReviewRepository reviewRepository;
    private final CenterBookmarkRepository centerBookmarkRepository;
    private final ParentRepository parentRepository;
    private final TeacherRepository teacherRepository;
    private final MapService mapService;


    /**
     * 시설 전체 조회
     */
    public List<CenterMapResponse> findCenterByFilterForMap(String searchContent, CenterMapRequest centerMapRequest){
        double longitude = centerMapRequest.getLongitude();
        double latitude = centerMapRequest.getLatitude();
        Double distance = centerMapRequest.getDistance();

        List<Center> centerByFilter = centerRepository.findByFilterForMap(longitude, latitude, distance, searchContent);

        List<CenterMapResponse> centerMapResponses = centerByFilter.stream().map(center -> {
            return new CenterMapResponse(center.getId(), center.getName(), center.getLongitude(), center.getLatitude());
        }).collect(Collectors.toList());

        return centerMapResponses;
    }

    /**
     * 유저가 설정한 필터 기반 시설 조회
     */
    public SliceImpl<CenterMapFilterResponse> findCenterByFilterForMapList(long userId, KindOf kindOf, CenterMapFilterRequest centerMapFilterRequest, Pageable pageable) {
        Parent parent = parentRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST));

        double longitude = centerMapFilterRequest.getLongitude();
        double latitude = centerMapFilterRequest.getLatitude();
        List<Long> centerIds = centerMapFilterRequest.getCenterIds();

        List<Center> centerByFilter = null;
        if(kindOf.equals(KindOf.ALL)){
            centerByFilter = centerRepository.findByIdInOrderByScoreDescIdAsc(centerIds);
        }else{
            centerByFilter = centerRepository.findByIdInAndKindOfOrderByScoreDescIdAsc(centerIds, kindOf);
        }

        List<CenterMapFilterResponse> centerMapFilterResponses = new ArrayList<>();

        centerByFilter.forEach((center -> {

            double avgScore = reviewRepository.findByCenter(center).stream()
                    .mapToDouble(Review::getScore)
                    .average()
                    .orElse(0.0);// 또는 null

            // 해당 유저 아이디가 센터북마크에 있는지 검증하는 로직
            Optional<Prefer> prefer = centerBookmarkRepository.findByCenterAndParent(center, parent);

            double distance = calculateDistance(latitude, longitude, center.getLatitude(), center.getLongitude());

            CenterMapFilterResponse centerMapFilterResponse = new CenterMapFilterResponse(center, distance, avgScore, prefer.isPresent());
            centerMapFilterResponses.add(centerMapFilterResponse);

        }));

        boolean hasNext = false;

        if (centerMapFilterResponses.size() > pageable.getPageSize()) {
            hasNext = true;
            centerMapFilterResponses.remove(pageable.getPageSize());
        }

        return new SliceImpl<>(centerMapFilterResponses, pageable, hasNext);
    }

    /**
     * 시설 상세 조회
     */
    public CenterDetailResponse findCenterDetailsByCenter(Long centerId) {
        Center center = centerRepository.findById(centerId)
                .orElseThrow(() -> new CenterException(CenterErrorResult.CENTER_NOT_EXIST));

        // Center 가 id 에 의해 조회 되었으므로 score에 1 추가
        center.addScore(Score.GET);

        CenterDetailResponse centerDetailResponse = new CenterDetailResponse(center,center.getProfileImagePath(),imageService.getInfoImages(center.getInfoImagePath()));
        return centerDetailResponse;
    }

    /**
     * 미리보기 배너 용 시설 상세 조회
     */
    public CenterBannerResponse findCenterBannerByCenter(Long userId, Long centerId) {
        Parent parent = parentRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST));

        Center center = centerRepository.findById(centerId)
                .orElseThrow(() -> new CenterException(CenterErrorResult.CENTER_NOT_EXIST));

        // 리뷰 score 평균
        Double tempStarAvg = reviewRepository.findByCenter(center).stream()
                .mapToInt(Review::getScore).average().orElse(0.0);
        Double starAvg = Math.round(tempStarAvg * 10) / 10.0;

        // 현재 유저와 센터에 해당하는 북마크가 있을 시 센터 북마크 조회, 없을시 null
        Optional<Prefer> centerBookmark = centerBookmarkRepository.findByCenterAndParent(center, parent);

        Boolean hasCenterBookmark = centerBookmark.isPresent();

        List<String> infoImages = imageService.getInfoImages(center.getInfoImagePath());

        CenterBannerResponse centerBannerResponse = new CenterBannerResponse(center, infoImages, hasCenterBookmark, starAvg);

        return centerBannerResponse;
    }

    /**
     *  추천 시설 전체 조회 ( 학부모가 선택한 관심 테마를 가지고 있는 시설 조회 )
     */
    public List<CenterRecommendResponse> findRecommendCenterWithTheme(Long userId) {
        Parent parent = parentRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST));

        Theme theme = parent.getTheme();
        Location location = parent.getLocation();
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("score"));


        List<CenterRecommendResponse> centerRecommendResponses = centerRepository.findRecommendCenter(theme, location, pageRequest).stream()
                .map(CenterRecommendResponse::new) // Center를 CenterRecommendDto로 변환
                .collect(Collectors.toList());

        return centerRecommendResponses;
    }

    /**
     * 시설 정보 수정
     */
    public void modifyCenterInfo(Long userId, Long centerId, CenterDetailRequest centerDetailRequest) {
        Teacher teacher = teacherRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST))
                .canWrite(centerId);
        // 해당하는 center 없으면 RuntimeException 반환

        Center center = teacher.getCenter();

        Pair<Double, Double> location = mapService.convertAddressToLocation(centerDetailRequest.getAddress());

        Double longitude = location.getFirst();
        Double latitude = location.getSecond();

        Pair<String, String> area = mapService.getSidoSigunguByLocation(longitude ,latitude);
        String sido = area.getFirst();
        String sigungu = area.getSecond();

        center.updateCenter(centerDetailRequest,longitude, latitude, sido, sigungu);
    }


    /**
     * 시설 이미지 수정
     */
    public void modifyCenterImage(Long userId, Long centerId, CenterImageRequest centerImageRequest) {
        Teacher teacher = teacherRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorResult.USER_NOT_EXIST))
                .canWrite(centerId);


        List<MultipartFile> infoImages = centerImageRequest.getInfoImages();
        MultipartFile profileImage = centerImageRequest.getProfileImage();

        imageService.saveInfoImages(infoImages, teacher.getCenter());
        imageService.saveProfileImage(profileImage, teacher.getCenter());
    }


    /**
     * 두 지점 사이의 거리를 계산하는 메서드
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

}
