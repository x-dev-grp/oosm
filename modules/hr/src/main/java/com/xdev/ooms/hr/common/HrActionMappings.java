package com.xdev.ooms.hr.common;

import com.xdev.ooms.sharedkernel.entities.BaseEntity;
import com.xdev.ooms.sharedkernel.models.Action;

import java.util.Set;

public final class HrActionMappings {

    private HrActionMappings() {
    }

    public static boolean isActive(BaseEntity entity) {
        return entity != null && !Boolean.TRUE.equals(entity.getDeleted());
    }

    public static void addRead(Set<Action> actions) {
        actions.add(Action.READ);
    }

    public static void addCrudIfActive(Set<Action> actions, BaseEntity entity) {
        addRead(actions);
        if (!isActive(entity)) {
            return;
        }
        actions.add(Action.UPDATE);
        actions.add(Action.DELETE);
    }
}
