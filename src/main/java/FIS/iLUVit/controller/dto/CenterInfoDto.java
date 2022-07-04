package FIS.iLUVit.controller.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CenterInfoDto {
    private Long id;
    private String name;
    private String address;

    @QueryProjection
    public CenterInfoDto(Long id, String centerName, String centerAddress) {
        this.id = id;
        this.name = centerName;
        this.address = centerAddress;
    }
}