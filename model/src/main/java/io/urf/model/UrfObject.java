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

package io.urf.model;

import static java.util.Objects.*;

import java.net.URI;
import java.util.*;

import javax.annotation.*;

/**
 * An implementation of an URF resource that provides access to its own description via a graph of properties.
 * @author Garret Wilson
 */
public class UrfObject extends AbstractUrfResource implements UrfResourceDescription {

	private final Map<URI, Object> propertyValuesByTag = new HashMap<>();

	@Override
	public int getPropertyCount() {
		return propertyValuesByTag.size();
	}

	@Override
	public Optional<Object> getPropertyValue(final URI propertyTag) {
		return Optional.ofNullable(propertyValuesByTag.get(requireNonNull(propertyTag)));
	}

	@Override
	public Optional<Object> setPropertyValue(final URI propertyTag, final Object propertyValue) {
		return Optional.ofNullable(propertyValuesByTag.put(requireNonNull(propertyTag), requireNonNull(propertyValue)));
	}

	//TODO add support for multiple values per property

	/** Constructor of a resource with no tag and an unknown type. */
	public UrfObject() {
		this(null, null);
	}

	/**
	 * Optional tag constructor.
	 * @param tag The identifying resource tag, or <code>null</code> if not known.
	 * @throws IllegalArgumentException if a tag is given that is not an absolute IRI.
	 */
	public UrfObject(@Nullable final URI tag) {
		this(tag, null);
	}

	/**
	 * Optional tag and optional type name constructor.
	 * @param tag The identifying resource tag, or <code>null</code> if not known.
	 * @param typeTag The tag of the resource type, or <code>null</code> if not known.
	 * @throws IllegalArgumentException if a tag is given that is not an absolute IRI.
	 */
	public UrfObject(@Nullable final URI tag, @Nullable final URI typeTag) {
		super(tag, typeTag);
	}
}
