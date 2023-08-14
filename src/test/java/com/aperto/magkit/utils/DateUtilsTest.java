package com.aperto.magkit.utils;

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

import static com.aperto.magkit.utils.DateUtils.createQueryDate;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Date;

import org.junit.Test;

/**
 * Test for DateUtils.
 *
 * @author frank.sommer
 * @since 08.04.13
 */
public class DateUtilsTest {

    @Test
    public void testCreateQueryDate() throws Exception {
        Date date = new Date(1234567890);
        String queryDate = createQueryDate(date);
        assertThat(queryDate, equalTo("1970-01-15T07:56:07.890+01:00"));
    }
}
