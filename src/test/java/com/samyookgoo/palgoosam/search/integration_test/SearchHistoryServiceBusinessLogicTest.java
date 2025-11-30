package com.samyookgoo.palgoosam.search.integration_test;

import com.samyookgoo.palgoosam.global.exception.ErrorCode;
import com.samyookgoo.palgoosam.search.domain.SearchHistory;
import com.samyookgoo.palgoosam.search.dto.SearchHistoryCreateRequestDto;
import com.samyookgoo.palgoosam.search.dto.SearchHistoryResponseDto;
import com.samyookgoo.palgoosam.search.exception.SearchHistoryNotFoundException;
import com.samyookgoo.palgoosam.search.repository.SearchHistoryRepository;
import com.samyookgoo.palgoosam.search.service.SearchHistoryService;
import com.samyookgoo.palgoosam.user.domain.User;
import com.samyookgoo.palgoosam.user.exception.UserForbiddenException;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestEntityManager
@Transactional
@DisplayName("SearchHistory 비즈니스 로직 테스트")
class SearchHistoryServiceBusinessLogicTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private SearchHistoryService searchHistoryService;

    private User tester;

    @BeforeEach
    void beforeEach() {
        tester = createUser("test@test.com", "tester");
    }

    @Test
    @DisplayName("검색했던 단어를 검색하면 검색어 순위가 올라간다.")
    public void recordUserSearch_ExistingKeywords_IncrementsSearchCount() {
        //given
        String keyword1 = "test1";
        SearchHistory searchHistory1 = createSearchHistory(keyword1, tester);

        String keyword2 = "test2";
        SearchHistory searchHistory2 = createSearchHistory(keyword2, tester);

        String keyword3 = "test3";
        SearchHistory searchHistory3 = createSearchHistory(keyword3, tester);

        List<SearchHistory> searchHistoryList = List.of(searchHistory1, searchHistory2, searchHistory3);
        List<SearchHistory> searchHistories = new ArrayList<>(searchHistoryList);
        searchHistoryRepository.saveAll(searchHistories);

        //when
        searchHistoryService.recordUserSearch(createSearchHistoryCreateRequestDto(keyword1), tester);
        searchHistoryService.recordUserSearch(createSearchHistoryCreateRequestDto(keyword2), tester);

        //then
        SearchHistory updatedSearchHistory1 = searchHistoryRepository.findByKeywordAndUserId(keyword1, tester.getId())
                .orElseThrow();
        Assertions.assertThat(updatedSearchHistory1.getKeyword()).isEqualTo(keyword1);
        Assertions.assertThat(updatedSearchHistory1.getSearchCount()).isEqualTo(2L);
        Assertions.assertThat(updatedSearchHistory1.getIsDeleted()).isFalse();

        SearchHistory updatedSearchHistory2 = searchHistoryRepository.findByKeywordAndUserId(keyword2, tester.getId())
                .orElseThrow();
        Assertions.assertThat(updatedSearchHistory2.getKeyword()).isEqualTo(keyword2);
        Assertions.assertThat(updatedSearchHistory2.getSearchCount()).isEqualTo(2L);
        Assertions.assertThat(updatedSearchHistory2.getIsDeleted()).isFalse();

        SearchHistory notUpdatedSearchHistory = searchHistoryRepository.findByKeywordAndUserId(keyword3, tester.getId())
                .orElseThrow();
        Assertions.assertThat(notUpdatedSearchHistory.getKeyword()).isEqualTo(keyword3);
        Assertions.assertThat(notUpdatedSearchHistory.getSearchCount()).isEqualTo(1L);
        Assertions.assertThat(notUpdatedSearchHistory.getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("새로운 단어를 검색하면 새로운 검색 기록이 저장된다.")
    public void recordUserSearch_NewKeyword_CreatesNewRecord() {
        //given
        String keyword = "test";
        SearchHistoryCreateRequestDto requestDto = createSearchHistoryCreateRequestDto(keyword);

        //when

        searchHistoryService.recordUserSearch(requestDto, tester);

        //then
        SearchHistory created = searchHistoryRepository.findByKeywordAndUserId(keyword, tester.getId())
                .orElseThrow();
        Assertions.assertThat(created.getKeyword()).isEqualTo(keyword);
        Assertions.assertThat(created.getSearchCount()).isEqualTo(1L);
        Assertions.assertThat(created.getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("사용자는 삭제되지 않은 검색 기록을 조회할 수 있다.")
    public void getSearchHistory_ActiveRecords_ReturnsInReverseOrder() {
        //given
        String keyword1 = "test1";
        SearchHistory searchHistory1 = createSearchHistory(keyword1, tester);

        String keyword2 = "test2";
        SearchHistory searchHistory2 = createSearchHistory(keyword2, tester);

        String keyword3 = "test3";
        SearchHistory searchHistory3 = createSearchHistory(keyword3, tester);

        List<SearchHistory> searchHistoryList = List.of(searchHistory1, searchHistory2, searchHistory3);
        List<SearchHistory> searchHistories = new ArrayList<>(searchHistoryList);
        saveInOrder(searchHistories);

        //when
        List<SearchHistoryResponseDto> result = searchHistoryService.getSearchHistory(tester);

        //then
        Assertions.assertThat(result).extracting("keyword")
                .containsExactly(keyword3, keyword2, keyword1);
    }


    @Test
    @DisplayName("검색 기록이 10개 일 때 조회")
    public void getSearchHistory_ExactlyTenRecords_ReturnsAll() {
        //given
        List<SearchHistory> searchHistories = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String keyword = "test" + i;
            searchHistories.add(createSearchHistory(keyword, tester));

        }
        searchHistoryRepository.saveAll(searchHistories);

        //when
        List<SearchHistoryResponseDto> result = searchHistoryService.getSearchHistory(tester);

        //then
        Assertions.assertThat(result).hasSize(10);
    }

    @Test
    @DisplayName("검색 기록이 11개일 때 조회")
    public void getSearchHistory_OverTenRecords_ReturnsLatestTen() {
        //given
        List<SearchHistory> searchHistories = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            String keyword = "test" + i;
            searchHistories.add(createSearchHistory(keyword, tester));

        }
        searchHistoryRepository.saveAll(searchHistories);

        //when
        List<SearchHistoryResponseDto> result = searchHistoryService.getSearchHistory(tester);

        //then
        Assertions.assertThat(result).hasSize(10);
        Assertions.assertThat(result).extracting("keyword")
                .containsExactly("test10", "test9", "test8", "test7", "test6", "test5", "test4", "test3", "test2",
                        "test1");
    }

    @Test
    @DisplayName("사용자는 검색 기록을 삭제할 수 있다.")
    public void deleteSearchHistory_ValidUser_SoftDeletesRecord() {
        //given
        String keyword1 = "test";
        SearchHistory searchHistory = createSearchHistory(keyword1, tester);
        SearchHistory saved = searchHistoryRepository.save(searchHistory);

        //when
        searchHistoryService.deleteSearchHistory(saved.getId(), tester);

        //then
        SearchHistory deleted = searchHistoryRepository.findById(saved.getId()).orElseThrow();
        Assertions.assertThat(deleted.getKeyword()).isEqualTo(saved.getKeyword());
        Assertions.assertThat(deleted.getSearchCount()).isEqualTo(saved.getSearchCount());
        Assertions.assertThat(deleted.getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("다른 사용자의 검색 기록을 삭제할 수 없다.")
    public void deleteSearchHistory_UnauthorizedUser_ThrowsForbiddenException() {
        //given
        String keyword1 = "test";
        SearchHistory searchHistory = createSearchHistory(keyword1, tester);
        SearchHistory saved = searchHistoryRepository.save(searchHistory);

        User unAuthorizedUser = createUser("unAuth@test.com", "unAuth");

        //when
        UserForbiddenException userForbiddenException = org.junit.jupiter.api.Assertions.assertThrows(
                UserForbiddenException.class,
                () -> searchHistoryService.deleteSearchHistory(saved.getId(), unAuthorizedUser));

        //then
        Assertions.assertThat(userForbiddenException.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("사용자는 존재하지 않는 검색 기록을 삭제할 수 없다.")
    public void deleteSearchHistory_NonExistentRecord_ThrowsNotFoundException() {
        //given

        //when
        SearchHistoryNotFoundException searchHistoryNotFoundException = org.junit.jupiter.api.Assertions.assertThrows(
                SearchHistoryNotFoundException.class,
                () -> searchHistoryService.deleteSearchHistory(1L, tester));

        //then
        Assertions.assertThat(searchHistoryNotFoundException.getErrorCode())
                .isEqualTo(ErrorCode.SEARCH_HISTORY_NOT_FOUND);

    }

    @Test
    @DisplayName("사용자의 검색 기록이 없으면 아무것도 확인할 수 없다.")
    public void getSearchHistory_NoRecords_ReturnsEmptyList() {
        //given

        //when
        List<SearchHistoryResponseDto> result = searchHistoryService.getSearchHistory(tester);

        //then
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사용자는 논리 삭제된 검색 기록을 확인할 수 없다.")
    public void getSearchHistory_WithDeletedRecords_ExcludesDeletedOnes() {
        //given
        String keyword1 = "saved1";
        SearchHistory searchHistory1 = createSearchHistory(keyword1, tester);

        String keyword2 = "deleted";
        SearchHistory searchHistory2 = createSearchHistory(keyword2, tester);

        String keyword3 = "saved2";
        SearchHistory searchHistory3 = createSearchHistory(keyword3, tester);

        List<SearchHistory> searchHistoryList = List.of(searchHistory1, searchHistory2, searchHistory3);
        List<SearchHistory> searchHistories = new ArrayList<>(searchHistoryList);
        saveInOrder(searchHistories);

        searchHistoryRepository.delete(searchHistory3);

        //when
        List<SearchHistoryResponseDto> result = searchHistoryService.getSearchHistory(tester);

        //then
        Assertions.assertThat(result).extracting("keyword")
                .containsExactly(keyword2, keyword1);

    }

    // 헬퍼 함수
    private User createUser(String email, String name) {
        User user = User.builder()
                .email(email)
                .name(name)
                .profileImage(email + name)
                .providerId(name)
                .provider("LOCAL")
                .build();
        return entityManager.persistAndFlush(user);
    }

    private SearchHistory createSearchHistory(String keyword, User user) {
        return SearchHistory.builder()
                .keyword(keyword)
                .user(user)
                .searchCount(1L)
                .isDeleted(false)
                .build();
    }

    private SearchHistoryCreateRequestDto createSearchHistoryCreateRequestDto(String keyword) {
        return new SearchHistoryCreateRequestDto(keyword);
    }

    @Transactional
    public void saveInOrder(List<SearchHistory> entities) {
        entities.forEach(entity -> {
            searchHistoryRepository.save(entity);
            entityManager.flush();
        });
    }
}
