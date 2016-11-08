
import transformations.AddNonExistentElementToMessagingSubsystem;
import transformations.DoNothing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * Annotation which specifies server config file from resources directory with which server should be started.
 *
 * If domain is tested then it allows to specify host.xml file to be used. Otherwise it's ignored.
 *
 * Used during tryStartAndWaitForFail of @see Server#tryStartAndWaitForFail()
 *
 * Created by mnovak on 10/24/16.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServerConfig {

    /**
     * Specifies with which confiuguration option server will be started.
     *
     * Default value for standalone mode is "standalone.xml"
     * Default value for domain mode is "domain.xml"
     */
    String configuration() default "standalone.xml";

    /**
     * Optional - will be applied only in domain mode. Same as --host-config=...
     *
     * Default is host.xml
     */
    String hostConfig() default "host.xml";

    Class xmlTransformationClass() default DoNothing.class;

}
