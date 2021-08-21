package me.vixen.chopperbot.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This field is excluded from serialization when used with
 * {@link me.vixen.chopperbot.database.Database#DESERIALIZE_STRATEGY This exclusion strategy}
 */
@Target(ElementType.FIELD)
public @interface ExcludeDeserialize {
}
