/*
 * Copyright © 2018 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.urf.config.file.format.turf;

import java.io.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import io.confound.config.Configuration;
import io.confound.config.MissingConfigurationKeyException;
import io.urf.config.file.format.turf.TurfConfigurationFileFormat;

/**
 * Tests of {@link TurfConfigurationFileFormat}.
 * 
 * @author Garret Wilson
 */
public class TurfConfigurationFileFormatTest {

	/** The name of the test configuration file in the Java test resources. */
	public static final String CONFIG_RESOURCE_NAME = "config.turf";

	/**
	 * Tests whether {@link TurfConfigurationFileFormat} is loading correctly a value.
	 * 
	 * @see TurfConfigurationFileFormat#load(InputStream)
	 * @throws IOException if there was an error preparing or loading the configuration.
	 */
	@Test
	public void testLoad() throws IOException {
		final TurfConfigurationFileFormat format = new TurfConfigurationFileFormat();
		final Configuration configuration;
		try (final InputStream inputStream = new BufferedInputStream(getClass().getResourceAsStream(CONFIG_RESOURCE_NAME))) {
			configuration = format.load(inputStream);
		}
		assertThat(configuration.getString("foo"), is("bar"));
		assertThat(configuration.getLong("test"), is(123L)); //TODO add test for retrieving int after converters are added in CONFOUND-15
		assertThat(configuration.getBoolean("flag"), is(true));
	}

	/**
	 * Tests whether {@link TurfConfigurationFileFormat} is failing when retrieving a value with a non-existent configuration key in the file.
	 * 
	 * @see TurfConfigurationFileFormat#load(InputStream)
	 * @throws IOException if there was an error preparing or loading the configuration.
	 */
	public void testLoadNonExistingConfigurationKey() throws IOException {
		final TurfConfigurationFileFormat format = new TurfConfigurationFileFormat();
		final Configuration configuration;
		try (final InputStream inputStream = new BufferedInputStream(getClass().getResourceAsStream(CONFIG_RESOURCE_NAME))) {
			configuration = format.load(inputStream);
		}
		assertThrows(MissingConfigurationKeyException.class, () -> configuration.getString("foobar"));
	}

}
