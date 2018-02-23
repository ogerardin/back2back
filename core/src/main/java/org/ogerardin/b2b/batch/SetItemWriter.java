package org.ogerardin.b2b.batch;

import org.springframework.batch.item.ItemWriter;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * An implementation of {@link org.springframework.batch.item.ItemWriter} that populates a specified
 * {@link java.util.Set}
 */
public class SetItemWriter<T> implements ItemWriter<T> {

    private final Set<T> set;

    public SetItemWriter(@NotNull Set<T> set) {
        this.set = set;
    }

    @Override
    public void write(List<? extends T> items) throws Exception {
        set.addAll(items);
    }

}
