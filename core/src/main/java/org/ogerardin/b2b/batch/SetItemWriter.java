package org.ogerardin.b2b.batch;

import lombok.NonNull;
import org.springframework.batch.item.ItemWriter;

import java.util.List;
import java.util.Set;

/**
 * An implementation of {@link org.springframework.batch.item.ItemWriter} that populates a specified
 * {@link java.util.Set}
 */
public class SetItemWriter<T> implements ItemWriter<T> {

    private final Set<T> set;

    public SetItemWriter(@NonNull Set<T> set) {
        this.set = set;
    }

    @Override
    public void write(List<? extends T> items) throws Exception {
        set.addAll(items);
    }

}
