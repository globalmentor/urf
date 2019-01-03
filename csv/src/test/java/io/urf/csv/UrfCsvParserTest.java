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
		final URI subjectTypeHandle = Handle.toTag(Filenames.getBaseFilename(testResourceName));
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

		final TurfSerializer turfSerializer = new TurfSerializer(); //TODO delete; testing
		turfSerializer.setFormatted(true);
		System.out.println(turfSerializer.serializeDocument(new StringBuilder(), urfProcessor));

		return result;
	}

	/** @see UrfCsvTestResources#USER_RESOURCE_NAME */
	@Test
	public void testUsers() throws IOException {
		final List<Object> resources = parseTestResource(USER_RESOURCE_NAME);
		//TODO test the results
	}

}
