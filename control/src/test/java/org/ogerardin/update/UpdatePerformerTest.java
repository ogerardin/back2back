package org.ogerardin.update;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class UpdatePerformerTest {


    @Test
    void run_success() throws UpdateException {
        UpdateAction a1 = mock(UpdateAction.class);
        UpdateAction a2 = mock(UpdateAction.class);
        UpdateAction a3 = mock(UpdateAction.class);

        UpdateContext context = mock(UpdateContext.class);

        UpdatePerformer updatePerformer = UpdatePerformer.builder()
                .context(context)
                .step(a1)
                .step(a2)
                .step(a3)
                .build();

        updatePerformer.run();

        verify(a1).perform(context);
        verify(a2).perform(context);
        verify(a3).perform(context);
    }

    @Test
    void run_rollback() throws UpdateException {
        UpdateContext context = mock(UpdateContext.class);

        UpdateAction a1 = mock(UpdateAction.class);
        UpdateAction r1 = mock(UpdateAction.class);
        when(a1.getRollbackStep()).thenReturn(r1);

        UpdateAction a2 = mock(UpdateAction.class);
        UpdateAction r2 = mock(UpdateAction.class);
        when(a2.getRollbackStep()).thenReturn(r2);

        UpdateAction a3 = mock(UpdateAction.class);
        UpdateAction r3 = mock(UpdateAction.class);
        when(a3.getRollbackStep()).thenReturn(r3);
        doThrow(new UpdateException("mock")).when(a3).perform(context);

        UpdatePerformer updatePerformer = UpdatePerformer.builder()
                .context(context)
                .step(a1)
                .step(a2)
                .step(a3)
                .build();

        updatePerformer.run();

        verify(a1).perform(context);
        verify(a2).perform(context);
        verify(a3).perform(context);
        verify(r3).perform(context);
        verify(r2).perform(context);
        verify(r1).perform(context);
    }
}