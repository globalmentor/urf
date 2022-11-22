/*
 * Copyright © 2019 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package io.urf.model;

import static java.util.Collections.*;

import java.net.URI;
import java.util.*;

/**
 * Immutable empty resource description.
 * @author Garret Wilson
 */
final class EmptyUrfResourceDescription implements UrfResourceDescription {

	@Override
	public int getPropertyCount() {
		return 0;
	}

	@Override
	public int getPropertyValueCount() {
		return 0;
	}

	@Override
	public Set<Object> getPropertyValues(final URI propertyTag) {
		return emptySet();
	}

	@Override
	public Optional<Object> findPropertyValue(final URI propertyTag) {
		return Optional.empty();
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation throws an {@link UnsupportedOperationException}.
	 * @throws UnsupportedOperationException because this implementation is read-only.
	 */
	@Override
	public Optional<Object> setPropertyValue(final URI propertyTag, final Object propertyValue) {
		throw new UnsupportedOperationException("This empty description is immutable.");
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation throws an {@link UnsupportedOperationException}.
	 * @throws UnsupportedOperationException because this implementation is read-only.
	 */
	@Override
	public boolean addPropertyValue(final URI propertyTag, final Object propertyValue) {
		throw new UnsupportedOperationException("This empty description is immutable.");
	}

	@Override
	public Iterable<Map.Entry<URI, Object>> getProperties() {
		return emptySet();
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation throws an {@link UnsupportedOperationException}.
	 * @throws UnsupportedOperationException because this implementation is read-only.
	 */
	@Override
	public boolean removeProperty(final URI propertyTag) {
		throw new UnsupportedOperationException("This empty description is immutable.");
	}

}
