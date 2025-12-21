package com.pipc.dashboard.globalexception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.pipc.dashboard.utility.ApplicationError;
import com.pipc.dashboard.utility.BaseResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public BaseResponse handleException(Exception ex) {
		return new BaseResponse(new ApplicationError("400", ex.getMessage()));
	}
}
