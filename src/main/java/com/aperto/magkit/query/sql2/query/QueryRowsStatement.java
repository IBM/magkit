package com.aperto.magkit.query.sql2.query;

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

import com.aperto.magkit.query.sql2.statement.Sql2Builder;

/**
 * The RowsQueryBuilder step interface declaring methods for the statement.
 *
 * @param <T> the type of Sql2QueryBuilder to be returned by methods
 * @author wolf.bubenik@aperto.com
 * @since (28.04.20)
 */
public interface QueryRowsStatement<T> {
    T withStatement(Sql2Builder statementBuilder);
    T withStatement(String sql2);
}
