package com.imocc.miaosha.validator; 

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import com.imocc.miaosha.util.ValidatorUtil;

/**
 * IsMobile注解的扩充类
 * 
 * @author nanshoudabaojian
 *
 */
public class IsMobileValidator implements ConstraintValidator<IsMobile, String>{

	private boolean required = false;
	
	public void initialize(IsMobile constraintAnnotation) {
		required = constraintAnnotation.required();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if(required){
			return ValidatorUtil.isMobile(value);
		}else{
			if(StringUtils.isEmpty(value)){
				return true;
			}else{
				return ValidatorUtil.isMobile(value);
			}
		}
	}

}
