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

package io.urf.turf;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static com.github.npathai.hamcrestopt.OptionalMatchers.*;

import java.net.URI;

import org.junit.*;

/**
 * Tests of TURF definitions and utilities.
 * 
 * @author Garret Wilson
 *
 */
public class TURFTest {

	/** @see TURF.Handle#fromTag(URI) */
	@Test
	public void testHandleFromTag() {
		assertThat(TURF.Handle.fromTag(URI.create("https://urf.name/")), isEmpty());
		assertThat(TURF.Handle.fromTag(URI.create("https://urf.name/Example")), isPresentAndIs("Example"));
		assertThat(TURF.Handle.fromTag(URI.create("https://urf.name/Example#foo")), isPresentAndIs("Example#foo"));
		assertThat(TURF.Handle.fromTag(URI.create("https://urf.name/Example#123")), isPresentAndIs("Example#123"));
		assertThat(TURF.Handle.fromTag(URI.create("https://urf.name/test")), isPresentAndIs("test"));
		assertThat(TURF.Handle.fromTag(URI.create("https://urf.name/test/")), isEmpty());
		assertThat(TURF.Handle.fromTag(URI.create("https://urf.name/foo/bar")), isPresentAndIs("foo-bar"));
		assertThat(TURF.Handle.fromTag(URI.create("https://urf.name/foo/bar/")), isEmpty());
		assertThat(TURF.Handle.fromTag(URI.create("https://urf.name/foo/bar/Example")), isPresentAndIs("foo-bar-Example"));
		assertThat(TURF.Handle.fromTag(URI.create("https://urf.name/foo/bar/Example#test123")), isPresentAndIs("foo-bar-Example#test123"));
	}

	/** @see TURF.Handle#toTag(String) */
	@Test
	public void testHandleToTag() {
		assertThat(TURF.Handle.toTag("Example"), is(URI.create("https://urf.name/Example")));
		assertThat(TURF.Handle.toTag("Example#foo"), is(URI.create("https://urf.name/Example#foo")));
		assertThat(TURF.Handle.toTag("Example#123"), is(URI.create("https://urf.name/Example#123")));
		assertThat(TURF.Handle.toTag("test"), is(URI.create("https://urf.name/test")));
		assertThat(TURF.Handle.toTag("foo-bar"), is(URI.create("https://urf.name/foo/bar")));
		assertThat(TURF.Handle.toTag("foo-bar-Example"), is(URI.create("https://urf.name/foo/bar/Example")));
		assertThat(TURF.Handle.toTag("foo-bar-Example#test123"), is(URI.create("https://urf.name/foo/bar/Example#test123")));
	}

}
