
/*
 * Copyright 2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  *
 * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
     * Specifies with which configuration option server will be started.
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
