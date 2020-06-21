/*
 * Copyright © 2019 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.urf.csv;

import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static io.urf.csv.UrfCsvTestResources.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.*;
import java.net.URI;
import java.time.Instant;
import java.util.*;

import javax.annotation.*;

import org.junit.jupiter.api.*;

import com.globalmentor.io.Filenames;

import io.urf.URF.Handle;
import io.urf.model.*;
import io.urf.turf.TurfSerializer;

/**
 * Test of parsing URF CSV documents with {@link UrfCsvParser} into a simple graph using {@link SimpleGraphUrfProcessor}.
 * @author Garret Wilson
 */
public class UrfCsvParserTest {

	/**
	 * Loads and parses the indicated URF CSV document resource. The base filename of the test resource will be used as its type handle.
	 * @implSpec The default implementation defaults to {@link #parse(InputStream, URI)}.
	 * @param testResourceName The name of the URF CSV document resource for testing, relative to {@link UrfCsvTestResources}.
	 * @return The list of URF CSV document roots parsed.
	 */
	protected List<Object> parseTestResource(@Nonnull final String testResourceName) throws IOException {
		final URI subjectTypeHandle = Handle.toTag(Filenames.getBase(testResourceName));
		try (final InputStream inputStream = UrfCsvTestResources.class.getResourceAsStream(testResourceName)) {
			return parse(inputStream, subjectTypeHandle);
		}
	}

	/**
	 * Loads and parses the indicated URF CSV document resource.
	 * @param inputStream The input stream containing the URF CSV document for testing.
	 * @param subjectTypeTag The tag indicating the type of the subject.
	 * @return The list of URF CSV document roots parsed.
	 */
	protected List<Object> parse(@Nonnull final InputStream inputStream, @Nonnull final URI subjectTypeTag) throws IOException {
		final SimpleGraphUrfProcessor urfProcessor = new SimpleGraphUrfProcessor();
		final List<Object> result = new UrfCsvParser<List<Object>>(urfProcessor).parseDocument(inputStream, subjectTypeTag);

		/*TODO delete; testing
		final TurfSerializer turfSerializer = new TurfSerializer(); //
		turfSerializer.setFormatted(true);
		turfSerializer.registerNamespace(URI.create("http://xmlns.com/foaf/0.1/"), "foaf");
		System.out.println(turfSerializer.serializeDocument(new StringBuilder(), urfProcessor));
		*/

		return result;
	}

	/** @see UrfCsvTestResources#USER_RESOURCE_NAME */
	@Test
	public void testUsers() throws IOException {
		final List<Object> resources = parseTestResource(USER_RESOURCE_NAME);
		assertThat(resources.size(), is(2));

		final UrfObject jdoe = (UrfObject)resources.get(0);
		assertThat(jdoe.getTag(), isPresentAndIs(URI.create("https://urf.name/User#jdoe")));
		assertThat(jdoe.findPropertyValueByHandle("username"), isPresentAndIs("jdoe"));
		assertThat(jdoe.findPropertyValueByHandle("name"), isPresentAndIs("Jane Doe"));
		assertThat(jdoe.findPropertyValueByHandle("authenticated"), isPresentAndIs(true));
		assertThat(jdoe.findPropertyValue(URI.create("http://xmlns.com/foaf/0.1/homepage")), isPresentAndIs(URI.create("https://janedoe.example.com/")));
		assertThat(jdoe.findPropertyValueByHandle("loginCount"), isPresentAndIs(3L));
		assertThat(jdoe.findPropertyValueByHandle("lastContact"), isPresentAndIs(Instant.parse("2016-08-26T21:54:22.892Z")));
		assertThat(jdoe.getPropertyValuesByHandle("many+"), containsInAnyOrder("test"));
		assertThat(jdoe.findPropertyValueByHandle("manager"), not(isPresent()));

		final UrfObject jsmith = (UrfObject)resources.get(1);
		assertThat(jsmith.getTag(), isPresentAndIs(URI.create("https://urf.name/User#jsmith")));
		assertThat(jsmith.findPropertyValueByHandle("username"), isPresentAndIs("jsmith"));
		assertThat(jsmith.findPropertyValueByHandle("name"), isPresentAndIs("John Smith"));
		assertThat(jsmith.findPropertyValueByHandle("authenticated"), isPresentAndIs(false));
		assertThat(jsmith.findPropertyValue(URI.create("http://xmlns.com/foaf/0.1/homepage")), isPresentAndIs(URI.create("https://johnsmith.example.com/")));
		assertThat(jsmith.findPropertyValueByHandle("loginCount"), isPresentAndIs(12L));
		assertThat(jsmith.findPropertyValueByHandle("lastContact"), not(isPresent()));
		assertThat(jsmith.getPropertyValuesByHandle("many+"), empty());
		assertThat(jsmith.findPropertyValueByHandle("manager"), isPresentAnd(sameInstance(jdoe)));
	}

}
