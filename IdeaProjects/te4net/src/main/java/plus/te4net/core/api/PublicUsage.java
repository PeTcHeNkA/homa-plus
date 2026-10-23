package plus.te4net.core.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;


@PublicUsage
@Target(ElementType.TYPE)
public @interface PublicUsage {
    /**
     * Mark class as Public API available
     */
}