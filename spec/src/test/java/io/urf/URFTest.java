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

package io.urf;

import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static org.junit.Assert.*;

import java.net.URI;

import org.junit.*;

/**
 * Tests of URF definitions and utilities.
 * 
 * @author Garret Wilson
 *
 */
public class URFTest {

	//TODO add name tests

	/** @see URF.Tag#getNamespace(URI) */
	@Test
	public void testTagGetNamespace() {
		assertThat(URF.Tag.getNamespace(URI.create("https://urf.name/")), isEmpty());
		assertThat(URF.Tag.getNamespace(URI.create("https://urf.name/Example#foo")), isPresentAndIs(URI.create("https://urf.name/")));
		assertThat(URF.Tag.getNamespace(URI.create("https://urf.name/Example#123")), isPresentAndIs(URI.create("https://urf.name/")));
		assertThat(URF.Tag.getNamespace(URI.create("https://urf.name/Example")), isPresentAndIs(URI.create("https://urf.name/")));
		assertThat(URF.Tag.getNamespace(URI.create("https://urf.name/test")), isPresentAndIs(URI.create("https://urf.name/")));
		assertThat(URF.Tag.getNamespace(URI.create("https://urf.name/test/")), isPresentAndIs(URI.create("https://urf.name/")));
		assertThat(URF.Tag.getNamespace(URI.create("https://urf.name/foo/bar")), isPresentAndIs(URI.create("https://urf.name/foo/")));
		assertThat(URF.Tag.getNamespace(URI.create("https://urf.name/foo/bar/")), isPresentAndIs(URI.create("https://urf.name/foo/")));
		assertThat(URF.Tag.getNamespace(URI.create("https://urf.name/foo/bar/123")), isPresentAndIs(URI.create("https://urf.name/foo/bar/")));
		assertThat(URF.Tag.getNamespace(URI.create("https://urf.name/foo/bar/Example")), isPresentAndIs(URI.create("https://urf.name/foo/bar/")));
		assertThat(URF.Tag.getNamespace(URI.create("https://urf.name/foo123/bar/Example")), isPresentAndIs(URI.create("https://urf.name/foo123/bar/")));
		assertThat(URF.Tag.getNamespace(URI.create("https://urf.name/foo/123/Example")), isPresentAndIs(URI.create("https://urf.name/foo/123/")));
	}

	/** @see URF.Tag#getName(URI) */
	@Test
	public void testTagGetName() {
		assertThat(URF.Tag.getName(URI.create("https://urf.name/")), isEmpty());
		assertThat(URF.Tag.getName(URI.create("https://urf.name/Example#foo")), isPresentAndIs("Example#foo"));
		assertThat(URF.Tag.getName(URI.create("https://urf.name/Example#123")), isPresentAndIs("Example#123"));
		assertThat(URF.Tag.getName(URI.create("https://urf.name/Example")), isPresentAndIs("Example"));
		assertThat(URF.Tag.getName(URI.create("https://urf.name/test")), isPresentAndIs("test"));
		assertThat(URF.Tag.getName(URI.create("https://urf.name/test/")), isEmpty());
		assertThat(URF.Tag.getName(URI.create("https://urf.name/foo/bar")), isPresentAndIs("bar"));
		assertThat(URF.Tag.getName(URI.create("https://urf.name/foo/bar/")), isEmpty());
		assertThat(URF.Tag.getName(URI.create("https://urf.name/foo/bar/123")), isEmpty());
		assertThat(URF.Tag.getName(URI.create("https://urf.name/foo/bar/Example")), isPresentAndIs("Example"));
		assertThat(URF.Tag.getName(URI.create("https://urf.name/foo123/bar/Example")), isPresentAndIs("Example"));
		assertThat(URF.Tag.getName(URI.create("https://urf.name/foo/123/Example")), isPresentAndIs("Example"));
	}

	/** @see URF.Tag#getId(URI) */
	@Test
	public void testTagGetId() {
		assertThat(URF.Tag.getId(URI.create("https://urf.name/")), isEmpty());
		assertThat(URF.Tag.getId(URI.create("https://urf.name/Example#foo")), isPresentAndIs("foo"));
		assertThat(URF.Tag.getId(URI.create("https://urf.name/Example#123")), isPresentAndIs("123"));
		assertThat(URF.Tag.getId(URI.create("https://urf.name/Example")), isEmpty());
		assertThat(URF.Tag.getId(URI.create("https://urf.name/test")), isEmpty());
		assertThat(URF.Tag.getId(URI.create("https://urf.name/test/")), isEmpty());
		assertThat(URF.Tag.getId(URI.create("https://urf.name/foo/bar")), isEmpty());
		assertThat(URF.Tag.getId(URI.create("https://urf.name/foo/bar/")), isEmpty());
		assertThat(URF.Tag.getId(URI.create("https://urf.name/foo/bar/123")), isEmpty());
		assertThat(URF.Tag.getId(URI.create("https://urf.name/foo/bar/Example")), isEmpty());
		assertThat(URF.Tag.getId(URI.create("https://urf.name/foo123/bar/Example")), isEmpty());
		assertThat(URF.Tag.getId(URI.create("https://urf.name/foo/123/Example")), isEmpty());
	}
}
