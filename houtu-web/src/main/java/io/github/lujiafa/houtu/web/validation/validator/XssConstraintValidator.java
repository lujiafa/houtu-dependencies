package io.github.lujiafa.houtu.web.validation.validator;

import io.github.lujiafa.houtu.web.validation.constroins.NotXss;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.regex.Pattern;


/**
 * @date 2019年8月27日
 * @author jonlu
 */
public class XssConstraintValidator implements ConstraintValidator<NotXss, String> {
	
	private static final List<Pattern> plist = List.of(
		Pattern.compile("<script.*>", Pattern.CASE_INSENSITIVE),
		Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
		Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
		Pattern.compile("eval\\((.*)\\)", Pattern.CASE_INSENSITIVE),
		Pattern.compile("onload(.*)=", Pattern.CASE_INSENSITIVE)
	);

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) return true;
		return !plist.stream().anyMatch(p -> p.matcher(value).find());
	}
	
}
