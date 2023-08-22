package world.trecord.domain.users;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import world.trecord.domain.feed.FeedEntity;

class UserEntityTest {

    @Test
    @DisplayName("사용자 정보 업데이트 테스트")
    void updateTest() throws Exception {
        //given
        String changeNickname = "after nickname";
        String changeIntroduction = "after introduction";
        String changeImageUrl = "after image url";

        UserEntity userEntity = UserEntity.builder()
                .nickname("before nickname")
                .introduction("before introduction")
                .imageUrl("before image url")
                .build();

        //when
        userEntity.update(changeNickname, changeImageUrl, changeIntroduction);

        //then
        Assertions.assertThat(userEntity.getNickname()).isEqualTo(changeNickname);
        Assertions.assertThat(userEntity.getImageUrl()).isEqualTo(changeImageUrl);
        Assertions.assertThat(userEntity.getIntroduction()).isEqualTo(changeIntroduction);
    }

    @Test
    @DisplayName("사용자가 피드 매니저이면 true를 반환한다")
    void isManagerOfWhenUserIsManagerOfFeedTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .nickname("before nickname")
                .introduction("before introduction")
                .imageUrl("before image url")
                .build();

        FeedEntity feedEntity = FeedEntity.builder()
                .userEntity(userEntity)
                .build();

        //when
        boolean result = userEntity.isManagerOf(feedEntity);

        //then
        Assertions.assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("사용자가 피드 매니저가 아니면 false를 반환한다")
    void isManagerOfWhenUserIsNotManagerOfFeedTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .build();

        UserEntity otherEntity = UserEntity.builder()
                .build();

        FeedEntity feedEntity = FeedEntity.builder()
                .userEntity(otherEntity)
                .build();

        //when
        boolean result = userEntity.isManagerOf(feedEntity);

        //then
        Assertions.assertThat(result).isFalse();
    }


}