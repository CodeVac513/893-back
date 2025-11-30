package com.samyookgoo.palgoosam.search.unit_test;

import static org.assertj.core.api.Assertions.assertThat;

import com.samyookgoo.palgoosam.global.exception.ErrorCode;
import com.samyookgoo.palgoosam.search.exception.SearchHistoryBadRequestException;
import com.samyookgoo.palgoosam.search.repository.SearchHistoryRepository;
import com.samyookgoo.palgoosam.search.service.SearchHistoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchHistoryService 유닛 테스트")
class SearchHistoryServiceTest {

    @Mock
    private SearchHistoryRepository searchHistoryRepository;

    @InjectMocks
    private SearchHistoryService searchHistoryService;

    // validateKeyword 유닛 테스트
    @Test
    @DisplayName("keyword가 비어있을 때 예외를 던진다.")
    public void validateKeyword_KeywordIsEmpty_ThrowBadRequestException() {
        //given
        String emptyKeyword = "";

        //when
        SearchHistoryBadRequestException exceptionWithEmptyKeyword = Assertions.assertThrows(
                SearchHistoryBadRequestException.class, () -> searchHistoryService.validateKeyword(emptyKeyword));

        //then
        assertThat(exceptionWithEmptyKeyword.getErrorCode()).isEqualTo(ErrorCode.SEARCH_HISTORY_BLANK_BAD_REQUEST);
    }

    @Test
    @DisplayName("keyword가 Null일 때 예외를 던진다.")
    public void validateKeyword_KeywordIsNull_ThrowBadRequestException() {
        //given
        String nullKeyword = null;

        //when
        SearchHistoryBadRequestException exceptionWithNullKeyword = Assertions.assertThrows(
                SearchHistoryBadRequestException.class, () -> searchHistoryService.validateKeyword(nullKeyword));

        //then
        assertThat(exceptionWithNullKeyword.getErrorCode()).isEqualTo(ErrorCode.SEARCH_HISTORY_BLANK_BAD_REQUEST);
    }

    @Test
    @DisplayName("keyword가 공백일 때 예외를 던진다.")
    public void validateKeyword_WithWhiteSpaceOnly_ThrowBadRequestException() {
        //given
        String whiteSpaceKeyword1 = " ";
        String whiteSpaceKeyword2 = "\t";
        String whiteSpaceKeyword3 = "\r";

        //when
        SearchHistoryBadRequestException exceptionWithWhiteSpaceKeyword1 = Assertions.assertThrows(
                SearchHistoryBadRequestException.class, () -> searchHistoryService.validateKeyword(whiteSpaceKeyword1));

        SearchHistoryBadRequestException exceptionWithWhiteSpaceKeyword2 = Assertions.assertThrows(
                SearchHistoryBadRequestException.class, () -> searchHistoryService.validateKeyword(whiteSpaceKeyword2));

        SearchHistoryBadRequestException exceptionWithWhiteSpaceKeyword3 = Assertions.assertThrows(
                SearchHistoryBadRequestException.class, () -> searchHistoryService.validateKeyword(whiteSpaceKeyword3));

        //then
        assertThat(exceptionWithWhiteSpaceKeyword1.getErrorCode()).isEqualTo(
                ErrorCode.SEARCH_HISTORY_BLANK_BAD_REQUEST);
        assertThat(exceptionWithWhiteSpaceKeyword2.getErrorCode()).isEqualTo(
                ErrorCode.SEARCH_HISTORY_BLANK_BAD_REQUEST);
        assertThat(exceptionWithWhiteSpaceKeyword3.getErrorCode()).isEqualTo(
                ErrorCode.SEARCH_HISTORY_BLANK_BAD_REQUEST);
    }
}