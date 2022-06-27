package FIS.iLUVit.domain.alarms;

import FIS.iLUVit.domain.Center;
import FIS.iLUVit.domain.User;
import FIS.iLUVit.service.AlarmUtils;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@NoArgsConstructor
public class CenterApprovalAcceptedAlarm extends Alarm{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centerId")
    private Center center;

    public CenterApprovalAcceptedAlarm(User user, Center center) {
        super(user);
        this.mode = AlarmUtils.CENTER_APPROVAL_ACCEPTED;
        this.center = center;
        String args[] = {center.getName()};
        message = AlarmUtils.getMessage(mode, args);
    }
}
