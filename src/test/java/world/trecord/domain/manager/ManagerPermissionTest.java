package world.trecord.domain.manager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static world.trecord.domain.manager.ManagerPermission.*;

class ManagerPermissionTest {

    @Test
    @DisplayName("모든 권한을 가져온다")
    void getAllPermissionsTest() throws Exception {
        //when
        Set<ManagerPermission> allPermissions = getAllPermissions();

        //then
        Assertions.assertThat(allPermissions)
                .containsAll(List.of(READ, WRITE, MODIFY, DELETE));
    }
}