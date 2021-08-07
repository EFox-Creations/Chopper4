package me.vixen.chopperbot.tools;

import java.awt.*;

public class ColorWrapper {
    private int r;
    private int g;
    private int b;

    public ColorWrapper(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Color toAwtColor() {
        return new Color(r,g,b);
    }
}
