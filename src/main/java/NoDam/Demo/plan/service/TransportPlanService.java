package NoDam.Demo.plan.service;

import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.domain.PlanStatus;
import NoDam.Demo.plan.domain.TransportPlan;
import NoDam.Demo.plan.domain.TransportLeg;
import NoDam.Demo.plan.dto.response.RouteInfo;
import NoDam.Demo.plan.repository.DatePlanRepository;
import NoDam.Demo.plan.repository.PlacePlanRepository;
import NoDam.Demo.plan.repository.TransportPlanRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransportPlanService {

    // todo :: date plan status를 변경하는 flag 판정 함수중 한개임! -> date plan으로 옮겨서 판단 해야함
    // todo : date plan domain에서 언제 date plan status 를 변경할지 고려해야함 (이것도 모두 수정 필요??)
    // todo : 위에 todo완료후 해당 class 삭제
    public void completeTransportPlanning(DatePlan datePlan) {

    }
}
