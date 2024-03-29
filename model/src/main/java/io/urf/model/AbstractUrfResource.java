/*
 * Copyright © 2017 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

import java.net.URI;

/**
 * An abstract base implementation of an URF resource.
 * @author Garret Wilson
 */
public abstract class AbstractUrfResource implements UrfResource {

	@Override
	public int hashCode() {
		final URI tag = getTag().orElse(null);
		//if this object has no tag, return the default hash code to provide wider distribution of hash values
		return tag != null ? tag.hashCode() : super.hashCode();
	}

	@Override
	public final boolean equals(final Object object) {
		if(this == object) {
			return true;
		}
		if(!(object instanceof UrfResource)) {
			return false;
		}
		final URI tag = getTag().orElse(null);
		return tag != null && tag.equals(((UrfResource)object).getTag().orElse(null));
	}

	@Override
	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("|<");
		getTag().ifPresent(stringBuilder::append);
		stringBuilder.append(">|");
		return stringBuilder.toString();
	}

}
