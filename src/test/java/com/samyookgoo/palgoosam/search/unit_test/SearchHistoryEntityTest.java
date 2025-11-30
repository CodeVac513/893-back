package com.samyookgoo.palgoosam.search.unit_test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.samyookgoo.palgoosam.global.exception.ErrorCode;
import com.samyookgoo.palgoosam.search.domain.SearchHistory;
import com.samyookgoo.palgoosam.search.exception.SearchHistoryBadRequestException;
import com.samyookgoo.palgoosam.user.domain.User;
import com.samyookgoo.palgoosam.user.exception.UserForbiddenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DisplayName("SearchHistory 유닛 테스트")
class SearchHistoryEntityTest {

    @Test
    @DisplayName("검색 기록은 검색 카운트를 1회 증가시키고, 논리적인 삭제 상태를 복구할 수 있다.")
    public void restoreAndIncrement_DeletedRecord_RestoresAndIncrementsCount() {
        //given
        SearchHistory target = SearchHistory.builder()
                .isDeleted(true)
                .searchCount(1L)
                .build();

        SearchHistory expected = SearchHistory.builder()
                .isDeleted(false)
                .searchCount(2L)
                .build();
        //when
        target.restoreAndIncrement();

        //then
        assertThat(target.getSearchCount()).isEqualTo(expected.getSearchCount());
        assertThat(target.getIsDeleted()).isEqualTo(expected.getIsDeleted());
    }

    @Test
    @DisplayName("검색 기록은 논리 삭제를 할 수 있다.")
    public void delete_ValidRecord_MarksAsDeleted() {
        //given
        SearchHistory target = SearchHistory.builder()
                .isDeleted(false)
                .build();

        SearchHistory expected = SearchHistory.builder()
                .isDeleted(true)
                .build();
        //when
        target.delete();

        //then
        assertThat(target.getIsDeleted()).isEqualTo(expected.getIsDeleted());
    }

    @Test
    @DisplayName("유저가 검색 권한에 대한 권한이 없다면 Forbidden 예외를 던진다.")
    public void checkPermission_UnauthorizedUser_ThrowsForbiddenException() {
        //given
        User userWithPermission = User.builder()
                .id(1L)
                .build();

        User userWithoutPermission = User.builder()
                .id(2L)
                .build();

        SearchHistory target = SearchHistory.builder()
                .isDeleted(false)
                .user(userWithPermission)
                .build();

        //when
        UserForbiddenException userForbiddenException = assertThrows(UserForbiddenException.class,
                () -> target.checkPermission(userWithoutPermission.getId()));

        //then
        assertThat(userForbiddenException.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("유저가 검색 권한에 대한 권한이 있다면 아무 동작을 하지 않는다.")
    public void checkPermission_AuthorizedUser_DoesNotThrow() {
        //given
        User userWithPermission = User.builder()
                .id(1L)
                .build();

        SearchHistory target = SearchHistory.builder()
                .isDeleted(false)
                .user(userWithPermission)
                .build();

        //when & then
        assertThatCode(() -> target.checkPermission(userWithPermission.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("검색 기록은 삭제할 수 있다면 아무 동작을 하지 않는다.")
    public void checkDeletable_isDeletedFalse_DoesNotThrow() {
        //given
        User user = User.builder()
                .id(1L)
                .build();

        SearchHistory target = SearchHistory.builder()
                .isDeleted(false)
                .user(user)
                .build();

        //when & then
        assertThatCode(target::checkDeletable).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("검색 기록은 이미 삭제되었다면 삭제 시 Bad Request 예외를 던진다.")
    public void checkDeletable_isDeletedTrue_ThrowsBadRequestException() {
        //given
        User user = User.builder()
                .id(1L)
                .build();

        SearchHistory target = SearchHistory.builder()
                .isDeleted(true)
                .user(user)
                .build();

        //when
        SearchHistoryBadRequestException badRequestException = assertThrows(SearchHistoryBadRequestException.class,
                target::checkDeletable);

        //then
        assertThat(badRequestException.getErrorCode()).isEqualTo(ErrorCode.SEARCH_HISTORY_ALREADY_DELETED_BAD_REQUEST);
    }

}
