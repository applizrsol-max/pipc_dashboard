package com.pipc.dashboard.business;

import com.pipc.dashboard.drawing.request.DamInspectionRequest;
import com.pipc.dashboard.drawing.request.DamNalikaRequest;
import com.pipc.dashboard.drawing.request.DamSafetyRequest;
import com.pipc.dashboard.drawing.request.PralambitBhusampadanRequest;
import com.pipc.dashboard.drawing.response.DamInspectionResponse;
import com.pipc.dashboard.drawing.response.DamNalikaResponse;
import com.pipc.dashboard.drawing.response.DamSafetyResponse;
import com.pipc.dashboard.drawing.response.PralambitBhusampadanResponse;

public interface DrawingBusiness {

	DamSafetyResponse saveOrUpdateDamSafety(DamSafetyRequest damSafetyRequest);

	DamSafetyResponse getDamSafetyData(String year, int page, int size);

	DamInspectionResponse saveOrUpdateDamInspection(DamInspectionRequest damInspectionRequest);

	DamInspectionResponse getDamInspectionData(String year, String period, String departmentKey, int page, int size);

	DamNalikaResponse saveOrUpdateNalika(DamNalikaRequest request);

	DamNalikaResponse getNalikaByPeriod(String period, String departmentKey, int page, int size);

	PralambitBhusampadanResponse saveOrUpdatePralambitBhusampadan(PralambitBhusampadanRequest req);

	PralambitBhusampadanResponse getPralambitBhusampadan(String period, String star, int page, int size);

}
