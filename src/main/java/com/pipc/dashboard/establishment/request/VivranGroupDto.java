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
public class VivranGroupDto {

	private String groupName;
	private List<VivranRowDto> rows;
}
