package org.ogerardin.update;

public interface UpdateAction  {

    void perform(UpdateContext context) throws UpdateException;

    default UpdateAction getRollbackStep() {
        return null;
    }
}
