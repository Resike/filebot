package net.filebot.similarity;

import static java.util.regex.Pattern.*;
import static net.filebot.util.RegularExpressions.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Normalization {

	public static final Pattern APOSTROPHE = compile("['`´‘’ʻ]+");
	public static final Pattern PUNCTUATION_OR_SPACE = compile("[\\p{Punct}\\p{Space}]+", UNICODE_CHARACTER_CLASS);
	public static final Pattern WORD_SEPARATOR_PUNCTUATION = compile("[:?._]");

	public static final Pattern TRAILING_PARENTHESIS = compile("(?<!^)[(]([^)]*)[)]$");
	public static final Pattern TRAILING_PUNCTUATION = compile("[!?.]+$");
	public static final Pattern EMBEDDED_CHECKSUM = compile("[\\(\\[](\\p{XDigit}{8})[\\]\\)]");

	private static final Pattern[] brackets = new Pattern[] { compile("\\([^\\(]*\\)"), compile("\\[[^\\[]*\\]"), compile("\\{[^\\{]*\\}") };

	private static final char[] doubleQuotes = new char[] { '\'', '\u0060', '\u00b4', '\u2018', '\u2019', '\u02bb' };
	private static final char[] singleQuotes = new char[] { '\"', '\u201c', '\u201d' };

	public static String normalizeQuotationMarks(String name) {
		for (char[] cs : new char[][] { doubleQuotes, singleQuotes }) {
			for (char c : cs) {
				name = name.replace(c, cs[0]);
			}
		}
		return name;
	}

	public static String trimTrailingPunctuation(CharSequence name) {
		return TRAILING_PUNCTUATION.matcher(name).replaceAll("").trim();
	}

	public static String normalizePunctuation(String name) {
		// remove/normalize special characters
		name = APOSTROPHE.matcher(name).replaceAll("");
		name = PUNCTUATION_OR_SPACE.matcher(name).replaceAll(" ");
		return name.trim();
	}

	public static String normalizeBrackets(String name) {
		// remove group names and checksums, any [...] or (...)
		for (Pattern it : brackets) {
			name = it.matcher(name).replaceAll(" ");
		}
		return name.trim();
	}

	public static String normalizeSpace(CharSequence name, String replacement) {
		return replaceSpace(WORD_SEPARATOR_PUNCTUATION.matcher(name).replaceAll(" ").trim(), replacement);
	}

	public static String replaceSpace(CharSequence name, String replacement) {
		return SPACE.matcher(name).replaceAll(replacement);
	}

	public static String replaceColon(CharSequence name, String ratio, String colon) {
		return COLON.matcher(RATIO.matcher(name).replaceAll(ratio)).replaceAll(colon);
	}

	public static String getEmbeddedChecksum(CharSequence name) {
		Matcher m = EMBEDDED_CHECKSUM.matcher(name);
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}

	public static String removeEmbeddedChecksum(CharSequence name) {
		// match embedded checksum and surrounding brackets
		return EMBEDDED_CHECKSUM.matcher(name).replaceAll("");
	}

	public static String removeTrailingBrackets(CharSequence name) {
		// remove trailing braces, e.g. Doctor Who (2005) -> Doctor Who
		return TRAILING_PARENTHESIS.matcher(name).replaceAll("").trim();
	}

	public static String truncateText(String title, int limit) {
		if (title.length() < limit) {
			return title;
		}

		String[] words = SPACE.split(title);
		StringBuilder s = new StringBuilder();

		for (int i = 0; i < words.length && s.length() + words[i].length() < limit; i++) {
			if (i > 0) {
				s.append(' ');
			}
			s.append(words[i]);
		}

		return s.toString().trim();
	}

}
