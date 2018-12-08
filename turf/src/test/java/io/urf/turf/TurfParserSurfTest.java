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

package io.urf.turf;

import java.io.*;
import java.util.*;

import io.urf.URF;
import io.urf.model.*;
import io.urf.surf.AbstractSimpleGraphSurfParserTest;

/**
 * Test of parsing SURF documents with {@link TurfParser} into a simple graph using {@link SimpleGraphUrfProcessor}.
 * 
 * @author Garret Wilson
 */
public class TurfParserSurfTest extends AbstractSimpleGraphSurfParserTest<UrfObject> {

	@Override
	protected Optional<Object> parseTestResource(InputStream inputStream) throws IOException {
		return new TurfParser<List<Object>>(new SimpleGraphUrfProcessor()).parseDocument(inputStream).stream().findAny(); //TODO require at most one
	}

	@Override
	protected Class<UrfObject> getSurfObjectClass() {
		return UrfObject.class;
	}

	@Override
	public Optional<String> getTypeHandle(final UrfObject urfObject) {
		return urfObject.getTypeTag().flatMap(URF.Handle::fromTag);
	}

	@Override
	protected int getPropertyCount(final UrfObject urfObject) {
		return urfObject.getPropertyCount();
	}

	@Override
	public Optional<Object> getPropertyValue(final UrfObject urfObject, final String propertyHandle) {
		return urfObject.getPropertyValue(propertyHandle);
	}

}
