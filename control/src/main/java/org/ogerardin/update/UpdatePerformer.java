package org.ogerardin.update;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
@Builder
public class UpdatePerformer implements Runnable {

    private final UpdateContext context;
    @Singular
    private final List<UpdateAction> steps;

    public void run() {

        List<UpdateAction> rollbackSteps = new ArrayList<>();

        try {
            for (int i = 0; i < steps.size(); i++) {
                UpdateAction step = steps.get(i);

                // store rollback action if there is one
                UpdateAction rollbackStep = step.getRollbackStep();
                if (rollbackStep != null) {
                    rollbackSteps.add(rollbackStep);
                }

                log.info("Performing update step {}/{}: {}", i+1, steps.size(), step);
                step.perform(context);
                log.info("...done performing update step {}", i+1);
            }
        } catch (UpdateException e) {
            log.error("Update step failed", e);
            try {
                log.info("Attempting rollback");
                rollback(rollbackSteps);
            } catch (UpdateException ue) {
                throw new RuntimeException("Rollback failed, giving up", ue);
            }
        }
    }

    private void rollback(List<UpdateAction> rollbackSteps) throws UpdateException {
        try {
            for (int i = 0; i < rollbackSteps.size(); i++) {
                // rollback steps
                UpdateAction rollbackStep = rollbackSteps.get(rollbackSteps.size() - i - 1);
                log.info("Performing rollback step {}/{}: {}", i+1, rollbackSteps.size(), rollbackStep);
                rollbackStep.perform(context);
                log.info("...done performing rollback step {}", i+1);
            }
        } catch (UpdateException e) {
            throw new UpdateException("Rollback step failed", e);
        }
    }

}
