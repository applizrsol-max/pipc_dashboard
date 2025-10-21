package com.pipc.dashboard.service;

import org.springframework.data.domain.Page;

import com.pipc.dashboard.suprama.repository.SupremaEntity;
import com.pipc.dashboard.suprama.request.SupremaRequest;
import com.pipc.dashboard.suprama.response.SupremaResponse;

public interface SupremaService {

	SupremaResponse saveOrUpdateSuprema(SupremaRequest supremaRequest);

	Page<SupremaEntity> getSupremaByProjectYear(String projectYear, int page, int size);

}
