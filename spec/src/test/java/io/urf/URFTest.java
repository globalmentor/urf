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

	/** @see URF.Tag#findNamespace(URI) */
	@Test
	public void testTagFindNamespace() {
		assertThat(URF.Tag.findNamespace(URI.create("https://urf.name/")), isEmpty());
		assertThat(URF.Tag.findNamespace(URI.create("https://urf.name/Example#foo")), isPresentAndIs(URI.create("https://urf.name/")));
		assertThat(URF.Tag.findNamespace(URI.create("https://urf.name/Example#123")), isPresentAndIs(URI.create("https://urf.name/")));
		assertThat(URF.Tag.findNamespace(URI.create("https://urf.name/Example#foo:bar")), isPresentAndIs(URI.create("https://urf.name/")));
		assertThat(URF.Tag.findNamespace(URI.create("https://urf.name/Example")), isPresentAndIs(URI.create("https://urf.name/")));
		assertThat(URF.Tag.findNamespace(URI.create("https://urf.name/test")), isPresentAndIs(URI.create("https://urf.name/")));
		assertThat(URF.Tag.findNamespace(URI.create("https://urf.name/test+")), isPresentAndIs(URI.create("https://urf.name/")));
		assertThat(URF.Tag.findNamespace(URI.create("https://urf.name/test/")), isPresentAndIs(URI.create("https://urf.name/")));
		assertThat(URF.Tag.findNamespace(URI.create("https://urf.name/false")), isPresentAndIs(URI.create("https://urf.name/")));
		assertThat(URF.Tag.findNamespace(URI.create("https://urf.name/true")), isPresentAndIs(URI.create("https://urf.name/")));
		assertThat(URF.Tag.findNamespace(URI.create("https://urf.name/foo/bar")), isPresentAndIs(URI.create("https://urf.name/foo/")));
		assertThat(URF.Tag.findNamespace(URI.create("https://urf.name/foo/bar+")), isPresentAndIs(URI.create("https://urf.name/foo/")));
		assertThat(URF.Tag.findNamespace(URI.create("https://urf.name/foo/bar/")), isPresentAndIs(URI.create("https://urf.name/foo/")));
		assertThat(URF.Tag.findNamespace(URI.create("https://urf.name/foo/bar/123")), isPresentAndIs(URI.create("https://urf.name/foo/bar/")));
		assertThat(URF.Tag.findNamespace(URI.create("https://urf.name/foo/bar/Example")), isPresentAndIs(URI.create("https://urf.name/foo/bar/")));
		assertThat(URF.Tag.findNamespace(URI.create("https://urf.name/foo/bar/Example+")), isPresentAndIs(URI.create("https://urf.name/foo/bar/")));
		assertThat(URF.Tag.findNamespace(URI.create("https://urf.name/foo123/bar/Example")), isPresentAndIs(URI.create("https://urf.name/foo123/bar/")));
		assertThat(URF.Tag.findNamespace(URI.create("https://urf.name/foo/123/Example")), isPresentAndIs(URI.create("https://urf.name/foo/123/")));
	}

	/** @see URF.Tag#findName(URI) */
	@Test
	public void testTagFindName() {
		assertThat(URF.Tag.findName(URI.create("https://urf.name/")), isEmpty());
		assertThat(URF.Tag.findName(URI.create("https://urf.name/Example#foo")), isPresentAndIs("Example#foo"));
		assertThat(URF.Tag.findName(URI.create("https://urf.name/Example+#foo")), isPresentAndIs("Example+#foo"));
		assertThat(URF.Tag.findName(URI.create("https://urf.name/Example#123")), isPresentAndIs("Example#123"));
		assertThat(URF.Tag.findName(URI.create("https://urf.name/Example#foo:bar")), isEmpty());
		assertThat(URF.Tag.findName(URI.create("https://urf.name/Example")), isPresentAndIs("Example"));
		assertThat(URF.Tag.findName(URI.create("https://urf.name/test")), isPresentAndIs("test"));
		assertThat(URF.Tag.findName(URI.create("https://urf.name/test+")), isPresentAndIs("test+"));
		assertThat(URF.Tag.findName(URI.create("https://urf.name/test/")), isEmpty());
		assertThat(URF.Tag.findName(URI.create("https://urf.name/false")), isPresentAndIs("false"));
		assertThat(URF.Tag.findName(URI.create("https://urf.name/true")), isPresentAndIs("true"));
		assertThat(URF.Tag.findName(URI.create("https://urf.name/foo/bar")), isPresentAndIs("bar"));
		assertThat(URF.Tag.findName(URI.create("https://urf.name/foo/bar+")), isPresentAndIs("bar+"));
		assertThat(URF.Tag.findName(URI.create("https://urf.name/foo/bar/")), isEmpty());
		assertThat(URF.Tag.findName(URI.create("https://urf.name/foo/bar/123")), isEmpty());
		assertThat(URF.Tag.findName(URI.create("https://urf.name/foo/bar/Example")), isPresentAndIs("Example"));
		assertThat(URF.Tag.findName(URI.create("https://urf.name/foo/bar/Example+")), isPresentAndIs("Example+"));
		assertThat(URF.Tag.findName(URI.create("https://urf.name/foo123/bar/Example")), isPresentAndIs("Example"));
		assertThat(URF.Tag.findName(URI.create("https://urf.name/foo/123/Example")), isPresentAndIs("Example"));
	}

	/** @see URF.Tag#isNary(URI) */
	@Test
	public void testTagIsNary() {
		assertThat(URF.Tag.isNary(URI.create("https://urf.name/")), is(false));
		assertThat(URF.Tag.isNary(URI.create("https://urf.name/Example#foo")), is(false));
		assertThat(URF.Tag.isNary(URI.create("https://urf.name/Example+#foo")), is(true));
		assertThat(URF.Tag.isNary(URI.create("https://urf.name/Example#123")), is(false));
		assertThat(URF.Tag.isNary(URI.create("https://urf.name/Example#foo:bar")), is(false));
		assertThat(URF.Tag.isNary(URI.create("https://urf.name/Example")), is(false));
		assertThat(URF.Tag.isNary(URI.create("https://urf.name/test")), is(false));
		assertThat(URF.Tag.isNary(URI.create("https://urf.name/test+")), is(true));
		assertThat(URF.Tag.isNary(URI.create("https://urf.name/test/")), is(false));
		assertThat(URF.Tag.isNary(URI.create("https://urf.name/false")), is(false));
		assertThat(URF.Tag.isNary(URI.create("https://urf.name/true")), is(false));
		assertThat(URF.Tag.isNary(URI.create("https://urf.name/foo/bar")), is(false));
		assertThat(URF.Tag.isNary(URI.create("https://urf.name/foo/bar+")), is(true));
		assertThat(URF.Tag.isNary(URI.create("https://urf.name/foo/bar/")), is(false));
		assertThat(URF.Tag.isNary(URI.create("https://urf.name/foo/bar/123")), is(false));
		assertThat(URF.Tag.isNary(URI.create("https://urf.name/foo/bar/Example")), is(false));
		assertThat(URF.Tag.isNary(URI.create("https://urf.name/foo/bar/Example+")), is(true));
		assertThat(URF.Tag.isNary(URI.create("https://urf.name/foo123/bar/Example")), is(false));
		assertThat(URF.Tag.isNary(URI.create("https://urf.name/foo/123/Example")), is(false));
	}

	/** @see URF.Tag#findId(URI) */
	@Test
	public void testTagFindId() {
		assertThat(URF.Tag.findId(URI.create("https://urf.name/")), isEmpty());
		assertThat(URF.Tag.findId(URI.create("https://urf.name/Example#foo")), isPresentAndIs("foo"));
		assertThat(URF.Tag.findId(URI.create("https://urf.name/Example+#foo")), isPresentAndIs("foo"));
		assertThat(URF.Tag.findId(URI.create("https://urf.name/Example#123")), isPresentAndIs("123"));
		assertThat(URF.Tag.findId(URI.create("https://urf.name/Example#foo:bar")), isPresentAndIs("foo:bar"));
		assertThat(URF.Tag.findId(URI.create("https://urf.name/Example")), isEmpty());
		assertThat(URF.Tag.findId(URI.create("https://urf.name/test")), isEmpty());
		assertThat(URF.Tag.findId(URI.create("https://urf.name/test+")), isEmpty());
		assertThat(URF.Tag.findId(URI.create("https://urf.name/test/")), isEmpty());
		assertThat(URF.Tag.findId(URI.create("https://urf.name/false")), isEmpty());
		assertThat(URF.Tag.findId(URI.create("https://urf.name/true")), isEmpty());
		assertThat(URF.Tag.findId(URI.create("https://urf.name/foo/bar")), isEmpty());
		assertThat(URF.Tag.findId(URI.create("https://urf.name/foo/bar+")), isEmpty());
		assertThat(URF.Tag.findId(URI.create("https://urf.name/foo/bar/")), isEmpty());
		assertThat(URF.Tag.findId(URI.create("https://urf.name/foo/bar/123")), isEmpty());
		assertThat(URF.Tag.findId(URI.create("https://urf.name/foo/bar/Example")), isEmpty());
		assertThat(URF.Tag.findId(URI.create("https://urf.name/foo/bar/Example+")), isEmpty());
		assertThat(URF.Tag.findId(URI.create("https://urf.name/foo123/bar/Example")), isEmpty());
		assertThat(URF.Tag.findId(URI.create("https://urf.name/foo/123/Example")), isEmpty());
	}

	//#URF.Handle

	/** @see URF.Handle#PATTERN */
	@Test
	public void testHandlePattern() {
		//TODO test the groups
		assertThat(URF.Handle.PATTERN.matcher("Example").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("Example+").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("Example#foo").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("Example#foo+").matches(), is(false));
		assertThat(URF.Handle.PATTERN.matcher("Example#123").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("BadExample#foo:bar").matches(), is(false));
		assertThat(URF.Handle.PATTERN.matcher("Bad Example").matches(), is(false));
		assertThat(URF.Handle.PATTERN.matcher("Bad Example#foo").matches(), is(false));
		assertThat(URF.Handle.PATTERN.matcher("Bad Example#123").matches(), is(false));
		assertThat(URF.Handle.PATTERN.matcher("foo").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("foo+").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("foo-bar").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("foo-bar+").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("foo-bar-Example").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("foo-bar-Example+").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("foo-bar-Example#test123").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("foo-bar-Example+#test123").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("foo-bar-Example#test123+").matches(), is(false));
		assertThat(URF.Handle.PATTERN.matcher("false").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("true").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/Example").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/Example+").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/Example#foo").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/Example#123").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/BadExample#foo:bar").matches(), is(false));
		assertThat(URF.Handle.PATTERN.matcher("test/Bad Example").matches(), is(false));
		assertThat(URF.Handle.PATTERN.matcher("test/Bad Example#foo").matches(), is(false));
		assertThat(URF.Handle.PATTERN.matcher("test/Bad Example#123").matches(), is(false));
		assertThat(URF.Handle.PATTERN.matcher("test/foo").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/foo+").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/foo-bar").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/foo-bar+").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/foo-bar-Example").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/foo-bar-Example+").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/foo-bar-Example#foo").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/foo-bar-Example#123").matches(), is(true));
		assertThat(URF.Handle.PATTERN.matcher("test/foo-bar-BadExample#foo:bar").matches(), is(false));
	}

	/** @see URF.Handle#findFromTag(URI) */
	@Test
	public void testHandleFindFromTag() {
		final Map<URI, String> aliases = new HashMap<>();
		aliases.put(URI.create("https://example.com/fake/"), "fake");
		assertThat(URF.Handle.findFromTag(URI.create("https://urf.name/"), aliases), isEmpty());
		assertThat(URF.Handle.findFromTag(URI.create("https://urf.name/Example"), aliases), isPresentAndIs("Example"));
		assertThat(URF.Handle.findFromTag(URI.create("https://urf.name/Example+"), aliases), isPresentAndIs("Example+"));
		assertThat(URF.Handle.findFromTag(URI.create("https://urf.name/Example#foo"), aliases), isPresentAndIs("Example#foo"));
		assertThat(URF.Handle.findFromTag(URI.create("https://urf.name/Example#123"), aliases), isPresentAndIs("Example#123"));
		assertThat(URF.Handle.findFromTag(URI.create("https://urf.name/Example#foo:bar"), aliases), isEmpty());
		assertThat(URF.Handle.findFromTag(URI.create("https://urf.name/test"), aliases), isPresentAndIs("test"));
		assertThat(URF.Handle.findFromTag(URI.create("https://urf.name/test+"), aliases), isPresentAndIs("test+"));
		assertThat(URF.Handle.findFromTag(URI.create("https://urf.name/test/"), aliases), isEmpty());
		assertThat(URF.Handle.findFromTag(URI.create("https://urf.name/false"), aliases), isPresentAndIs("false"));
		assertThat(URF.Handle.findFromTag(URI.create("https://urf.name/true"), aliases), isPresentAndIs("true"));
		assertThat(URF.Handle.findFromTag(URI.create("https://urf.name/foo/bar"), aliases), isPresentAndIs("foo-bar"));
		assertThat(URF.Handle.findFromTag(URI.create("https://urf.name/foo/bar+"), aliases), isPresentAndIs("foo-bar+"));
		assertThat(URF.Handle.findFromTag(URI.create("https://urf.name/foo/bar/"), aliases), isEmpty());
		assertThat(URF.Handle.findFromTag(URI.create("https://urf.name/foo/bar/Example"), aliases), isPresentAndIs("foo-bar-Example"));
		assertThat(URF.Handle.findFromTag(URI.create("https://urf.name/foo/bar/Example+"), aliases), isPresentAndIs("foo-bar-Example+"));
		assertThat(URF.Handle.findFromTag(URI.create("https://urf.name/foo/bar/Example#test123"), aliases), isPresentAndIs("foo-bar-Example#test123"));
		assertThat(URF.Handle.findFromTag(URI.create("https://example.com/fake/Example"), aliases), isPresentAndIs("fake/Example"));
		assertThat(URF.Handle.findFromTag(URI.create("https://example.com/fake/Example+"), aliases), isPresentAndIs("fake/Example+"));
		assertThat(URF.Handle.findFromTag(URI.create("https://example.com/fake/Example#foo"), aliases), isPresentAndIs("fake/Example#foo"));
		assertThat(URF.Handle.findFromTag(URI.create("https://example.com/fake/Example#123"), aliases), isPresentAndIs("fake/Example#123"));
		assertThat(URF.Handle.findFromTag(URI.create("https://example.com/fake/Example#foo:bar"), aliases), isEmpty());
		assertThat(URF.Handle.findFromTag(URI.create("https://example.com/fake/test"), aliases), isPresentAndIs("fake/test"));
		assertThat(URF.Handle.findFromTag(URI.create("https://example.com/fake/test+"), aliases), isPresentAndIs("fake/test+"));
		assertThat(URF.Handle.findFromTag(URI.create("https://example.com/fake/Example"), aliases), isPresentAndIs("fake/Example"));
		assertThat(URF.Handle.findFromTag(URI.create("https://example.com/fake/Example+"), aliases), isPresentAndIs("fake/Example+"));
		assertThat(URF.Handle.findFromTag(URI.create("https://example.com/fake/foo/bar"), aliases), isEmpty());
		assertThat(URF.Handle.findFromTag(URI.create("https://example.com/fake/foo/bar+"), aliases), isEmpty());
		assertThat(URF.Handle.findFromTag(URI.create("https://example.com/fake/foo/bar/Example"), aliases), isEmpty());
		assertThat(URF.Handle.findFromTag(URI.create("https://example.com/fake/foo/bar/Example+"), aliases), isEmpty());
		assertThat(URF.Handle.findFromTag(URI.create("https://example.com/fake/foo/bar/Example#foo"), aliases), isEmpty());
		assertThat(URF.Handle.findFromTag(URI.create("https://example.com/fake/foo/bar/Example#123"), aliases), isEmpty());
		assertThat(URF.Handle.findFromTag(URI.create("https://example.com/fake/foo/bar/Example#foo:bar"), aliases), isEmpty());
	}

	/** @see URF.Handle#toTag(String) */
	@Test
	public void testHandleToTag() {
		final Map<String, URI> namespaces = new HashMap<>();
		namespaces.put("fake", URI.create("https://example.com/fake/"));
		assertThat(URF.Handle.toTag("Example", namespaces), is(URI.create("https://urf.name/Example")));
		assertThat(URF.Handle.toTag("Example+", namespaces), is(URI.create("https://urf.name/Example+")));
		assertThat(URF.Handle.toTag("Example#foo", namespaces), is(URI.create("https://urf.name/Example#foo")));
		assertThat(URF.Handle.toTag("Example#123", namespaces), is(URI.create("https://urf.name/Example#123")));
		assertThat(URF.Handle.toTag("test", namespaces), is(URI.create("https://urf.name/test")));
		assertThat(URF.Handle.toTag("test+", namespaces), is(URI.create("https://urf.name/test+")));
		assertThat(URF.Handle.toTag("false", namespaces), is(URI.create("https://urf.name/false")));
		assertThat(URF.Handle.toTag("true", namespaces), is(URI.create("https://urf.name/true")));
		assertThat(URF.Handle.toTag("foo-bar", namespaces), is(URI.create("https://urf.name/foo/bar")));
		assertThat(URF.Handle.toTag("foo-bar+", namespaces), is(URI.create("https://urf.name/foo/bar+")));
		assertThat(URF.Handle.toTag("foo-bar-Example", namespaces), is(URI.create("https://urf.name/foo/bar/Example")));
		assertThat(URF.Handle.toTag("foo-bar-Example+", namespaces), is(URI.create("https://urf.name/foo/bar/Example+")));
		assertThat(URF.Handle.toTag("foo-bar-Example#foo", namespaces), is(URI.create("https://urf.name/foo/bar/Example#foo")));
		assertThat(URF.Handle.toTag("foo-bar-Example#123", namespaces), is(URI.create("https://urf.name/foo/bar/Example#123")));
		assertThat(URF.Handle.toTag("fake/Example", namespaces), is(URI.create("https://example.com/fake/Example")));
		assertThat(URF.Handle.toTag("fake/Example+", namespaces), is(URI.create("https://example.com/fake/Example+")));
		assertThat(URF.Handle.toTag("fake/Example#foo", namespaces), is(URI.create("https://example.com/fake/Example#foo")));
		assertThat(URF.Handle.toTag("fake/Example#123", namespaces), is(URI.create("https://example.com/fake/Example#123")));
		assertThat(URF.Handle.toTag("fake/test", namespaces), is(URI.create("https://example.com/fake/test")));
		assertThat(URF.Handle.toTag("fake/test+", namespaces), is(URI.create("https://example.com/fake/test+")));
		assertThat(URF.Handle.toTag("fake/Example", namespaces), is(URI.create("https://example.com/fake/Example")));
		assertThat(URF.Handle.toTag("fake/Example+", namespaces), is(URI.create("https://example.com/fake/Example+")));
	}

	/** @see URF.Handle#toTag(String) */
	@Test(expected = IllegalArgumentException.class)
	public void testHandleToTagInvalidHandleID() {
		URF.Handle.toTag("Example#foo:bar");
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

	/**
	 * Ensures that the tag generation method from a handle is caching {@link URI}s when appropriate.
	 * @see URF.Handle#toTag(String)
	 */
	@Test
	public void testHandleToTagCaching() {
		final Map<String, URI> namespaces = new HashMap<>();
		namespaces.put("fake", URI.create("https://example.com/fake/"));

		final URI fooTag = URF.Handle.toTag("foo", namespaces);
		assertThat(URF.Handle.toTag("foo", namespaces), is(sameInstance(fooTag)));
		assertThat(URF.Handle.toTag("foo", namespaces), is(sameInstance(fooTag)));

		final URI barTag = URF.Handle.toTag("bar");
		assertThat(barTag, is(not(fooTag)));
		assertThat(URF.Handle.toTag("bar", namespaces), is(sameInstance(barTag)));
		assertThat(URF.Handle.toTag("foo", namespaces), is(sameInstance(fooTag)));

		final URI fooBarExampleTag = URF.Handle.toTag("foo-bar-Example");
		assertThat(fooBarExampleTag, is(not(fooTag)));
		assertThat(fooBarExampleTag, is(not(barTag)));
		assertThat(URF.Handle.toTag("foo-bar-Example", namespaces), is(sameInstance(fooBarExampleTag)));
		assertThat(URF.Handle.toTag("bar", namespaces), is(sameInstance(barTag)));
		assertThat(URF.Handle.toTag("foo", namespaces), is(sameInstance(fooTag)));
		assertThat(URF.Handle.toTag("foo-bar-Example", namespaces), is(sameInstance(fooBarExampleTag)));

		//custom namespaces aren't cached
		final URI fakeTestTag = URF.Handle.toTag("fake/test", namespaces);
		assertThat(fakeTestTag, is(not(fooTag)));
		assertThat(fakeTestTag, is(not(barTag)));
		assertThat(fakeTestTag, is(not(fooBarExampleTag)));
		final URI fakeTestTag2 = URF.Handle.toTag("fake/test", namespaces);
		assertThat(fakeTestTag2, is(equalTo(fakeTestTag)));
		assertThat(fakeTestTag2, is(not(sameInstance(fakeTestTag))));

		//ID tags aren't cached
		final URI exampleFooIdTag = URF.Handle.toTag("Example#foo", namespaces);
		assertThat(exampleFooIdTag, is(not(fooTag)));
		assertThat(exampleFooIdTag, is(not(barTag)));
		assertThat(exampleFooIdTag, is(not(fooBarExampleTag)));
		assertThat(exampleFooIdTag, is(not(fakeTestTag)));
		final URI exampleFooIdTag2 = URF.Handle.toTag("Example#foo", namespaces);
		assertThat(exampleFooIdTag2, is(equalTo(exampleFooIdTag)));
		assertThat(exampleFooIdTag2, is(not(sameInstance(exampleFooIdTag))));
	}

}
