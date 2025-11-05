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
 * Interface for name conditions providing the single value step.
 * Thread-safety: Not thread safe.
 * Null handling: Null value results in an empty condition.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-11-11
 */
public interface Sql2NameOperandSingle {
    /**
     * Provide the single node name value to compare with.
     *
     * @param value node name (may be null)
     * @return next step (join selector decision)
     */
    Sql2JoinConstraint value(String value);
}
