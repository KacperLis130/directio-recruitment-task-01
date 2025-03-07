package pl.directio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParserTest {

	private final Parser parser = new Parser();

	@ParameterizedTest
	@ValueSource(strings = {
			"",
			" ",
			"\n",
			"\t",
			"     "
	})
	void throws_on_empty_or_blank(String expression) {
		final IllegalArgumentException illegalArgumentException = assertThrows(
				IllegalArgumentException.class,
				() -> parser.parse(expression)
		);

		assertEquals("Empty or blank expression: \"" + expression + "\"", illegalArgumentException.getMessage());
	}

	@Test
	void throws_on_null() {
		final NullPointerException nullPointerException = assertThrows(
				NullPointerException.class,
				() -> parser.parse(null)
		);

		assertEquals("expression is marked non-null but is null", nullPointerException.getMessage());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"+",
			"-",
			"*",
			"/",
			"1+",
			"+1",
			"1 + 2 +",
			"-2 -"
	})
	void throw_on_missing_operands_expression(String expression) {
		Throwable t = assertThrows(
				Parser.InvalidExpressionException.class,
				() -> parser.parse(expression)
		);

		assertTrue(t.getMessage().startsWith("Missing operands for operation:"));
	}

	@Test
	void throws_on_division_by_0() {
		final ArithmeticException arithmeticException = assertThrows(
				ArithmeticException.class,
				() -> parser.parse("1 / 0")
		);

		assertEquals("Division by 0", arithmeticException.getMessage());
	}

	@Test
	void evaluates_left_to_right() {
		assertEquals(
				0,
				parser.parse("1 / 1 * 0")
		);
	}

	@Test
	void throws_on_unknown_symbol() {
		final Parser.InvalidExpressionException invalidExpressionException = assertThrows(
				Parser.InvalidExpressionException.class,
				() -> parser.parse("1 ^ 1")
		);

		assertEquals("Unknown operator \"^\"", invalidExpressionException.getMessage());
	}

	@ParameterizedTest
	@MethodSource("getHappyPathTestCases")
	void happy_path(
			String expression,
			int expectedResult
	) {
		assertEquals(
				expectedResult,
				parser.parse(expression)
		);
	}

	static Stream<Arguments> getHappyPathTestCases() {
		return Stream.of(
				Arguments.of("1", 1),
				Arguments.of("-1", -1),
				Arguments.of("1 + -1", 0),
				Arguments.of("1 + 1", 2),
				Arguments.of("1 - 1", 0),
				Arguments.of("1 * 1", 1),
				Arguments.of("1 / 1", 1),
				Arguments.of("2 + 3", 5),
				Arguments.of("3 * 2 + 1", 7),
				Arguments.of("3 * -2 + 6", 0)
		);
	}
}
