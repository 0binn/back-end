package project.seatsence.src.utilization.api.reservation;

import static project.seatsence.global.code.ResponseCode.*;
import static project.seatsence.src.utilization.domain.reservation.ReservationStatus.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import project.seatsence.global.config.security.JwtProvider;
import project.seatsence.global.exceptions.BaseException;
import project.seatsence.global.response.SliceResponse;
import project.seatsence.src.store.domain.Store;
import project.seatsence.src.store.domain.StoreChair;
import project.seatsence.src.store.domain.StoreSpace;
import project.seatsence.src.store.service.StoreChairService;
import project.seatsence.src.store.service.StoreService;
import project.seatsence.src.store.service.StoreSpaceService;
import project.seatsence.src.user.domain.User;
import project.seatsence.src.user.service.UserService;
import project.seatsence.src.utilization.domain.reservation.Reservation;
import project.seatsence.src.utilization.dto.reservation.request.AllReservationsForChairAndDateRequest;
import project.seatsence.src.utilization.dto.reservation.request.SeatReservationRequest;
import project.seatsence.src.utilization.dto.reservation.request.SpaceReservationRequest;
import project.seatsence.src.utilization.dto.reservation.response.AllReservationsForChairAndDateResponse;
import project.seatsence.src.utilization.dto.reservation.response.UserReservationListResponse;
import project.seatsence.src.utilization.service.reservation.ReservationService;
import project.seatsence.src.utilization.service.reservation.UserReservationService;

@RestController
@RequestMapping("/v1/reservations/users")
@Tag(name = "05. [reservation]", description = "유저에 관한 예약 API")
@Validated
@RequiredArgsConstructor
public class UserReservationApi {
    private final UserReservationService userReservationService;
    private final StoreService storeService;
    private final StoreChairService storeChairService;
    private final StoreSpaceService storeSpaceService;
    private final UserService userService;
    private final ReservationService reservationService;

    @Operation(summary = "유저 좌석 예약")
    @PostMapping("/seat")
    public void seatReservation(@RequestBody SeatReservationRequest seatReservationRequest) {
        StoreChair storeChairFound =
                storeChairService.findByIdAndState(seatReservationRequest.getStoreChairId());

        Store storeFound =
                storeService.findById(storeChairFound.getStoreSpace().getTempStore().getId());

        if (storeSpaceService.reservationUnitIsOnlySpace(storeChairFound.getStoreSpace())) {
            throw new BaseException(INVALID_RESERVATION_UNIT);
        }

        if (!userReservationService.isPossibleReservationTimeUnit(
                seatReservationRequest.getStartSchedule(),
                seatReservationRequest.getEndSchedule())) {
            throw new BaseException(INVALID_RESERVATION_TIME);
        }

        if (!userReservationService.isMoreThanMinimumReservationTime(
                seatReservationRequest.getStartSchedule(),
                seatReservationRequest.getEndSchedule())) {
            throw new BaseException(INVALID_RESERVATION_TIME);
        }

        if (!userReservationService.reservationDateTimeIsAfterOrEqualNowDateTime(
                seatReservationRequest.getStartSchedule())) {
            throw new BaseException(INVALID_RESERVATION_TIME);
        }

        if (!userReservationService.startDateIsEqualEndDate(
                seatReservationRequest.getStartSchedule(),
                seatReservationRequest.getEndSchedule())) {
            throw new BaseException(INVALID_RESERVATION_TIME);
        }

        if (!userReservationService.startDateTimeIsBeforeEndDateTime(
                seatReservationRequest.getStartSchedule(),
                seatReservationRequest.getEndSchedule())) {
            throw new BaseException(INVALID_RESERVATION_TIME);
        }

        // 당일예약 유효성 체크
        if (userReservationService.isSameDayReservation(
                seatReservationRequest.getStartSchedule())) {
            if (!userReservationService.isPossibleSameDayReservationStartDateAndTime(
                    seatReservationRequest.getStartSchedule())) {
                throw new BaseException(INVALID_RESERVATION_TIME);
            }
        }

        User userFound = userService.findByIdAndState(seatReservationRequest.getUserId());

        Reservation reservation =
                Reservation.builder()
                        .store(storeFound)
                        .storeChair(storeChairFound)
                        .storeSpace(null)
                        .user(userFound)
                        .startSchedule(seatReservationRequest.getStartSchedule())
                        .endSchedule(seatReservationRequest.getEndSchedule())
                        .reservationStatus(PENDING)
                        .build();

        userReservationService.saveReservation(reservation);
    }

