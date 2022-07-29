package FIS.iLUVit.controller.dto;

import FIS.iLUVit.domain.enumtype.Auth;
import lombok.Data;

@Data
public class ParentInfoResponse extends UserInfoResponse {

    public ParentInfoResponse(Long id, String nickName, Auth auth) {
        super(id, nickName, auth);
    }
}
