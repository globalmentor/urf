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

package io.urf.surf.parser;

import static io.urf.SURF.*;
import static java.util.Objects.*;

import java.net.URI;
import java.util.*;

import javax.annotation.*;

import com.globalmentor.collections.iterables.ConverterIterable;
import com.globalmentor.model.NameValuePair;

/**
 * Implementation of an URF resource for the object descriptions that appear in a SURF document.
 * <p>
 * SURF objects are considered equal if their IRIs, type names, and property names and values are equal.
 * </p>
 * <p>
 * This implementation does not consider another object equal unless it is an implementation of {@link SurfObject}.
 * </p>
 * @author Garret Wilson
 */
public class SurfObject implements SimpleUrfResource {

	/** The identifying resource IRI, or <code>null</code> if not known. */
	private final URI iri;

	@Override
	public Optional<URI> getIri() {
		return Optional.ofNullable(iri);
	}

	/** The name of the resource type, or <code>null</code> if not known. */
	private final String typeName;

	@Override
	public Optional<String> getTypeName() {
		return Optional.ofNullable(typeName);
	}

	private final Map<String, Object> properties = new HashMap<>();

	@Override
	public int getPropertyCount() {
		return properties.size();
	}

	@Override
	public Optional<Object> getPropertyValue(final String propertyName) {
		return Optional.ofNullable(properties.get(requireNonNull(propertyName)));
	}

	@Override
	public Optional<Object> setPropertyValue(final String propertyName, final Object value) {
		return Optional.ofNullable(properties.put(requireNonNull(propertyName), requireNonNull(value)));
	}

	@Override
	public Iterable<NameValuePair<String, Object>> getPropertyNameValuePairs() {
		return new ConverterIterable<>(properties.entrySet(), NameValuePair::fromMapEntry);
	}

	/** Constructor of a resource with no IRI and an unknown type. */
	public SurfObject() {
		this(null, null);
	}

	/**
	 * Optional IRI constructor.
	 * @param iri The identifying resource IRI, or <code>null</code> if not known.
	 */
	public SurfObject(@Nullable final URI iri) {
		this(iri, null);
	}

	/**
	 * Optional type name constructor.
	 * @param typeName The name of the resource type, or <code>null</code> if not known.
	 * @throws IllegalArgumentException if the given type name is not a valid SURF name.
	 */
	public SurfObject(@Nullable final String typeName) {
		this(null, typeName);
	}

	/**
	 * Optional IRI and optional type name constructor.
	 * @param iri The identifying resource IRI, or <code>null</code> if not known.
	 * @param typeName The name of the resource type, or <code>null</code> if not known.
	 * @throws IllegalArgumentException if the given type name is not a valid SURF name.
	 */
	public SurfObject(@Nullable final URI iri, @Nullable final String typeName) {
		this.iri = iri;
		this.typeName = typeName != null ? checkArgumentValidSurfName(typeName) : null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(iri, typeName, properties);
	}

	@Override
	public boolean equals(final Object object) {
		if(this == object) {
			return true;
		}
		if(!(object instanceof SurfObject)) {
			return false;
		}
		final SurfObject surfObject = (SurfObject)object;
		return getIri().equals(surfObject.getIri()) && getTypeName().equals(surfObject.getTypeName()) && properties.equals(surfObject.properties);
	}
}
