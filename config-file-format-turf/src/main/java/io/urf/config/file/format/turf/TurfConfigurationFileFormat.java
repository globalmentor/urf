/*
 * Copyright © 2018 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.urf.config.file.format.turf;

import static java.util.Collections.*;

import java.io.*;
import java.util.*;

import io.confound.config.*;
import io.confound.config.file.ConfigurationFileFormat;
import io.urf.config.urf.UrfConfiguration;
import io.urf.model.*;
import io.urf.turf.*;

/**
 * File format implementation for a configuration stored in the Text URF (TURF) format.
 * <p>
 * This implementation recognizes files with the extension suffix {@value TURF#FILENAME_EXTENSION}.
 * </p>
 * @author Garret Wilson
 */
public class TurfConfigurationFileFormat implements ConfigurationFileFormat {

	@Override
	public Set<String> getFilenameExtensions() {
		return singleton(TURF.FILENAME_EXTENSION);
	}

	@Override
	public Configuration load(final InputStream inputStream) throws IOException {
		final TurfParser<List<Object>> turfParser = new TurfParser<>(new SimpleGraphUrfProcessor()); //create a parser to parse TURF into a graph
		return turfParser.parseDocument(inputStream).stream().findAny() //parse the TURF document TODO decide about TURF BOM support TODO use something like MoreCollectors.onlyElement() 
				//create an URF configuration backed by the object graph
				.<Configuration>map(root -> new UrfConfiguration(root))
				//if the TURF document was empty
				.orElse(Configuration.empty());
	}

}
