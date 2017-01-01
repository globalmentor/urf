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

import static io.urf.SURF.*;

import java.util.Optional;

import javax.annotation.*;

/**
 * Implementation of an URF resource for the simple descriptions that appear in a SURF document.
 * @author Garret Wilson
 */
public class SurfResource implements SimpleUrfResource {

	/** The name of the resource type, or <code>null</code> if not known. */
	private final String typeName;

	@Override
	public Optional<String> getTypeName() {
		return Optional.ofNullable(typeName);
	}

	/** Constructor of a resource with an unknown type. */
	public SurfResource() {
		this(null);
	}

	/**
	 * Optional type name constructor.
	 * @param typeName The name of the resource type, or <code>null</code> if not known.
	 * @throws IllegalArgumentException if the given type name is not a valid SURF name.
	 */
	public SurfResource(@Nullable final String typeName) {
		this.typeName = typeName != null ? checkArgumentValidSurfName(typeName) : null;
	}

}
