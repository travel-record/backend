package world.trecord.domain.invitation;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum InvitationStatus {
    COMPLETED("초대 완료"), DECLINED("초대 거부"), EXPELLED("초대 취소");

    private String description;
}
