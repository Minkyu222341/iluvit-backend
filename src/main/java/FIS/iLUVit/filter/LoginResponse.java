package FIS.iLUVit.filter;

import FIS.iLUVit.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long id;
    private String nickname;
    private String auth;

    public LoginResponse(User user) {
        this.id = user.getId();
        this.nickname = user.getNickName();
        this.auth = user.getAuth().toString();
    }
}