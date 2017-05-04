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

package io.urf.surf.serializer;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import static io.urf.surf.test.SurfTestResources.*;
import static org.hamcrest.Matchers.*;

import org.junit.*;

import io.urf.surf.parser.SurfParser;
import io.urf.surf.test.SurfTestResources;

/**
 * Tests of {@link SurfParser}.
 * <p>
 * Rather than testing exact serializations (which would make the tests tedious and brittle), most of the tests here re-parse the serializations and compare
 * them to parsed test serializations provided in {@link SurfTestResources}.
 * </p>
 * @author Garret Wilson
 */
public class SurfSerializerTest {

	/**
	 * Loads and parses a Java resource using {@link SurfParser}.
	 * @param testResourceName The name of the Java resource for testing, relative to {@link SurfParserTest}.
	 * @return The optional resource instance parsed from the named Java instance.
	 * @see SurfTestResources
	 * @see SurfParser#parse(InputStream)
	 */
	protected static Optional<Object> parseTestResource(@Nonnull final String testResourceName) throws IOException {
		try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(testResourceName)) {
			return new SurfParser().parse(inputStream);
		}
	}

	//#literals

	//##string

	/**
	 * @see SurfTestResources#OK_STRING_FOOBAR_RESOURCE_NAME
	 * @see SurfParserTest#testOkStringFoobar()
	 */
	@Test
	public void testSerializeStringFoobar() throws IOException {
		final String serialization = new SurfSerializer().serialize("foobar");
		assertThat(new SurfParser().parse(serialization), equalTo(parseTestResource(OK_STRING_FOOBAR_RESOURCE_NAME)));
	}

	//TODO implement OK_STRINGS_RESOURCE_NAME

}
