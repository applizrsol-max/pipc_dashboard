package com.pipc.dashboard.establishment.request;

import java.util.List;

import com.pipc.dashboard.bhusmapadan.request.PraptraMasterDataRowRequest;
import com.pipc.dashboard.utility.BaseResponse;

import lombok.Data;

@Data
public class MasterDataRequest extends BaseResponse{
	private String year;
	private List<PraptraMasterDataRowRequest> rows;
}
