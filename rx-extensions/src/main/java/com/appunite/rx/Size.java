package com.appunite.rx;


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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Size)) return false;

        Size size = (Size) o;

        return width == size.width && height == size.height;

    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        return result;
    }
}
