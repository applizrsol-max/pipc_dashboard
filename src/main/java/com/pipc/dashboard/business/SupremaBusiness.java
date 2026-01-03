package com.pipc.dashboard.business;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.pipc.dashboard.suprama.repository.SupremaEntity;
import com.pipc.dashboard.suprama.request.SupremaRequest;
import com.pipc.dashboard.suprama.response.SupremaResponse;

public interface SupremaBusiness {

	SupremaResponse saveOrUpdateSuprema(SupremaRequest supremaRequest);

	List<SupremaEntity> getSupremaByProjectYear(String projectYear);

	ResponseEntity<InputStreamResource> downloadSupremaExcel(String year) throws IOException;

}
