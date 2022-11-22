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

import static com.globalmentor.collections.iterables.Iterables.*;
import static java.util.Collections.*;
import static java.util.Objects.*;

import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;

/**
 * An internal "join resource" for representing the values of a one-to-many relationship; the value of an n-ary property.
 * @author Garret Wilson
 * @see UrfObject
 */
final class Many implements Iterable<Object> {

	/** Shared immutable empty many. */
	public static final Many NONE = new Many(emptySet());

	private final Set<Object> set;

	/**
	 * Empty constructor. Creates a new, initially empty many.
	 */
	public Many() {
		this(new HashSet<>());
	}

	/**
	 * Creates a many backed by the given set.
	 * @param set The set backing the contents.
	 */
	private Many(@Nonnull final Set<Object> set) {
		this.set = requireNonNull(set);
	}

	/**
	 * Creates a many initially containing one object.
	 * @param object The object to contain in the many.
	 * @return A new many, initially containing a single object.
	 * @throws NullPointerException if the given object is <code>null</code>.
	 */
	public static Many of(@Nonnull final Object object) {
		final Set<Object> set = new HashSet<>();
		set.add(requireNonNull(object));
		return new Many(set);
	}

	/**
	 * Returns the number of elements in this many (its cardinality). If this set contains more than {@code Integer.MAX_VALUE} elements, returns
	 * {@code Integer.MAX_VALUE}.
	 * @return The number of elements in this many.
	 */
	public int count() {
		return set.size();
	}

	/** @return {@code true} if this many is empty, containing no objects. */
	public boolean isEmpty() {
		return set.isEmpty();
	}

	/**
	 * Adds the specified object to this many if it is not already present.
	 * @param object The object to be added to this many.
	 * @return <code>true</code> if the many did not already contain the specified object.
	 * @throws NullPointerException if the given object is <code>null</code>.
	 */
	public boolean add(@Nonnull final Object object) {
		return set.add(requireNonNull(object));
	}

	@Override
	public Iterator<Object> iterator() {
		return set.iterator();
	}

	/** @return A stream to the many contents. */
	public Stream<Object> stream() {
		return set.stream();
	}

	/** Returns any one of the many contents, which may not be present if empty. */
	public Optional<Object> findAny() {
		return findFirst(set);
	}

	/**
	 * Returns a set view of the many. Updating the set will update the many.
	 * @return A set view of the many.
	 */
	public Set<Object> asSet() {
		return set;
	}

}
