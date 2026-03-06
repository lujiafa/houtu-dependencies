package io.github.lujiafa.houtu.web.validation.validator;

import io.github.lujiafa.houtu.web.validation.constroins.NotXss;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * @date 2019年8月27日
 * @author jonlu
 */
public class XssConstraintValidator implements ConstraintValidator<NotXss, String> {
	
	private static List<Pattern> plist = new ArrayList<Pattern>();
	
	static {
		plist.add(Pattern.compile("<script.*>", Pattern.CASE_INSENSITIVE));
		plist.add(Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE));
		plist.add(Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE));
		plist.add(Pattern.compile("eval\\((.*)\\)", Pattern.CASE_INSENSITIVE));
		plist.add(Pattern.compile("onload(.*)=", Pattern.CASE_INSENSITIVE));
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return !plist.parallelStream().anyMatch(p -> p.matcher(value).find());
	}
	
}
