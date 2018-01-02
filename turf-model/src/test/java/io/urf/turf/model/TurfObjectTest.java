/*
 * Copyright © 2017 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.urf.turf.model;

import static org.junit.Assert.*;
import static com.github.npathai.hamcrestopt.OptionalMatchers.*;

import java.time.*;
import java.util.*;

import org.junit.*;

/**
 * Tests of {@link TurfObject}.
 * @author Garret Wilson
 */
public class TurfObjectTest {

	@Test
	public void testGetPropertyValue() {
		final TurfObject turfObject = new TurfObject();
		turfObject.setPropertyValue("joined", LocalDate.of(2016, Month.JANUARY, 23));

		final Optional<Object> optionalJoined = turfObject.getPropertyValue("joined");
		assertThat(optionalJoined, isPresentAndIs(LocalDate.parse("2016-01-23")));
	}

}
