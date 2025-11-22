package lucee.commons.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SensitiveDataSanitizer - A utility class for detecting and masking sensitive information in
 * strings. This class can identify and sanitize various types of sensitive data such as passwords,
 * API keys, and JWT tokens from input strings.
 */
public final class SensitiveDataSanitizer {

	// Regex patterns for sensitive data as constants
	private static final String PASSWORD_PATTERN = "(?i)(?:password|passwd|pwd)[\\s]*[=:][\\s]*([^\\s&,;]+)";
	private static final String API_KEY_PATTERN = "(?i)(?:api[_-]?key|apikey|access[_-]?key|token)[\\s]*[=:][\\s]*([^\\s&,;]+)";
	private static final String SECRET_KEY_PATTERN = "(?i)(?:secret|private[_-]?key)[\\s]*[=:][\\s]*([^\\s&,;]+)";
	private static final String JWT_TOKEN_PATTERN = "eyJ[a-zA-Z0-9_-]+\\.eyJ[a-zA-Z0-9_-]+\\.[a-zA-Z0-9_-]+";
	private static final String BEARER_TOKEN_PATTERN = "(?i)(?:bearer)[\\s]+([a-zA-Z0-9_\\-\\.]+)";
	private static final String GENERIC_API_KEY_PATTERN = "[a-zA-Z0-9]{20,40}";
	private static final String JSON_PASSWORD_PATTERN = "(?:\\\"|\\')(?:password|passwd|pwd)(?:\\\"|\\')[\\s]*[:=][\\s]*(?:\\\"|\\\')?([^\\\"\\'\\,;\\s]+)(?:\\\"|\\\')?";
	private static final String JSON_API_KEY_PATTERN = "(?:\\\"|\\')(?:api[_-]?key|apikey|access[_-]?key|token)(?:\\\"|\\')[\\s]*[:=][\\s]*(?:\\\"|\\\')?([^\\\"\\'\\,;\\s]+)(?:\\\"|\\\')?";

	// Default mask for replacing sensitive data
	public static final String DEFAULT_MASK = "****";

	// Class to hold pattern information
	private static class PatternInfo {
		Pattern pattern;
		int group;

		PatternInfo(String patternStr, int group) {
			this.pattern = Pattern.compile(patternStr);
			this.group = group;
		}
	}

	// List of default patterns with their associated information
	private static final List<PatternInfo> PATTERNS = new ArrayList<>();

	static {
		// Initialize the patterns
		PATTERNS.add(new PatternInfo(PASSWORD_PATTERN, 1));
		PATTERNS.add(new PatternInfo(API_KEY_PATTERN, 1));
		PATTERNS.add(new PatternInfo(SECRET_KEY_PATTERN, 1));
		PATTERNS.add(new PatternInfo(JWT_TOKEN_PATTERN, 0));
		PATTERNS.add(new PatternInfo(BEARER_TOKEN_PATTERN, 1));
		PATTERNS.add(new PatternInfo(GENERIC_API_KEY_PATTERN, 0));
		PATTERNS.add(new PatternInfo(JSON_PASSWORD_PATTERN, 1));
		PATTERNS.add(new PatternInfo(JSON_API_KEY_PATTERN, 1));
	}

	/**
	 * Sanitizes sensitive information from the input text.
	 * 
	 * @param text The text to sanitize
	 * @return Sanitized text with sensitive data masked
	 */
	public static String sanitize(String text) {
		return sanitize(text, DEFAULT_MASK);
	}

	/**
	 * Sanitizes sensitive information from the input text.
	 * 
	 * @param text The text to sanitize
	 * @return Sanitized text with sensitive data masked
	 */
	public static String sanitize(String text, String replacement) {
		if (StringUtil.isEmpty(text)) {
			return text;
		}

		String sanitizedText = text;

		// Process each pattern
		for (PatternInfo patternInfo: PATTERNS) {
			Matcher matcher = patternInfo.pattern.matcher(sanitizedText);
			StringBuilder sb = new StringBuilder();

			while (matcher.find()) {
				// Handle the replacement based on the group
				if (patternInfo.group == 0) {
					matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
				}
				else {
					String matchedGroup = matcher.group(patternInfo.group);
					String fullMatch = matcher.group(0);
					String modifiedMatch = fullMatch.replace(matchedGroup, replacement);
					matcher.appendReplacement(sb, Matcher.quoteReplacement(modifiedMatch));
				}
			}

			matcher.appendTail(sb);
			sanitizedText = sb.toString();
		}

		return sanitizedText;
	}

	/**
	 * Main method for testing the sanitization functionality.
	 */
	public static void main(String[] args) {
		// Test cases
		String[] testCases = { "User login with apiKey=abc123xyz and password=secretPass",
				"Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
				"config={\"password\": \"supersecret\", \"api_key\": \"abcdef1234567890\"}", "Your AWS access_key=AKIAIOSFODNN7EXAMPLE",
				"A random string with no secrets: hello world", "http://example.com/api?sensitive=true&password=mysecretpassword&token=12345",
				"config={\"password\": \"supersecret\", \"api_key\": \"abcdef1234567890\"}" };

		System.out.println("Sanitization results:");
		for (String testCase: testCases) {
			System.out.println("Original: " + testCase);
			System.out.println("Sanitized: " + sanitize(testCase, "xxxx"));
			System.out.println();
		}
		System.out.println("Sanitized: " + sanitize(null, "xxxx"));

	}
}