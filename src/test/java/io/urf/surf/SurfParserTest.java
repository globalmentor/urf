/*
 * Copyright © 2016-2017 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.urf.surf;

import java.io.*;
import java.util.*;

import javax.annotation.*;

/**
 * Tests of {@link SurfParser}.
 * @author Garret Wilson
 */
public class SurfParserTest extends AbstractSimpleGraphSurfParserTest<SurfObject> {

	/**
	 * {@inheritDoc}
	 * @see SurfParser#parse(InputStream)
	 */
	@Override
	protected Optional<Object> parseTestResource(@Nonnull final InputStream inputStream) throws IOException {
		return new SurfParser().parse(inputStream);
	}

	@Override
	protected Class<SurfObject> getSurfObjectClass() {
		return SurfObject.class;
	}

	@Override
	public Optional<String> getTypeHandle(final SurfObject surfObject) {
		return surfObject.getTypeHandle();
	}

	@Override
	protected int getPropertyCount(final SurfObject surfObject) {
		return surfObject.getPropertyCount();
	}

	@Override
	public Optional<Object> getPropertyValue(final SurfObject surfObject, final String propertyHandle) {
		return surfObject.getPropertyValue(propertyHandle);
	}

}
