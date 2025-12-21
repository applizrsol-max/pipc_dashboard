package com.pipc.dashboard.utility;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class ApplicationError {

	private String errorCode;
	private String errorDescription;

	public ApplicationError(String errorCode, String errorDescription) {
		this.errorCode = errorCode;
		this.errorDescription = errorDescription;
	}

}
