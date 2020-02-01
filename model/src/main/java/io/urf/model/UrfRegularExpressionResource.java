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

package io.urf.model;

import static io.urf.URF.*;

import java.util.regex.Pattern;

import javax.annotation.*;

/**
 * A lexical ID type resource for a regular expression.
 * @author Garret Wilson
 */
public class UrfRegularExpressionResource extends AbstractValueUrfResource<Pattern> {

	/**
	 * Constructor.
	 * @param value The value object encapsulated by this resource.
	 */
	public UrfRegularExpressionResource(@Nonnull final Pattern value) {
		super(REGULAR_EXPRESSION_TYPE_TAG, value);
	}

	/** {@inheritDoc} This implementation delegates to {@link #getLexicalId(Pattern)}. */
	@Override
	public String getLexicalId() {
		return getLexicalId(getValue());
	}

	/**
	 * Determines the URF lexical ID of an URF regular expression value.
	 * @param value The regular expression value.
	 * @return The lexical ID of the regular expression.
	 */
	public static String getLexicalId(@Nonnull final Pattern value) {
		//TODO add support for flags
		return value.toString();
	}

}
