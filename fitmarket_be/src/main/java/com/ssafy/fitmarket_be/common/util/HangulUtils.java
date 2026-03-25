package com.ssafy.fitmarket_be.common.util;

/**
 * 한글 유니코드 자모 분해/초성 추출 유틸리티.
 * 외부 라이브러리 없이 Java 유니코드 연산만 사용한다.
 */
public final class HangulUtils {

    private HangulUtils() {
    }

    private static final char[] CHOSUNG = {
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
            'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    private static final char[] JUNGSEONG = {
            'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
            'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    };

    private static final char[] JONGSEONG = {
            '\0',
            'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ',
            'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ',
            'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    private static final int HANGUL_BASE = 0xAC00;
    private static final int JUNGSEONG_COUNT = 21;
    private static final int JONGSEONG_COUNT = 28;

    /**
     * 한글 문자열을 자모 단위로 분해한다.
     * 비한글 문자(영문, 숫자, 공백 등)는 그대로 통과한다.
     *
     * @param text 분해할 문자열
     * @return 자모 분해된 문자열 (null 입력 시 빈 문자열)
     */
    public static String decompose(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder(text.length() * 3);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isHangulSyllable(c)) {
                int code = c - HANGUL_BASE;
                int chosungIdx = code / (JUNGSEONG_COUNT * JONGSEONG_COUNT);
                int jungseongIdx = (code % (JUNGSEONG_COUNT * JONGSEONG_COUNT)) / JONGSEONG_COUNT;
                int jongseongIdx = code % JONGSEONG_COUNT;

                sb.append(CHOSUNG[chosungIdx]);
                sb.append(JUNGSEONG[jungseongIdx]);
                if (jongseongIdx != 0) {
                    sb.append(JONGSEONG[jongseongIdx]);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 한글 문자열에서 초성만 추출한다.
     * 비한글 문자는 그대로 통과한다.
     *
     * @param text 초성을 추출할 문자열
     * @return 초성만 포함된 문자열 (null 입력 시 빈 문자열)
     */
    public static String extractChosung(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isHangulSyllable(c)) {
                int chosungIdx = (c - HANGUL_BASE) / (JUNGSEONG_COUNT * JONGSEONG_COUNT);
                sb.append(CHOSUNG[chosungIdx]);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 문자가 한글 완성형 음절인지 판별한다.
     */
    public static boolean isHangulSyllable(char c) {
        return c >= HANGUL_BASE && c <= 0xD7A3;
    }
}
