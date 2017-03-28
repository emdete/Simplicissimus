package de.emdete.thinstore;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// @Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Col {
    public String name();
    public String value();
}
