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

import java.util.*;
import java.util.regex.Pattern;

import javax.annotation.*;

import io.confound.config.*;
import io.urf.model.UrfObject;

/**
 * Configuration implementation backed by an URF object graph.
 * @author Garret Wilson
 * @see <a href="https://io.urf/">Uniform Resource Framework (URF)</a>
 */
public class UrfConfiguration extends AbstractObjectConfiguration {

	private final Object root;

	/**
	 * URF object graph root constructor.
	 * @param root The root object of the URF object graph.
	 * @throws NullPointerException if the given object is <code>null</code>.
	 */
	public UrfConfiguration(@Nonnull final Object root) {
		this.root = requireNonNull(root);
	}

	//TODO override hasConfigurationKeyImpl() if can be made more efficient

	/** The delimiter for hierarchical keys. */
	private static final char KEY_HIERARCHY_DELIMITER = '.'; //TODO consolidate; provide more Confound infrastructure for hierarchical configurations

	/** The pattern for splitting out the segments of a potentially hierarchical key. */
	private static final Pattern KEY_HIERARCHY_DELIMITER_PATTERN = Pattern.compile(Pattern.quote(String.valueOf(KEY_HIERARCHY_DELIMITER)));

	/**
	 * {@inheritDoc}
	 * @throws IllegalArgumentException if the given key has subsequent delimiters, such as <code>"foo..bar"</code>.
	 */
	@Override
	protected Optional<Object> findConfigurationValueImpl(final String key) throws ConfigurationException {
		Object object = root;
		for(final String keySegment : KEY_HIERARCHY_DELIMITER_PATTERN.split(key, -1)) { //use -1 to subsequent delimiters by not discarding empty strings
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

}
