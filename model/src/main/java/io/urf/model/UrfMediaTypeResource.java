/*
 * Copyright © 2020 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

import static io.urf.URF.*;
import static java.util.Comparator.*;

import java.util.*;

import javax.annotation.*;

import com.globalmentor.net.MediaType;

/**
 * A lexical ID type resource for an Internet media type.
 * @author Garret Wilson
 */
public class UrfMediaTypeResource extends AbstractValueUrfResource<MediaType> {

	/**
	 * A comparator for ordering media type parameters:
	 * <ol>
	 * <li>Primarily by parameter name using the lexicographic comparison {@link String#compareTo(String)}.</li>
	 * <li>Secondarily by decoded parameter value using the lexicographic comparison {@link String#compareTo(String)}.</li>
	 * </ol>
	 * @implNote This comparator relies on the {@link MediaType} implementation, which normalizes parameter names and {@value MediaType#CHARSET_PARAMETER}
	 *           parameter values to ASCII lowercase. All other case-insensitive parameters should have been provided in lowercase.
	 */
	public static final Comparator<MediaType.Parameter> PARAMETER_ORDER = comparing(MediaType.Parameter::getName)
			.thenComparing(MediaType.Parameter::getValue);

	/**
	 * Constructor.
	 * @param value The value object encapsulated by this resource.
	 */
	public UrfMediaTypeResource(@Nonnull final MediaType value) {
		super(MEDIA_TYPE_TYPE_TAG, value);
	}

	/** {@inheritDoc} This implementation delegates to {@link #toLexicalId(MediaType)}. */
	@Override
	public String getLexicalId() {
		return toLexicalId(getValue());
	}

	/**
	 * Determines the URF lexical ID of an URF media type value.
	 * @implSpec This implementation calls {@link MediaType#toString(String, String, Iterable)} using {@link #PARAMETER_ORDER} to sort the parameters.
	 * @param value The regular expression value.
	 * @return The lexical ID of the regular expression.
	 */
	public static String toLexicalId(@Nonnull final MediaType value) {
		final Set<MediaType.Parameter> parameters = value.getParameters();
		final int parameterSize = parameters.size();
		final Iterable<MediaType.Parameter> sortedParameters;
		if(parameterSize > 1) { //only sort they parameters if there are more than one
			final MediaType.Parameter[] parameterArray = parameters.toArray(new MediaType.Parameter[parameterSize]);
			Arrays.sort(parameterArray, PARAMETER_ORDER);
			sortedParameters = Arrays.asList(parameterArray);
		} else { //no need to sort the parameters unless there are more than one
			sortedParameters = parameters;
		}
		return MediaType.toString(value.getPrimaryType(), value.getSubType(), sortedParameters);
	}

}
