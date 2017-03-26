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

	//simple files
	public static final String OK_SIMPLE_SURF_RESOURCE_NAME = "ok-simple.surf";
	public static final String OK_SIMPLE_COMMENT_SURF_RESOURCE_NAME = "ok-simple-comment.surf";
	public static final String OK_SIMPLE_COMMENT_WHITESPACE_SURF_RESOURCE_NAME = "ok-simple-comment-whitespace.surf";
	public static final String OK_SIMPLE_MULTILINE_SURF_RESOURCE_NAME = "ok-simple-multiline.surf";
	public static final String OK_SIMPLE_NO_EOL_SURF_RESOURCE_NAME = "ok-simple-no-eol.surf";

	/** Names of SURF document resources semantically equivalent to {@value #OK_SIMPLE_SURF_RESOURCE_NAME}. */
	public static Set<String> OK_SIMPLE_RESOURCE_NAMES = immutableSetOf(OK_SIMPLE_SURF_RESOURCE_NAME, OK_SIMPLE_NO_EOL_SURF_RESOURCE_NAME,
			OK_SIMPLE_MULTILINE_SURF_RESOURCE_NAME, OK_SIMPLE_COMMENT_SURF_RESOURCE_NAME, OK_SIMPLE_COMMENT_WHITESPACE_SURF_RESOURCE_NAME);

	//objects
	public static final String OK_OBJECT_NO_PROPERTIES_0_RESOURCE_NAME = "ok-object-no-properties-0.surf";
	public static final String OK_OBJECT_NO_PROPERTIES_1_RESOURCE_NAME = "ok-object-no-properties-1.surf";
	public static final String OK_OBJECT_NO_PROPERTIES_2_RESOURCE_NAME = "ok-object-no-properties-2.surf";

	/** Names of SURF document resources semantically equivalent to {@value #OK_OBJECT_NO_PROPERTIES_0_RESOURCE_NAME}. */
	public static Set<String> OK_OBJECT_NO_PROPERTIES_RESOURCE_NAMES = immutableSetOf(OK_OBJECT_NO_PROPERTIES_0_RESOURCE_NAME,
			OK_OBJECT_NO_PROPERTIES_1_RESOURCE_NAME, OK_OBJECT_NO_PROPERTIES_2_RESOURCE_NAME);

	public static final String OK_OBJECT_ONE_PROPERTY_0_RESOURCE_NAME = "ok-object-one-property-0.surf";
	public static final String OK_OBJECT_ONE_PROPERTY_1_RESOURCE_NAME = "ok-object-one-property-1.surf";
	public static final String OK_OBJECT_ONE_PROPERTY_2_RESOURCE_NAME = "ok-object-one-property-2.surf";

	/** Names of SURF document resources semantically equivalent to {@value #OK_OBJECT_ONE_PROPERTY_0_RESOURCE_NAME}. */
	public static Set<String> OK_OBJECT_ONE_PROPERTY_RESOURCE_NAMES = immutableSetOf(OK_OBJECT_ONE_PROPERTY_0_RESOURCE_NAME,
			OK_OBJECT_ONE_PROPERTY_1_RESOURCE_NAME, OK_OBJECT_ONE_PROPERTY_2_RESOURCE_NAME);

	public static final String OK_OBJECT_TWO_PROPERTIES_0_RESOURCE_NAME = "ok-object-two-properties-0.surf";
	public static final String OK_OBJECT_TWO_PROPERTIES_0_WHITESPACE_RESOURCE_NAME = "ok-object-two-properties-0-whitespace.surf";
	public static final String OK_OBJECT_TWO_PROPERTIES_1_RESOURCE_NAME = "ok-object-two-properties-1.surf";
	public static final String OK_OBJECT_TWO_PROPERTIES_1_NO_COMMAS_RESOURCE_NAME = "ok-object-two-properties-1-no-commas.surf";
	public static final String OK_OBJECT_TWO_PROPERTIES_2_RESOURCE_NAME = "ok-object-two-properties-2.surf";
	public static final String OK_OBJECT_TWO_PROPERTIES_2_NEWLINES_RESOURCE_NAME = "ok-object-two-properties-2-newlines.surf";
	public static final String OK_OBJECT_TWO_PROPERTIES_2_NO_COMMAS_RESOURCE_NAME = "ok-object-two-properties-2-no-commas.surf";

	/** Names of SURF document resources semantically equivalent to {@value #OK_OBJECT_TWO_PROPERTIES_0_RESOURCE_NAME}. */
	public static Set<String> OK_OBJECT_TWO_PROPERTIES_RESOURCE_NAMES = immutableSetOf(OK_OBJECT_TWO_PROPERTIES_0_RESOURCE_NAME,
			OK_OBJECT_TWO_PROPERTIES_0_WHITESPACE_RESOURCE_NAME, OK_OBJECT_TWO_PROPERTIES_1_RESOURCE_NAME, OK_OBJECT_TWO_PROPERTIES_1_NO_COMMAS_RESOURCE_NAME,
			OK_OBJECT_TWO_PROPERTIES_2_RESOURCE_NAME, OK_OBJECT_TWO_PROPERTIES_2_NEWLINES_RESOURCE_NAME, OK_OBJECT_TWO_PROPERTIES_2_NO_COMMAS_RESOURCE_NAME);

	//booleans
	public static final String OK_BOOLEAN_TRUE_RESOURCE_NAME = "ok-boolean-true.surf";
	public static final String OK_BOOLEAN_FALSE_RESOURCE_NAME = "ok-boolean-false.surf";

	//regular expressions
	public static final String OK_REGULAR_EXPRESSIONS_RESOURCE_NAME = "ok-regular-expressions.surf";

	//strings
	public static final String OK_STRING_FOOBAR_RESOURCE_NAME = "ok-string-foobar.surf";
	public static final String OK_STRINGS_RESOURCE_NAME = "ok-strings.surf";

	public static final String OK_BOOLEANS_RESOURCE_NAME = "ok-booleans.surf";
	public static final String OK_NUMBERS_RESOURCE_NAME = "ok-numbers.surf";

	//lists
	public static final String OK_LIST_NO_ITEMS_0_RESOURCE_NAME = "ok-list-no-items-0.surf";
	public static final String OK_LIST_NO_ITEMS_1_RESOURCE_NAME = "ok-list-no-items-1.surf";
	public static final String OK_LIST_NO_ITEMS_2_RESOURCE_NAME = "ok-list-no-items-2.surf";

	/** Names of SURF document resources semantically equivalent to {@value #OK_LIST_NO_ITEMS_0_RESOURCE_NAME}. */
	public static Set<String> OK_LIST_NO_ITEMS_RESOURCE_NAMES = immutableSetOf(OK_LIST_NO_ITEMS_0_RESOURCE_NAME, OK_LIST_NO_ITEMS_1_RESOURCE_NAME,
			OK_LIST_NO_ITEMS_2_RESOURCE_NAME);

	public static final String OK_LIST_ONE_ITEM_0_RESOURCE_NAME = "ok-list-one-item-0.surf";
	public static final String OK_LIST_ONE_ITEM_1_RESOURCE_NAME = "ok-list-one-item-1.surf";
	public static final String OK_LIST_ONE_ITEM_2_RESOURCE_NAME = "ok-list-one-item-2.surf";

	/** Names of SURF document resources semantically equivalent to {@value #OK_LIST_ONE_ITEM_0_RESOURCE_NAME}. */
	public static Set<String> OK_LIST_ONE_ITEM_RESOURCE_NAMES = immutableSetOf(OK_LIST_ONE_ITEM_0_RESOURCE_NAME, OK_LIST_ONE_ITEM_1_RESOURCE_NAME,
			OK_LIST_ONE_ITEM_2_RESOURCE_NAME);

	public static final String OK_LIST_TWO_ITEMS_0_RESOURCE_NAME = "ok-list-two-items-0.surf";
	public static final String OK_LIST_TWO_ITEMS_0_WHITESPACE_RESOURCE_NAME = "ok-list-two-items-0-whitespace.surf";
	public static final String OK_LIST_TWO_ITEMS_1_RESOURCE_NAME = "ok-list-two-items-1.surf";
	public static final String OK_LIST_TWO_ITEMS_1_NO_COMMAS_RESOURCE_NAME = "ok-list-two-items-1-no-commas.surf";
	public static final String OK_LIST_TWO_ITEMS_2_RESOURCE_NAME = "ok-list-two-items-2.surf";
	public static final String OK_LIST_TWO_ITEMS_2_NO_COMMAS_RESOURCE_NAME = "ok-list-two-items-2-no-commas.surf";

	/** Names of SURF document resources semantically equivalent to {@value #OK_LIST_TWO_ITEMS_0_RESOURCE_NAME}. */
	public static Set<String> OK_LIST_TWO_ITEMS_RESOURCE_NAMES = immutableSetOf(OK_LIST_TWO_ITEMS_0_RESOURCE_NAME, OK_LIST_TWO_ITEMS_0_WHITESPACE_RESOURCE_NAME,
			OK_LIST_TWO_ITEMS_1_RESOURCE_NAME, OK_LIST_TWO_ITEMS_1_NO_COMMAS_RESOURCE_NAME, OK_LIST_TWO_ITEMS_2_RESOURCE_NAME,
			OK_LIST_TWO_ITEMS_2_NO_COMMAS_RESOURCE_NAME);
}
