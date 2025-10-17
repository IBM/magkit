package de.ibmix.magkit.query.sql2.condition;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2023 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * Entry comparison API for String properties BEFORE calling {@code not()} allowing a negation.
 * Extends {@link Sql2CompareString} with {@link #not()} which then returns a narrowed API without another
 * negation option to avoid double NOT misuse.
 *
 * Thread-safety: Not thread safe.
 * Null handling: Null/empty values ignored when rendering.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-05-26
 */
public interface Sql2CompareStringNot extends Sql2CompareString {
    /**
     * Negate the upcoming string comparison operations.
     *
     * @return string comparison API without further not()
     */
    Sql2CompareString not();
}
