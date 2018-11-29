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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.util.*;

import org.junit.*;

/**
 * Tests of URF definitions and utilities.
 * 
 * @author Garret Wilson
 */
public class URFTest {

	//TODO add name tests

	//#URF.Tag

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

	//#URF.Handle

	/** @see URF.Handle#PATTERN */
	@Test
	public void testHandlePattern() {
		//TODO test the groups
		assertThat(URF.Handle.PATTERN.matcher("Example").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("Example#foo").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("Example#123").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("Bad Example").matches(), is(false));
		assertThat(URF.Handle.PATTERN.matcher("Bad Example#foo").matches(), is(false));
		assertThat(URF.Handle.PATTERN.matcher("Bad Example#123").matches(), is(false));
		assertThat(URF.Handle.PATTERN.matcher("foo").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("foo-bar").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("foo-bar-Example").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("foo-bar-Example#test123").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/Example").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/Example#foo").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/Example#123").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/Bad Example").matches(), is(false));
		assertThat(URF.Handle.PATTERN.matcher("test/Bad Example#foo").matches(), is(false));
		assertThat(URF.Handle.PATTERN.matcher("test/Bad Example#123").matches(), is(false));
		assertThat(URF.Handle.PATTERN.matcher("test/foo").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/foo-bar").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/foo-bar-Example").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/foo-bar-Example#foo").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/foo-bar-Example#123").matches(), is(true));
	}

	/** @see URF.Handle#fromTag(URI) */
	@Test
	public void testHandleFromTag() {
		final Map<URI, String> aliases = new HashMap<>();
		aliases.put(URI.create("https://example.com/fake/"), "fake");
		assertThat(URF.Handle.fromTag(URI.create("https://urf.name/"), aliases), isEmpty());
		assertThat(URF.Handle.fromTag(URI.create("https://urf.name/Example"), aliases), isPresentAndIs("Example"));
		assertThat(URF.Handle.fromTag(URI.create("https://urf.name/Example#foo"), aliases), isPresentAndIs("Example#foo"));
		assertThat(URF.Handle.fromTag(URI.create("https://urf.name/Example#123"), aliases), isPresentAndIs("Example#123"));
		assertThat(URF.Handle.fromTag(URI.create("https://urf.name/test"), aliases), isPresentAndIs("test"));
		assertThat(URF.Handle.fromTag(URI.create("https://urf.name/test/"), aliases), isEmpty());
		assertThat(URF.Handle.fromTag(URI.create("https://urf.name/foo/bar"), aliases), isPresentAndIs("foo-bar"));
		assertThat(URF.Handle.fromTag(URI.create("https://urf.name/foo/bar/"), aliases), isEmpty());
		assertThat(URF.Handle.fromTag(URI.create("https://urf.name/foo/bar/Example"), aliases), isPresentAndIs("foo-bar-Example"));
		assertThat(URF.Handle.fromTag(URI.create("https://urf.name/foo/bar/Example#test123"), aliases), isPresentAndIs("foo-bar-Example#test123"));
		assertThat(URF.Handle.fromTag(URI.create("https://example.com/fake/Example"), aliases), isPresentAndIs("fake/Example"));
		assertThat(URF.Handle.fromTag(URI.create("https://example.com/fake/Example#foo"), aliases), isPresentAndIs("fake/Example#foo"));
		assertThat(URF.Handle.fromTag(URI.create("https://example.com/fake/Example#123"), aliases), isPresentAndIs("fake/Example#123"));
		assertThat(URF.Handle.fromTag(URI.create("https://example.com/fake/test"), aliases), isPresentAndIs("fake/test"));
		assertThat(URF.Handle.fromTag(URI.create("https://example.com/fake/Example"), aliases), isPresentAndIs("fake/Example"));
		assertThat(URF.Handle.fromTag(URI.create("https://example.com/fake/foo/bar"), aliases), isEmpty());
		assertThat(URF.Handle.fromTag(URI.create("https://example.com/fake/foo/bar/Example"), aliases), isEmpty());
		assertThat(URF.Handle.fromTag(URI.create("https://example.com/fake/foo/bar/Example#foo"), aliases), isEmpty());
		assertThat(URF.Handle.fromTag(URI.create("https://example.com/fake/foo/bar/Example#123"), aliases), isEmpty());
	}

	/** @see URF.Handle#toTag(String) */
	@Test
	public void testHandleToTag() {
		final Map<String, URI> namespaces = new HashMap<>();
		namespaces.put("fake", URI.create("https://example.com/fake/"));
		assertThat(URF.Handle.toTag("Example", namespaces), is(URI.create("https://urf.name/Example")));
		assertThat(URF.Handle.toTag("Example#foo", namespaces), is(URI.create("https://urf.name/Example#foo")));
		assertThat(URF.Handle.toTag("Example#123", namespaces), is(URI.create("https://urf.name/Example#123")));
		assertThat(URF.Handle.toTag("test", namespaces), is(URI.create("https://urf.name/test")));
		assertThat(URF.Handle.toTag("foo-bar", namespaces), is(URI.create("https://urf.name/foo/bar")));
		assertThat(URF.Handle.toTag("foo-bar-Example", namespaces), is(URI.create("https://urf.name/foo/bar/Example")));
		assertThat(URF.Handle.toTag("foo-bar-Example#foo", namespaces), is(URI.create("https://urf.name/foo/bar/Example#foo")));
		assertThat(URF.Handle.toTag("foo-bar-Example#123", namespaces), is(URI.create("https://urf.name/foo/bar/Example#123")));
		assertThat(URF.Handle.toTag("fake/Example", namespaces), is(URI.create("https://example.com/fake/Example")));
		assertThat(URF.Handle.toTag("fake/Example#foo", namespaces), is(URI.create("https://example.com/fake/Example#foo")));
		assertThat(URF.Handle.toTag("fake/Example#123", namespaces), is(URI.create("https://example.com/fake/Example#123")));
		assertThat(URF.Handle.toTag("fake/test", namespaces), is(URI.create("https://example.com/fake/test")));
		assertThat(URF.Handle.toTag("fake/Example", namespaces), is(URI.create("https://example.com/fake/Example")));
	}

	/** @see URF.Handle#toTag(String) */
	@Test(expected = IllegalArgumentException.class)
	public void testHandleToTagSegmentsInOtherNamespacesNotAllowed() {
		URF.Handle.toTag("fake/foo-bar");
	}

	/** @see URF.Handle#toTag(String) */
	@Test(expected = IllegalArgumentException.class)
	public void testHandleToTagSegmentsClassNameInOtherNamespacesNotAllowed() {
		URF.Handle.toTag("fake/foo-bar-Example");
	}

	/** @see URF.Handle#toTag(String) */
	@Test(expected = IllegalArgumentException.class)
	public void testHandleToTagSegmentsIDInOtherNamespacesNotAllowed() {
		URF.Handle.toTag("fake/foo-bar-Example#foo");
	}

	/** @see URF.Handle#toTag(String) */
	@Test(expected = IllegalArgumentException.class)
	public void testHandleToTagSegmentsIDNumberInOtherNamespacesNotAllowed() {
		URF.Handle.toTag("fake/foo-bar-Example#123");
	}

}
