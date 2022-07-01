package FIS.iLUVit.service;

import FIS.iLUVit.controller.dto.CenterInfoDto;
import FIS.iLUVit.controller.dto.CenterInfoRequest;
import FIS.iLUVit.controller.dto.CenterInfoResponseDto;
import FIS.iLUVit.controller.dto.CenterModifyRequestDto;
import FIS.iLUVit.domain.*;
import FIS.iLUVit.domain.embeddable.Area;
import FIS.iLUVit.domain.embeddable.Score;
import FIS.iLUVit.domain.embeddable.Theme;
import FIS.iLUVit.domain.enumtype.KindOf;
import FIS.iLUVit.exception.CenterException;
import FIS.iLUVit.exception.UserException;
import FIS.iLUVit.repository.ParentRepository;
import FIS.iLUVit.repository.UserRepository;
import FIS.iLUVit.repository.dto.CenterAndDistancePreview;
import FIS.iLUVit.repository.CenterRepository;
import FIS.iLUVit.repository.dto.CenterBannerDto;
import FIS.iLUVit.repository.dto.CenterPreview;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CenterService {

    private final CenterRepository centerRepository;
    private final ImageService imageService;
    private final ParentRepository parentRepository;
    private final UserRepository userRepository;

    public Slice<CenterPreview> findByFilter(List<Area> areas, Theme theme, Integer interestedAge, KindOf kindOf, Pageable pageable) {
        if (!(kindOf == KindOf.Kindergarten) && !(kindOf == KindOf.Childhouse) && !(kindOf == KindOf.ALL)) {
            throw new RuntimeException();
        }
        Slice<CenterPreview> results = centerRepository.findByFilter(areas, theme, interestedAge, kindOf, pageable);
        results.getContent().forEach(centerPreview -> {
            Long centerId = centerPreview.getId();
            String centerProfileDir = imageService.getCenterProfileDir();
            centerPreview.setProfileImage(imageService.getEncodedProfileImage(centerProfileDir, centerId));
        });
        return results;
    }

    public List<CenterAndDistancePreview> findByFilterAndMap(double longitude, double latitude, Theme theme, Integer interestedAge, KindOf kindOf, Integer distance) {
        if (!(kindOf == KindOf.Kindergarten) && !(kindOf == KindOf.Childhouse) && !(kindOf == KindOf.ALL)) {
            throw new RuntimeException();
        }
        Map<Long, CenterAndDistancePreview> map =
                centerRepository.findByMapFilter(longitude, latitude, theme, interestedAge, kindOf, distance)
                        .stream().collect(Collectors.toMap(CenterAndDistancePreview::getId,
                        centerAndDistancePreview -> centerAndDistancePreview));
        List<Long> idList = new ArrayList<>(map.keySet());
        imageService.getEncodedProfileImage(imageService.getCenterProfileDir(), idList)
                .forEach((id, image) -> {
                    map.get(id).setImage(image);
                });
        return new ArrayList<>(map.values());
    }

    public CenterInfoResponseDto findInfoById(Long id) {
        Center center = centerRepository.findById(id)
                .orElseThrow(() -> new CenterException("해당 센터 존재하지 않음"));
        // Center 가 id 에 의해 조회 되었으므로 score에 1 추가
        center.addScore(Score.GET);
        CenterInfoResponseDto centerInfoResponseDto = new CenterInfoResponseDto(center);
        String imageDir = imageService.getCenterDir(id);
        centerInfoResponseDto.setImages(imageService.getEncodedInfoImage(imageDir, centerInfoResponseDto.getImgCnt()));
        return centerInfoResponseDto;
    }

    public CenterBannerDto findBannerById(Long id) {
        CenterBannerDto dto = centerRepository.findBannerById(id);
        String profileDir = imageService.getCenterProfileDir();
        dto.setProfileImage(imageService.getEncodedProfileImage(profileDir, id));
        return dto;
    }

    public Long modifyCenter(Long centerId, Long userId, CenterModifyRequestDto requestDto, List<MultipartFile> files) {
        userRepository.findTeacherById(userId)
                .orElseThrow(() -> new UserException("존재하지 않는 유저입니다"))
                .canWrite(centerId);
        // 해당하는 center 없으면 RuntimeException 반환
        Center center = centerRepository.findById(centerId).orElseThrow(RuntimeException::new);
        String centerDir = imageService.getCenterDir(centerId);
        imageService.saveInfoImage(files, centerDir);
        center.update(requestDto);
        center.updateImageCntAndVideoCnt(files, 0);
        return center.getId();
    }

    public List<String> findCenterForParent(Long userId) {
        Parent parent = parentRepository.findById(userId).orElseThrow(() -> new UserException("해당 유저가 존재 하지 않습니다."));
        Theme theme = parent.getTheme();
        List<Long> idList = centerRepository.findByThemeAndAgeOnly3(theme, PageRequest.of(0, 3, Sort.by("score")));
        return new ArrayList<>(imageService.getEncodedProfileImage(imageService.getCenterProfileDir(), idList).values());
    }

    /**
    *   작성날짜: 2022/06/24 10:28 AM
    *   작성자: 이승범
    *   작성내용: 회원가입 과정에서 필요한 센터정보 가져오기
    */
    public Slice<CenterInfoDto> findCenterForSignup(CenterInfoRequest request, Pageable pageable) {
       return centerRepository.findForSignup(request.getSido(), request.getSigungu(), request.getCenterName(), pageable);
    }

    /**
    *   작성날짜: 2022/06/24 10:31 AM
    *   작성자: 이승범
    *   작성내용: 아이추가 과정에서 필요한 센터정보 가져오기
    */
    public Slice<CenterInfoDto> findCenterForAddChild(CenterInfoRequest request, Pageable pageable) {
        System.out.println("request.getSido() = " + request.getSido());
        System.out.println("request.getSigungu() = " + request.getSigungu());
        System.out.println("request.getSido()==null = " + (request.getSido() == null));
        System.out.println("request.getSigungu()==null = " + (request.getSigungu() == null));
        return centerRepository.findCenterForAddChild(request.getSido(), request.getSigungu(), request.getCenterName(), pageable);
    }
}
