package org.ogerardin.b2b.mongo.cascade;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that indicates the annotated field, which is expected to also be anotated with {@link com.mongodb.DBRef},
 * will be saved when its parent document is saved.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CascadeSave {
}