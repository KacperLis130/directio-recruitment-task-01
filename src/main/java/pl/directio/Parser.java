package pl.directio;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.StandardException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiFunction;

class Parser {

	@RequiredArgsConstructor
	private enum Operator {
		ADD(100, Integer::sum),
		SUBTRACT(100, (a, b) -> a - b),
		MULTIPLY(200, (a, b) -> a * b),
		DIVIDE(
				200, (a, b) -> {
			if (b == 0) {
				throw new ArithmeticException("Division by 0");
			} else {
				return a / b;
			}
		}
		);

		private final int precedence;
		private final BiFunction<Integer, Integer, Integer> operation;

		public int apply(
				int a,
				int b
		) {
			return operation.apply(a, b);
		}
	}

	@StandardException
	public static class InvalidExpressionException extends RuntimeException {

	}

	public int parse(@NonNull String expression) {
		if (expression.isBlank()) {
			throw new IllegalArgumentException("Empty or blank expression: \"" + expression + "\"");
		}

		final Deque<Integer> numbers = new ArrayDeque<>();
		final Deque<Operator> operators = new ArrayDeque<>();

		final StringBuilder buffer = new StringBuilder();
		final char[] chars = expression.toCharArray();

		for (int i = 0; i < chars.length; ++i) {
			final char current = chars[i];
			final char next = i + 1 < chars.length ? chars[i + 1] : '\0';

			if (Character.isDigit(current) || (current == '-' && Character.isDigit(next))) {
				buffer.append(current);
			} else {
				if (!buffer.isEmpty()) {
					final int number = Integer.parseInt(buffer.toString());
					numbers.push(number);
					buffer.setLength(0);
				}

				if (Character.isWhitespace(current)) {
					// do nothing
				} else {
					final Operator newOperator = switch (current) {
						case '+' -> Operator.ADD;
						case '-' -> Operator.SUBTRACT;
						case '*' -> Operator.MULTIPLY;
						case '/' -> Operator.DIVIDE;
						default -> throw new InvalidExpressionException("Unknown operator \"" + current + "\"");
					};
					while (!operators.isEmpty() && newOperator.precedence <= operators.peek().precedence) {
						final Operator operator = operators.pop();
						if (numbers.size() < 2) {
							throw new InvalidExpressionException("Missing operands for operation: " + operator.name());
						}

						final Integer b = numbers.pop();
						final Integer a = numbers.pop();
						final int result = operator.apply(
								a,
								b
						);

						numbers.push(result);
					}
					operators.push(newOperator);
				}
			}
		}

		if (!buffer.isEmpty()) {
			final int number = Integer.parseInt(buffer.toString());
			numbers.push(number);
			buffer.setLength(0);
		}

		while (!operators.isEmpty()) {
			final Operator operator = operators.pop();
			if (numbers.size() < 2) {
				throw new InvalidExpressionException("Missing operands for operation: " + operator.name());
			}

			final Integer b = numbers.pop();
			final Integer a = numbers.pop();
			final int result = operator.apply(
					a,
					b
			);

			numbers.push(result);
		}

		return numbers.pop();
	}
}
