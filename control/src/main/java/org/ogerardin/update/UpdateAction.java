package org.ogerardin.update;

import org.ogerardin.update.action.UpdateContext;

public interface UpdateAction  {

    void perform(UpdateContext context) throws UpdateException;

    default UpdateAction getRollbackStep() {
        return null;
    }
}
