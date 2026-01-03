package team.chisel.ctm.api.util;

import java.lang.annotation.*;

/**
 * An alternative to {@link javax.annotation.Nonnull} which works on type parameters (J8 feature).
 *
 * @deprecated Use {@link org.jetbrains.annotations.NotNull}. Kept in case some mods (like Chisel) use it.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Deprecated
public @interface NonnullType {
}