    @Operation(summary = "유저 스페이스 예약")
    @PostMapping("/space")
    public void spaceReservation(@RequestBody SpaceReservationRequest spaceReservationRequest) {
        StoreSpace storeSpaceFound =
                storeSpaceService.findByIdAndState(spaceReservationRequest.getStoreSpaceId());
        Store storeFound = storeService.findById(storeSpaceFound.getTempStore().getId());

        if (storeSpaceService.reservationUnitIsOnlySeat(storeSpaceFound)) {
            throw new BaseException(INVALID_RESERVATION_UNIT);
        }

        if (!userReservationService.isPossibleReservationTimeUnit(
                spaceReservationRequest.getStartSchedule(),
                spaceReservationRequest.getEndSchedule())) {
            throw new BaseException(INVALID_RESERVATION_TIME);
        }

        if (!userReservationService.isMoreThanMinimumReservationTime(
                spaceReservationRequest.getStartSchedule(),
                spaceReservationRequest.getEndSchedule())) {
            throw new BaseException(INVALID_RESERVATION_TIME);
        }

        if (!userReservationService.reservationDateTimeIsAfterOrEqualNowDateTime(
                spaceReservationRequest.getStartSchedule())) {
            throw new BaseException(INVALID_RESERVATION_TIME);
        }

        if (!userReservationService.startDateIsEqualEndDate(
                spaceReservationRequest.getStartSchedule(),
                spaceReservationRequest.getEndSchedule())) {
            throw new BaseException(INVALID_RESERVATION_TIME);
        }

        if (!userReservationService.startDateTimeIsBeforeEndDateTime(
                spaceReservationRequest.getStartSchedule(),
                spaceReservationRequest.getEndSchedule())) {
            throw new BaseException(INVALID_RESERVATION_TIME);
        }

        // 당일예약 유효성 체크
        if (userReservationService.isSameDayReservation(
                spaceReservationRequest.getStartSchedule())) {
            if (!userReservationService.isPossibleSameDayReservationStartDateAndTime(
                    spaceReservationRequest.getStartSchedule())) {
                throw new BaseException(INVALID_RESERVATION_TIME);
            }
        }

        User userFound = userService.findByIdAndState(spaceReservationRequest.getUserId());

        Reservation reservation =
                Reservation.builder()
                        .store(storeFound)
                        .storeChair(null)
                        .storeSpace(storeSpaceFound)
                        .user(userFound)
                        .startSchedule(spaceReservationRequest.getStartSchedule())
                        .endSchedule(spaceReservationRequest.getEndSchedule())
                        .reservationStatus(PENDING)
                        .build();

        userReservationService.saveReservation(reservation);
    }

    @Operation(
            summary = "유저 예약 현황 조회",
            description = "유저의 '예약 대기중', '승인된 예약', '거절된 예약', '취소한 예약'의 정보를 불러옵니다.")
    @GetMapping("/my-list")
    public SliceResponse<UserReservationListResponse> getUserReservationList(
            @RequestHeader("Authorization") String token,
            @Parameter(name = "조회할 예약 상태값", in = ParameterIn.QUERY, example = "대기/취소/승인/거절")
                    @RequestParam("reservationStatus")
                    String reservationStatus,
            @ParameterObject @PageableDefault(page = 1, size = 10) Pageable pageable) {
        String userEmail = JwtProvider.getUserEmailFromToken(token);
        return userReservationService.getUserReservationList(
                userEmail, reservationStatus, pageable);
    }

    @Operation(summary = "유저 예약 취소", description = "유저가 예약했던 좌석 혹은 스페이스의 예약을 취소합니다.")
    @DeleteMapping("/{reservation-id}")
    public void cancelReservation(
            @Parameter(name = "예약 식별자", in = ParameterIn.PATH, example = "1")
                    @PathVariable("reservation-id")
                    Long reservationId) {
        Reservation reservation =
                reservationService.findByIdAndState(reservationId); // Todo : Refactoring

        userReservationService.cancelReservation(reservation);
    }

    @Operation(
            summary = "특정 좌석과 날짜에 대한 모든 예약 조회",
            description = "선택한 좌석과 날짜에 예약 되어있는(대기or승인) 모든 예약 내역을 조회합니다.")
    @GetMapping("/reserved-list/seat/date")
    public AllReservationsForChairAndDateResponse getAllReservationsForChairAndDate(
            @RequestBody
                    AllReservationsForChairAndDateRequest allReservationsForChairAndDateRequest) {

        List<AllReservationsForChairAndDateResponse.ReservationForChairAndDate> mappedReservations =
                userReservationService.getAllReservationsForChairAndDate(
                        allReservationsForChairAndDateRequest);

        AllReservationsForChairAndDateResponse response =
                new AllReservationsForChairAndDateResponse(mappedReservations);

        return response;
    }
}
