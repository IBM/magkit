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
 * Generic interface for all property conditions. Declared methods for the step that declares the value.
 * Allows providing only one value to be used for comparison or a bind variable name.
 *
 * @param <V> the type of the value
 * @author wolf.bubenik@aperto.com
 * @since (20.05.20)
 */
public interface Sql2StaticOperandSingle<V> {
    Sql2JoinConstraint value(V value);
    Sql2JoinConstraint bindVariable(String name);
}
