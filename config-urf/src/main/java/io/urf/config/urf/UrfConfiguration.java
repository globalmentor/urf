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

package io.urf.config.urf;

import static com.globalmentor.java.Conditions.*;
import static java.util.Objects.*;

import java.nio.file.*;
import java.util.*;

import javax.annotation.*;

import io.confound.config.*;
import io.urf.URF;
import io.urf.model.UrfObject;

/**
 * Configuration implementation backed by an URF object graph.
 * @author Garret Wilson
 * @see <a href="https://urf.io/">Uniform Resource Framework (URF)</a>
 */
public class UrfConfiguration extends AbstractObjectConfiguration implements Section {

	private final Object root;

	/**
	 * URF object graph root constructor.
	 * @param root The root object of the URF object graph.
	 * @throws NullPointerException if the given object is <code>null</code>.
	 */
	public UrfConfiguration(@Nonnull final Object root) {
		this.root = requireNonNull(root);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns the handle of the root object, if the root object is an {@link UrfObject} and has a type that can be represented as a
	 *           handle. If the root object has a type that cannot be represented as a handle, it is returned as the string form of its tag.
	 */
	@Override
	public Optional<String> getSectionType() {
		return root instanceof UrfObject ? ((UrfObject)root).getTypeTag().map(tag -> URF.Handle.fromTag(tag).orElse(tag.toString())) : Optional.empty();
	}

	//TODO override hasConfigurationKeyImpl() if can be made more efficient

	/**
	 * {@inheritDoc}
	 * @throws IllegalArgumentException if the given key has subsequent delimiters, such as <code>"foo..bar"</code>.
	 */
	@Override
	protected Optional<Object> findConfigurationValueImpl(final String key) throws ConfigurationException {
		Object object = root;
		for(final String keySegment : KEY_SEGMENTS_PATTERN.split(key, -1)) { //use -1 to subsequent delimiters by not discarding empty strings
			checkArgument(!keySegment.isEmpty(), "Configuration key %s cannot have an empty hiararchy segment.", key);
			if(object == null) { //if we can't go down further, continue validating the rest of the key before returning the value
				continue;
			}
			if(object instanceof UrfObject) {
				object = ((UrfObject)object).findPropertyValueByHandle(keySegment).orElse(null);
			} else if(object instanceof Map) {
				object = ((Map<?, ?>)object).get(requireNonNull(keySegment));
			} else {
				object = null;
			}
		}
		return Optional.ofNullable(object);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This version adds the ability to convert a {@link String} value from the URF model to a {@link Path} using {@link Paths#get(String, String...)}.
	 */
	@Override
	protected <O> Optional<O> convertValue(final Optional<Object> value, final Class<O> convertClass) throws ConfigurationException {
		if(value.isPresent()) { //TODO convert to Java 9 or()
			final Object object = value.get();
			if(convertClass.equals(Path.class) && object instanceof String) {
				return Optional.of(convertClass.cast(Paths.get((String)object)));
			}
		}
		return super.convertValue(value, convertClass); //if we don't recognize it, perform the default conversion
	}
}
