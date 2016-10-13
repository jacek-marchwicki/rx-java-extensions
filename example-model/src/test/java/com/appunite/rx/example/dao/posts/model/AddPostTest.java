/*
 * Copyright 2016 Jacek Marchwicki <jacek.marchwicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.appunite.rx.example.dao.posts.model;

import org.junit.Test;

import static com.google.common.truth.Truth.assert_;

public class AddPostTest {

    @Test
    public void testTwoEqualsPosts_areEquals() throws Exception {
        assert_().that(new AddPost("pies", "body"))
                .isEqualTo(new AddPost("pies", "body"));
    }

    @Test
    public void testTwoEqualsPosts_hasSameHashCode() throws Exception {
        assert_().that(new AddPost("pies", "body").hashCode())
                .isEqualTo(new AddPost("pies", "body").hashCode());
    }

}