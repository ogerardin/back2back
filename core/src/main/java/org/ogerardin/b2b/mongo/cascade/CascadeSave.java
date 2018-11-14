package org.ogerardin.b2b.mongo.cascade;

import org.springframework.data.mongodb.core.mapping.DBRef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated field, which is expected to be annotated with
 * {@link DBRef}, will be saved when its containing document is saved.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CascadeSave {
}