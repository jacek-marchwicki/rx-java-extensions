package com.appunite.rx.example.model.model;

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