package com.aperto.magkit.query.sql2.statement;

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
 * SQL2 statement builder interface for fluent API: Optional ordering step.
 *
 * @author wolf.bubenik@aperto.com
 * @since 15.04.2020
 */
public interface Sql2Order extends Sql2Builder {
    Sql2OrderDirection orderBy(String... attribute);
    Sql2OrderDirection orderByScore();
}
