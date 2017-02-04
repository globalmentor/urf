/*
 * Copyright © 2016 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.urf.surf.test;

import static com.globalmentor.collections.Sets.*;

import java.util.Set;

/**
 * Defines available SURF test resources.
 * @author Garret Wilson
 */
public class SurfTestResources {

	public static final String OK_SIMPLE_SURF_RESOURCE_NAME = "ok-simple.surf";
	public static final String OK_SIMPLE_COMMENT_SURF_RESOURCE_NAME = "ok-simple-comment.surf";
	public static final String OK_SIMPLE_COMMENT_WHITESPACE_SURF_RESOURCE_NAME = "ok-simple-comment-whitespace.surf";
	public static final String OK_SIMPLE_MULTILINE_SURF_RESOURCE_NAME = "ok-simple-multiline.surf";
	public static final String OK_SIMPLE_NO_EOL_SURF_RESOURCE_NAME = "ok-simple-no-eol.surf";

	/** Names of SURF document resources semantically equivalent to {@value #OK_SIMPLE_SURF_RESOURCE_NAME}. */
	public static Set<String> OK_SIMPLE_RESOURCE_NAMES = immutableSetOf(OK_SIMPLE_SURF_RESOURCE_NAME, OK_SIMPLE_NO_EOL_SURF_RESOURCE_NAME,
			OK_SIMPLE_MULTILINE_SURF_RESOURCE_NAME, OK_SIMPLE_COMMENT_SURF_RESOURCE_NAME, OK_SIMPLE_COMMENT_WHITESPACE_SURF_RESOURCE_NAME);

	public static final String OK_SINGLE_BOOLEAN_FALSE = "ok-single-boolean-false.surf";
	public static final String OK_SINGLE_BOOLEAN_TRUE = "ok-single-boolean-true.surf";
}
