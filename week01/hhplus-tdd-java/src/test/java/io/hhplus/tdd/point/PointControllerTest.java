package io.hhplus.tdd.point;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    /**
     * 요청받은 사용자의 포인트를 조회한다.
     */
    @Test
    void point_요청받은_사용자의_포인트를_조회한다() throws Exception {
        UserPoint userPoint = new UserPoint(1L, 0L, System.currentTimeMillis());
        when(pointService.point(userPoint.id())).thenReturn(userPoint);

        mockMvc.perform(get("/point/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userPoint.id()))
            .andExpect(jsonPath("$.point").exists())
            .andExpect(jsonPath("$.updateMillis").exists());
    }

    /**
     * - 컨트롤러의 비즈니스 로직은 service에서 수행되며, 응답 시 `json`을 반환한다.
     * - 요청과 응답의 Content-Type은 `application/json`이다.
     */
    @Test
    void point_요청을_service로_위임하고_json으로_응답한다() throws Exception {
        UserPoint userPoint = new UserPoint(1L, 0L, System.currentTimeMillis());
        when(pointService.point(userPoint.id())).thenReturn(userPoint);

        mockMvc.perform(get("/point/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(userPoint.id()))
            .andExpect(jsonPath("$.point").value(userPoint.point()))
            .andExpect(jsonPath("$.updateMillis").exists());

        verify(pointService).point(userPoint.id());
        verifyNoMoreInteractions(pointService);
    }

    /**
     * - 사용자 아이디는 숫자만 허용된다.
     */
    @Test
    void point_사용자_id가_숫자가_아니면_400_에러를_반환한다() throws Exception {
        mockMvc.perform(get("/point/abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").exists())
            .andExpect(jsonPath("$.message").exists());

        verifyNoInteractions(pointService);
    }

    /**
     * - 컨트롤러의 비즈니스 로직은 service에서 수행되며, 응답 시 `json`을 반환한다.
     * - 요청과 응답의 Content-Type은 `application/json`이다.
     */
    @Test
    void charge_요청을_service로_위임하고_json으로_응답한다() throws Exception {
        // given
        long userId = 1L;
        long amount = 1000L;
        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());
        when(pointService.charge(userPoint.id(), amount)).thenReturn(userPoint);

        // when & then
        mockMvc.perform(
                patch("/point/{id}/charge", userId)
                    .contentType("application/json")
                    .content(String.valueOf(amount))
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(userPoint.id()))
            .andExpect(jsonPath("$.point").value(userPoint.point()))
            .andExpect(jsonPath("$.updateMillis").exists());

        verify(pointService).charge(userPoint.id(), amount);
        verifyNoMoreInteractions(pointService);
    }

    /**
     * 사용자 아이디는 숫자만 허용된다.
     */
    @Test
    void charge_사용자_id가_숫자가_아니면_400_에러가_발생한다() throws Exception {
        mockMvc.perform(
                patch("/point/abc/charge")
                    .contentType("application/json")
                    .content(String.valueOf(1000L))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").exists())
            .andExpect(jsonPath("$.message").exists());

        verifyNoInteractions(pointService);
    }

    /**
     * 요청받은 사용자의 포인트를 충전한다.
     */
    @Test
    void 요청받은_사용자의_포인트를_충전한다() throws Exception {
        // given
        long userId = 1L;
        long amount = 1000L;
        when(pointService.charge(userId, amount))
            .thenReturn(new UserPoint(userId, amount, System.currentTimeMillis()));

        // when & then
        mockMvc.perform(
                patch("/point/1/charge")
                    .contentType("application/json")
                    .content(String.valueOf(amount))
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.point").exists())
            .andExpect(jsonPath("$.updateMillis").exists());

        verify(pointService).charge(userId, amount);
        verifyNoMoreInteractions(pointService);
    }

}