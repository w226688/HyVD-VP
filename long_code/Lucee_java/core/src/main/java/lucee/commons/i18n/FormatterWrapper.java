package lucee.commons.i18n;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public final class FormatterWrapper {
	public final DateTimeFormatter formatter;
	public final DateTimeFormatter smartFormatter;
	public final DateTimeFormatter strictFormatter;
	public final DateTimeFormatter lenientFormatter;
	public int successCount;
	public final String pattern;
	public final boolean custom;
	public final short type;
	public final ZoneId zone;

	private final boolean hasComma;
	private final boolean hasSlash;
	private final boolean hasColon;
	private final boolean hasWhitespace;
	private final boolean hasHyphen;
	private final boolean hasTimeZone;

	FormatterWrapper(DateTimeFormatter formatter, String pattern, short type, ZoneId zone) {
		this(formatter, pattern, type, zone, false);
	}

	FormatterWrapper(DateTimeFormatter formatter, String pattern, short type, ZoneId zone, boolean custom) {
		this.smartFormatter = formatter;
		this.strictFormatter = formatter.withResolverStyle(ResolverStyle.STRICT);
		this.lenientFormatter = formatter.withResolverStyle(ResolverStyle.LENIENT);
		this.formatter = smartFormatter;

		this.successCount = 0;
		this.pattern = pattern;
		this.type = type;
		this.zone = zone;
		this.custom = custom;

		this.hasComma = pattern.indexOf(',') != -1;
		this.hasSlash = pattern.indexOf('/') != -1;
		this.hasHyphen = pattern.indexOf('-') != -1;
		this.hasColon = pattern.indexOf(':') != -1;
		this.hasTimeZone = pattern.indexOf('z') != -1 || pattern.indexOf('Z') != -1;
		this.hasWhitespace = pattern.chars().anyMatch(Character::isWhitespace);
	}

	@Override
	public String toString() {
		return "Pattern:" + pattern + "; Zone:" + zone + "; Custom:" + custom + "; " + formatter.toString();
	}

	public boolean valid(String str) {
		if (hasComma) {
			if (str.indexOf(',') == -1) return false;
		}
		else {
			if (str.indexOf(',') != -1) return false;
		}
		if (hasHyphen) {
			if (str.indexOf('-') == -1) return false;
		}
		else {
			// timezone also can have a hyphen like "-0800"
			if (str.indexOf('-') != -1 && !hasTimeZone) return false;
		}

		if (hasSlash) {
			if (str.indexOf('/') == -1) return false;
		}
		else {
			if (str.indexOf('/') != -1) return false;
		}

		if (hasColon) {
			if (str.indexOf(':') == -1) return false;
		}
		else {
			if (str.indexOf(':') != -1) return false;
		}

		if (hasWhitespace) {
			if (!str.chars().anyMatch(Character::isWhitespace)) return false;
		}
		else {
			if (str.chars().anyMatch(Character::isWhitespace)) return false;
		}
		return true;
	}

	public boolean validx(String str) {
		if (pattern.length() > str.length()) return false;

		boolean foundComma = false;
		boolean foundHyphen = false;
		boolean foundSlash = false;
		boolean foundColon = false;
		boolean foundWhitespace = false;

		// Single pass through the string
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			switch (c) {
			case ',':
				if (!hasComma) return false;
				foundComma = true;
				break;
			case '-':
				if (!hasHyphen) return false;
				foundHyphen = true;
				break;
			case '/':
				if (!hasSlash) return false;
				foundSlash = true;
				break;
			case ':':
				if (!hasColon) return false;
				foundColon = true;
				break;
			default:
				if (Character.isWhitespace(c)) {
					if (!hasWhitespace) return false;
					foundWhitespace = true;
				}
			}
		}

		// Only check for required characters we haven't found
		if (hasComma && !foundComma) return false;
		if (hasHyphen && !foundHyphen) return false;
		if (hasSlash && !foundSlash) return false;
		if (hasColon && !foundColon) return false;
		if (hasWhitespace && !foundWhitespace) return false;

		return true;
	}

}