/*
 * Copyright © 2017 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.urf.surf;

import static java.util.Objects.*;

import java.util.*;

import javax.annotation.*;

/**
 * Utility methods for working with SURF resources.
 * @author Garret Wilson
 */
public class SurfResources {

	/**
	 * Determines whether the given resource is a compound resource, that is, one that can contain other resources. This method returns <code>true</code> for
	 * {@link SurfObject}, any {@link Collection}, and any {@link Map}.
	 * @param resource The resource which may or may not be a compound resource.
	 * @return <code>true</code> if the given resource may hold other resources.
	 */
	public static boolean isCompoundResource(@Nonnull final Object resource) {
		requireNonNull(resource);
		return resource instanceof SurfObject || resource instanceof Collection || resource instanceof Map;
	}
}
