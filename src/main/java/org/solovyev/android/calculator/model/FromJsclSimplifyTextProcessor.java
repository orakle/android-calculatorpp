package org.solovyev.android.calculator.model;

import jscl.MathContext;
import jscl.math.Generic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.calculator.math.MathType;

import java.util.Arrays;
import java.util.List;

/**
 * User: serso
 * Date: 10/20/11
 * Time: 2:59 PM
 */
public class FromJsclSimplifyTextProcessor implements TextProcessor<String, Generic> {

	@NotNull
	private final MathContext mathContext;

	public FromJsclSimplifyTextProcessor(@NotNull MathContext mathContext) {
		this.mathContext = mathContext;
	}

	@NotNull
	@Override
	public String process(@NotNull Generic from) throws CalculatorParseException {
		final String s = from.toString();
		return process(s);
	}

	public String process(@NotNull String s) {
		final StringBuilder sb = new StringBuilder();

		final NumberBuilder nb = new NumberBuilder(false, CalculatorEngine.instance.getEngine());
		for (int i = 0; i < s.length(); i++) {
			final MathType.Result mathTypeResult = MathType.getType(s, i, nb.isHexMode());

			nb.process(sb, mathTypeResult, null);

			i = mathTypeResult.processFromJscl(sb, i);
		}

		nb.processNumber(sb, null);

		return removeMultiplicationSigns(sb.toString());
	}

	@NotNull
	private String removeMultiplicationSigns(String s) {
		final StringBuilder sb = new StringBuilder();

		MathType.Result mathTypeBefore;
		MathType.Result mathType = null;
		MathType.Result mathTypeAfter = null;

		for (int i = 0; i < s.length(); i++) {
			mathTypeBefore = mathType;
			if (mathTypeAfter == null) {
				mathType = MathType.getType(s, i, false);
			} else {
				mathType = mathTypeAfter;
			}

			char ch = s.charAt(i);
			if (ch == '*') {
				if (i + 1 < s.length()) {
					mathTypeAfter = MathType.getType(s, i + 1, false);
				} else {
					mathTypeAfter = null;
				}

				if (needMultiplicationSign(mathTypeBefore == null ? null : mathTypeBefore.getMathType(), mathTypeAfter == null ? null : mathTypeAfter.getMathType())) {
					sb.append("×");
				}

			} else {
				if (mathType.getMathType() == MathType.constant || mathType.getMathType() == MathType.function || mathType.getMathType() == MathType.operator) {
					sb.append(mathType.getMatch());
					i += mathType.getMatch().length() - 1;
				} else {
					sb.append(ch);
				}
				mathTypeAfter = null;
			}

		}

		return sb.toString();
	}

	private final List<MathType> mathTypes = Arrays.asList(MathType.function, MathType.constant);

	private boolean needMultiplicationSign(@Nullable MathType mathTypeBefore, @Nullable MathType mathTypeAfter) {
		if (mathTypeBefore == null || mathTypeAfter == null) {
			return true;
		} else if (mathTypes.contains(mathTypeBefore) || mathTypes.contains(mathTypeAfter)) {
			return false;
		} else if ( mathTypeBefore == MathType.close_group_symbol ) {
			return false;
		} else if ( mathTypeAfter == MathType.open_group_symbol ) {
			return false;
		}

		return true;
	}

}
