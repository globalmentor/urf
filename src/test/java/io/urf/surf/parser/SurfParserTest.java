/*
 * Copyright © 2016 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.urf.surf.parser;

import static org.junit.Assert.*;

import java.io.*;

import static io.urf.surf.SurfTestResources.*;
import static org.hamcrest.Matchers.*;
import org.junit.*;

import io.urf.surf.SurfTestResources;

/**
 * Tests of {@link SurfParser}.
 * @author Garret Wilson
 */
public class SurfParserTest {

	/** @see SurfTestResources#OK_SIMPLE_RESOURCE_NAMES */
	@Test
	public void testOkSimpleResources() throws IOException {
		for(final String okSimpleResourceName : OK_SIMPLE_RESOURCE_NAMES) {
			try (final InputStream inputStream = SurfTestResources.class.getResourceAsStream(okSimpleResourceName)) {
				final SurfDocument document = new SurfParser().parse(inputStream);
				//TODO assert that the document has no metadata
				assertThat(document.getDocumentObject().isPresent(), is(true));
				assertThat(document.getDocumentObject().get(), is(instanceOf(SurfResource.class)));
				final SurfResource resource = (SurfResource)document.getDocumentObject().get();
				assertThat(resource.getTypeName().isPresent(), is(false));
				//TODO assert that the object has no properties
			}
		}
	}
}
