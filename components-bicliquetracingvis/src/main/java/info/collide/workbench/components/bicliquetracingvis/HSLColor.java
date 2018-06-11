package info.collide.workbench.components.bicliquetracingvis;

import java.awt.Color;

public class HSLColor {
	private Color color;
	private float[] hsl;

	/**
	 * Create a HSLColor object using an {@link Color} object.
	 *
	 * @param color
	 *            the Color object
	 */
	public HSLColor(Color color) {
		this.color = color;
		hsl = fromRGB(color);
	}

	/**
	 * Creates a HSLColor object with the given values.
	 *
	 * @param h
	 *            is the hue value in degrees between 0 - 360
	 * @param s
	 *            is the saturation percentage between 0 - 100
	 * @param l
	 *            is the luminance percentage between 0 - 100
	 */
	public HSLColor(float h, float s, float l) {
		this.color = toColorObject(h, s, l);
	}

	public float getHue() {
		return hsl[0];
	}

	public float getLuminance() {
		return hsl[2];
	}

	public Color getAsColorObject() {
		return color;
	}

	public float getSaturation() {
		return hsl[1];
	}

	/**
	 * Convert a {@link Color} to it corresponding hsl values.
	 *
	 * @return an array containing the three hsl values.
	 */
	public static float[] fromRGB(Color color) {
		// get rgb values in the range 0 - 1
		float[] rgb = color.getRGBColorComponents(null);
		float r = rgb[0];
		float g = rgb[1];
		float b = rgb[2];

		// min and max rgb values are used in the hsl calculations
		float min = Math.min(r, Math.min(g, b));
		float max = Math.max(r, Math.max(g, b));

		// calculate hue value
		float h = 0;
		if (max == min) {
			h = 0;
		} else if (max == r) {
			h = ((60 * (g - b) / (max - min)) + 360) % 360;
		} else if (max == g) {
			h = (60 * (b - r) / (max - min)) + 120;
		} else if (max == b) {
			h = (60 * (r - g) / (max - min)) + 240;
		}

		// calculate l value
		float l = (max + min) / 2;

		// calculate s value
		float s = 0;
		if (max == min) {
			s = 0;
		} else if (l <= .5f) {
			s = (max - min) / (max + min);
		} else {
			s = (max - min) / (2 - max - min);
		}
		return new float[] { h, s * 100, l * 100 };
	}

	/**
	 * Convert hsl values to a {@link Color} object
	 *
	 * @param h
	 *            hue, value in degree range 0-360
	 * @param s
	 *            saturation, value range 0-100 (percent)
	 * @param l
	 *            lightness, value range 0-100 (percent)
	 * @returns the corresponding {@link Color} object
	 */
	public static Color toColorObject(float h, float s, float l) {
		// Formula needs all values between 0 - 1.
		h = h % 360.0f;
		h /= 360f;
		s /= 100f;
		l /= 100f;

		float q = 0;

		if (l < 0.5) {
			q = l * (1 + s);
		} else {
			q = (l + s) - (s * l);
		}

		float p = 2 * l - q;

		float r = Math.max(0, HueToRGB(p, q, h + (1.0f / 3.0f)));
		float g = Math.max(0, HueToRGB(p, q, h));
		float b = Math.max(0, HueToRGB(p, q, h - (1.0f / 3.0f)));

		r = Math.min(r, 1.0f);
		g = Math.min(g, 1.0f);
		b = Math.min(b, 1.0f);

		return new Color(r, g, b);
	}

	private static float HueToRGB(float p, float q, float h) {
		if (h < 0) {
			h += 1;
		}

		if (h > 1) {
			h -= 1;
		}

		if (6 * h < 1) {
			return p + ((q - p) * 6 * h);
		}

		if (2 * h < 1) {
			return q;
		}

		if (3 * h < 2) {
			return p + ((q - p) * 6 * ((2.0f / 3.0f) - h));
		}

		return p;
	}
}