package project.seatsence.src.utilization.service;

import static project.seatsence.global.code.ResponseCode.UTILIZATION_NOT_FOUND;
import static project.seatsence.global.entity.BaseTimeAndStateEntity.State.ACTIVE;
import static project.seatsence.src.store.domain.ReservationUnit.*;
import static project.seatsence.src.utilization.domain.UtilizationStatus.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.seatsence.global.exceptions.BaseException;
import project.seatsence.src.store.domain.ReservationUnit;
import project.seatsence.src.store.service.StoreSpaceService;
import project.seatsence.src.utilization.dao.UtilizationRepository;
import project.seatsence.src.utilization.domain.Utilization;
import project.seatsence.src.utilization.domain.UtilizationStatus;
import project.seatsence.src.utilization.dto.response.LoadSeatsCurrentlyInUseResponse;
import project.seatsence.src.utilization.service.reservation.ReservationService;
import project.seatsence.src.utilization.service.walkin.WalkInService;

@Service
@Transactional
@RequiredArgsConstructor
public class UtilizationService {
    private final UtilizationRepository utilizationRepository;
    private final ReservationService reservationService;
    private final WalkInService walkInService;
    private final StoreSpaceService storeSpaceService;

    public Utilization findByIdAndState(Long id) {
        return utilizationRepository
                .findByIdAndState(id, ACTIVE)
                .orElseThrow(() -> new BaseException(UTILIZATION_NOT_FOUND));
    }

    public List<UtilizationStatus> utilizationStatusesCurrentlyInUse() {
        return Arrays.asList(HOLDING, CHECK_IN);
    }

    public Boolean isReservation(Utilization utilization) {
        Boolean isReservation = false;
        if (utilization.getReservation() != null) {
            isReservation = true;
        }
        return isReservation;
    }

    public Boolean isWalkIn(Utilization utilization) {
        Boolean isWalkIn = false;
        if (utilization.getWalkIn() != null) {
            isWalkIn = true;
        }
        return isWalkIn;
    }

    public List<LoadSeatsCurrentlyInUseResponse.ChairCurrentlyInUse> loadAllChairsCurrentlyInUse(
            Long spaceId) {
        List<LoadSeatsCurrentlyInUseResponse.ChairCurrentlyInUse> mappedUtilizations =
                new ArrayList<>();

        List<Utilization> allUtilizationsByChair = findSeatCurrentlyInUseByUnit(spaceId, CHAIR);

        mappedUtilizations =
                allUtilizationsByChair.stream()
                        .map(
                                utilization ->
                                        LoadSeatsCurrentlyInUseResponse.ChairCurrentlyInUse.from(
                                                utilization))
                        .collect(Collectors.toList());

        return mappedUtilizations;
    }

    /**
     * 현재 이용되고있는 좌석을 이용 단위에 따라 조회
     *
     * @param storeSpaceId 조회하고자 하는 스페이스 식별자
     * @param utilizationUnit 이용 단위
     * @return
     */
    public List<Utilization> findSeatCurrentlyInUseByUnit(
            Long storeSpaceId, ReservationUnit utilizationUnit) {
        List<UtilizationStatus> utilizationStatuses = utilizationStatusesCurrentlyInUse();

        return utilizationRepository.findSeatCurrentlyInUseByUnit(
                storeSpaceId,
                utilizationUnit,
                utilizationStatuses.get(0),
                utilizationStatuses.get(1),
                ACTIVE);
    }
}