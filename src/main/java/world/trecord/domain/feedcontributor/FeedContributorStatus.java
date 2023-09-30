package world.trecord.domain.feedcontributor;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FeedContributorStatus {
    PARTICIPATING("피드에 참여중"),
    DELETED("피드가 삭제됨"),
    EXPELLED("피드에서 내보내짐"),
    LEFT("피드에서 나감");

    private final String description;
}
