/*
 * Copyright © 2018 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

import static io.urf.URF.*;

import java.util.Base64;

import javax.annotation.*;

/**
 * A lexical ID type resource containing binary data.
 * <p>
 * <strong>Warning:</strong> The binary value contained in this class is mutable, and therefore may be changed by any code that has access to it. It is
 * recommended to use instances of this class only temporarily in order to perform some task.
 * </p>
 * @author Garret Wilson
 */
public class UrfBinaryResource extends AbstractValueUrfResource<byte[]> {

	/**
	 * Constructor.
	 * @param value The value object encapsulated by this resource.
	 */
	public UrfBinaryResource(@Nonnull final byte[] value) {
		super(BINARY_TYPE_TAG, value);
	}

	/** {@inheritDoc} This implementation delegates to {@link #toLexicalId(byte[])}. */
	@Override
	public String getLexicalId() {
		return toLexicalId(getValue());
	}

	/**
	 * Determines the URF lexical ID of an URF binary value.
	 * @param value The binary value.
	 * @return The lexical ID of the binary value.
	 */
	public static String toLexicalId(@Nonnull final byte[] value) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
	}

}
