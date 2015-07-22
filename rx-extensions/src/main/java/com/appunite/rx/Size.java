package com.appunite.rx;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class Size {
    private final int width;
    private final int height;

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    @Override
    public String toString() {
        return toStringHelper()
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Size)) return false;
        final Size size = (Size) o;
        return Objects.equal(width, size.width)
                && Objects.equal(height, size.height);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(width, height);
    }

    protected MoreObjects.ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(this)
                .add("width", width)
                .add("height", height);
    }
}
