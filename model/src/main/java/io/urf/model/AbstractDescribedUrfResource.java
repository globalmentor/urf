/*
 * Copyright © 2017-2018 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

import static com.globalmentor.java.Conditions.*;
import static java.lang.Math.*;
import static java.util.Collections.*;
import static java.util.Objects.*;

import java.net.URI;
import java.util.*;

import javax.annotation.*;

import io.urf.URF.Tag;

/**
 * An implementation of an URF resource that provides access to its own description via a graph of properties.
 * @implSpec This implementation uses an instance of {@link Many} as the value of n-ary property. The implementation must not allow any property to have an
 *           empty {@link Many} instance as a value.
 * @author Garret Wilson
 */
public abstract class AbstractDescribedUrfResource extends BaseUrfResource implements UrfResourceDescription {

	private final Map<URI, Object> propertyValuesByTag = new HashMap<>();

	@Override
	public int getPropertyCount() {
		return propertyValuesByTag.size();
	}

	@Override
	public boolean hasProperties() {
		return !propertyValuesByTag.isEmpty();
	}

	@Override
	public int getPropertyValueCount() {
		//TODO create more efficient implementation
		int propertyValueCount = propertyValuesByTag.size(); //start with the minimums count we could have
		for(final Object value : propertyValuesByTag.values()) {
			//TODO check for Integer.MAX_VALUE
			if(value instanceof Many) { //account for any n-ary values
				final Many many = (Many)value;
				assert !many.isEmpty() : "The implementation should not allow an empty many as a value.";
				propertyValueCount += many.count() - 1; //we already accounted for one of these 
			}
		}
		return propertyValueCount;
	}

	@Override
	public Set<Object> getPropertyValues(final URI propertyTag) {
		final Object value = propertyValuesByTag.get(Tag.checkArgumentValid(propertyTag));
		if(value instanceof Many) { //support n-ary properties
			assert Tag.isNary(propertyTag) : "This implementation should not allow a Many instance as the value of a property that is not n-ary.";
			final Many many = (Many)value;
			assert !many.isEmpty() : "The implementation should not allow an empty many as a value.";
			return unmodifiableSet(new HashSet<>(many.asSet())); //TODO switch to Java 9 Set.copyOf()
		}
		return value != null ? singleton(value) : emptySet(); //TODO switch to Java 9 Set.of() 
	}

	@Override
	public Optional<Object> findPropertyValue(final URI propertyTag) {
		final Object value = propertyValuesByTag.get(Tag.checkArgumentValid(propertyTag));
		if(value instanceof Many) { //support n-ary properties
			assert Tag.isNary(propertyTag) : "This implementation should not allow a Many instance as the value of a property that is not n-ary.";
			final Many many = (Many)value;
			assert !many.isEmpty() : "The implementation should not allow an empty many as a value.";
			return many.findAny();
		}
		return Optional.ofNullable(value);
	}

	@Override
	public Optional<Object> setPropertyValue(final URI propertyTag, final Object propertyValue) {
		final Object newValue = Tag.isNary(propertyTag) ? Many.of(propertyValue) : requireNonNull(propertyValue);
		final Object oldValue = propertyValuesByTag.put(propertyTag, newValue);
		if(oldValue instanceof Many) {
			assert Tag.isNary(propertyTag) : "This implementation should not allow a Many instance as the value of a property that is not n-ary.";
			final Many many = (Many)oldValue;
			assert !many.isEmpty() : "The implementation should not allow an empty many as a value.";
			return many.findAny();
		}
		return Optional.ofNullable(oldValue);
	}

	@Override
	public boolean addPropertyValue(final URI propertyTag, final Object propertyValue) {
		requireNonNull(propertyValue);
		if(Tag.isNary(propertyTag)) {
			//Technically it would be slightly safer if we could add the object to the many _before_ putting the many in the map,
			//but the current approach is more efficient and the logic is easier to understand. 
			final Many manyValue = (Many)propertyValuesByTag.computeIfAbsent(propertyTag, prop -> new Many());
			return manyValue.add(propertyValue);
		} else {
			final Object oldValue = propertyValuesByTag.putIfAbsent(propertyTag, propertyValue);
			checkState(oldValue != null, "Cannot add multiple values with non n-ary property %s.", propertyTag);
			return true; //if this succeeded, and there was no previous value, then we had to have added something
		}
	}

	@Override
	public Iterable<Map.Entry<URI, Object>> getProperties() {
		final int propertyCount = propertyValuesByTag.size();
		final int initialPropertyValueCapacity = max(min(propertyCount * 5, 100), propertyCount); //guess the capacity at 5 times the properties, unless over 100, but at least PropertyCount
		final List<Map.Entry<URI, Object>> properties = new ArrayList<>(initialPropertyValueCapacity);
		for(final Map.Entry<URI, Object> propertyValueByTag : propertyValuesByTag.entrySet()) {
			final URI propertyTag = propertyValueByTag.getKey();
			final Object value = propertyValueByTag.getValue();
			if(value instanceof Many) { //support n-ary property values
				assert Tag.isNary(propertyTag) : "This implementation should not allow a Many instance as the value of a property that is not n-ary.";
				final Many many = (Many)value;
				for(final Object propertyValue : many) { //recursively add the many values
					properties.add(new AbstractMap.SimpleImmutableEntry<>(propertyTag, propertyValue));
				}
			} else { //for one-to-one properties
				properties.add(new AbstractMap.SimpleImmutableEntry<>(propertyValueByTag)); //just a add a copy of this entry
			}
		}
		return properties; //TODO improve implementation to use dynamic iterator by chaining sub-iterators
	}

	/**
	 * Optional tag and optional type name constructor.
	 * @param tag The identifying resource tag, or <code>null</code> if not known.
	 * @param typeTag The tag of the resource type, or <code>null</code> if not known.
	 * @throws IllegalArgumentException if a tag is given that is not an absolute IRI.
	 */
	public AbstractDescribedUrfResource(@Nullable final URI tag, @Nullable final URI typeTag) {
		super(tag, typeTag);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This version includes the resource property tags and their associated values if there are any properties.
	 */
	@Override
	public String toString() {
		final String defaultString = super.toString();
		return !propertyValuesByTag.isEmpty() ? defaultString + " " + propertyValuesByTag.toString() : defaultString;
	}

}
