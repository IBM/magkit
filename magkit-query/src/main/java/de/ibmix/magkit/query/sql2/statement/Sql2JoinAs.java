package de.ibmix.magkit.query.sql2.statement;

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
 * Fluent API step after choosing a JOIN type to assign a selector name for the joined node type.
 * <p>The selector name allows scoping conditions and attribute references for the joined selector.
 * If omitted (null/blank) subsequent conditions referencing the join will be invalid.</p>
 * <p>Thread-safety: Not thread-safe; builder state is mutated.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-05-18
 */
public interface Sql2JoinAs {
    /**
     * Assign the selector name to the joined node type.
     * @param joinSelectorName alias for the joined selector (may be null/blank to skip)
     * @return next step to define the ON join condition
     */
    Sql2JoinOn joinAs(String joinSelectorName);
}
