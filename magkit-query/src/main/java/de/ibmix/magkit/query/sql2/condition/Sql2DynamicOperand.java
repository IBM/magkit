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
 * Builder Interface for String conditions that declares usage of jcr methods for strings.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-05-20
 */
public interface Sql2DynamicOperand extends Sql2CompareStringNot {
    Sql2CompareStringNot lowerCase();
    Sql2CompareStringNot upperCase();
    Sql2CompareNot<Long> length();
}
