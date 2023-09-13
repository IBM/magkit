package de.ibmix.magkit.query;

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

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static de.ibmix.magkit.query.DateUtils.createQueryDate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Test for DateUtils.
 *
 * @author frank.sommer
 * @since 08.04.13
 */
public class DateUtilsTest {

    @Test
    public void testCreateQueryDate() throws Exception {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = isoFormat.parse("2010-05-23T09:01:02");
        String queryDate = createQueryDate(date);
        assertThat(queryDate, equalTo("2010-05-23T11:01:02.000+02:00"));
    }
}
