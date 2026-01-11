package com.pipc.dashboard.establishment.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VivranPatraASummaryDto {

    private String district;

    private Integer manjurPad;          // sanctionPost
    private Integer karyaratPad;        // workingPost
    private Integer riktaPad;           // vacantPost
    private Integer bhavishyaRiktaPad;  // futureVacancy
}
