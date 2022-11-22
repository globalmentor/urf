/*
 * Copyright © 2020 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;

import com.globalmentor.net.MediaType;

/**
 * Tests of {@link UrfMediaTypeResource}.
 * @author Garret Wilson
 */
public class UrfMediaTypeResourceTest {

	/** @see UrfMediaTypeResource#toLexicalId(MediaType) */
	@Test
	public void testLexicalIdParametersOrdered() {
		assertThat(UrfMediaTypeResource.toLexicalId(MediaType.parse("text/plain")), is("text/plain"));
		assertThat(UrfMediaTypeResource.toLexicalId(MediaType.parse("text/html; charset=UTF-8")), is("text/html;charset=utf-8"));
		assertThat(UrfMediaTypeResource.toLexicalId(MediaType.parse("text/html; charset=UTF-8; test=foo")), is("text/html;charset=utf-8;test=foo"));
		assertThat(UrfMediaTypeResource.toLexicalId(MediaType.parse("text/html; charset=UTF-8; test=foo; apple=berry")),
				is("text/html;apple=berry;charset=utf-8;test=foo"));
		assertThat(UrfMediaTypeResource.toLexicalId(MediaType.parse("text/html; charset=UTF-8; test=foo; apple=berry; test=bar")),
				is("text/html;apple=berry;charset=utf-8;test=bar;test=foo"));
		assertThat(UrfMediaTypeResource.toLexicalId(MediaType.parse("text/html; charset=UTF-8; test=foo; apple=berry; test=bar; foo=bar")),
				is("text/html;apple=berry;charset=utf-8;foo=bar;test=bar;test=foo"));
	}

}
