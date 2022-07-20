package FIS.iLUVit.domain.alarms;

import FIS.iLUVit.controller.dto.AlarmDto;
import FIS.iLUVit.domain.User;
import FIS.iLUVit.service.AlarmUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
public class ChatAlarm extends Alarm{

    @JoinColumn(name = "senderId")
    @ManyToOne(fetch = FetchType.LAZY)
    private User sender;
    private Boolean anonymous;
    private String profileImage;

    public ChatAlarm(User user, User sender, Boolean anonymous) {
        super(user);
        this.mode = AlarmUtils.CHAT_RECEIVED;
        this.sender = sender;
        this.anonymous = anonymous;
        this.profileImage = anonymous ? null : sender.getProfileImagePath();
        String[] args = anonymous ? new String[]{"익명"} : new String[]{sender.getNickName()};
        this.message = AlarmUtils.getMessage(mode, args);
    }

    @Override
    public AlarmDto exportAlarm() {
        return anonymous ? new ChatAlarmDto(id, createdDate, message, dtype, null, true, profileImage)
        : new ChatAlarmDto(id, createdDate, message, dtype, sender.getId(), false, profileImage);
    }

    @Getter
    public static class ChatAlarmDto extends AlarmDto{

        protected Long senderId;
        protected Boolean anonymous;
        protected String profileImage;

        public ChatAlarmDto(Long id, LocalDateTime createdDate, String message, String type, Long senderId, Boolean anonymous, String profileImage) {
            super(id, createdDate, message, type);
            this.senderId = senderId;
            this.anonymous = anonymous;
            this.profileImage = profileImage;
        }
    }
}
