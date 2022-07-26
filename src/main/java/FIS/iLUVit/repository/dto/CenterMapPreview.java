package FIS.iLUVit.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class CenterMapPreview {
    private Long id;
    private String name;                    // 시설명
    private Double longitude;               // 경도
    private Double latitude;                // 위도

    @QueryProjection
    public CenterMapPreview(Long id, String name, Double longitude, Double latitude) {
        this.id = id;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
