package FIS.iLUVit.domain.alarms;

import FIS.iLUVit.dto.alarm.AlarmDto;
import FIS.iLUVit.domain.Center;
import FIS.iLUVit.domain.Presentation;
import FIS.iLUVit.domain.User;
import FIS.iLUVit.service.AlarmUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
public class PresentationFullAlarm extends Alarm{

    @Column(name = "presentationId")
    private Long presentationId;

    @Column(name = "centerId")
    private Long centerId;

    @Column(name = "centerName")
    private String centerName;

    public PresentationFullAlarm(User user, Presentation presentation, Center center) {
        super(user);
        this.mode = AlarmUtils.PRESENTATION_APPLICANTS_FULL;
        this.presentationId = presentation.getId();
        this.centerId = center.getId();
        this.centerName = center.getName();
        message = AlarmUtils.getMessage(mode, null);
    }

    @Override
    public AlarmDto exportAlarm() {
        return new PresentationFullAlarmDto(id, createdDate, message, dtype, presentationId, centerId, centerName);
    }

    @Getter
    public static class PresentationFullAlarmDto extends AlarmDto {

        protected Long presentationId;
        protected Long centerId;
        protected String centerName;

        public PresentationFullAlarmDto(Long id, LocalDateTime createdDate, String message, String type, Long presentationId, Long centerId, String centerName) {
            super(id, createdDate, message, type);
            this.presentationId = presentationId;
            this.centerId = centerId;
            this.centerName = centerName;
        }
    }
}
