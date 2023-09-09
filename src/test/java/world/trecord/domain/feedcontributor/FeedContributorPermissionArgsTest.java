package world.trecord.domain.feedcontributor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import world.trecord.domain.feedcontributor.args.FeedContributorPermissionArgs;


class FeedContributorPermissionArgsTest {

    @Test
    @DisplayName("피드 컨트리부터는 레코드에 대한 모든 권한을 가진다")
    void getAllPermissionsTest() throws Exception {
        //given
        FeedContributorPermissionArgs permission = new FeedContributorPermissionArgs();

        //when //then
        Assertions.assertThat(permission.getRecordPermission())
                .extracting("read", "write", "modify", "delete")
                .containsExactly(true, true, true, true);
    }
}