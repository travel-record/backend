package world.trecord.domain.feedcontributor;

import java.util.EnumSet;
import java.util.Set;

public enum ManagerPermission {
    READ,       // record 읽기 권한
    WRITE,      // record 작성 권한
    MODIFY,     // record 수정 권한
    DELETE;      // record 삭제 권한

    public static Set<ManagerPermission> getAllPermissions() {
        return EnumSet.allOf(ManagerPermission.class);
    }
}
