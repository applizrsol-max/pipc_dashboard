package com.pipc.dashboard.establishment.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeputyReturnADivisionDto {

	private String karyalayacheNav;

	private List<DeputyReturnARowDto> rows;
}
