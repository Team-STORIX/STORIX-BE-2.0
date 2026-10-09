package com.storix.domain.domains.search.helper;

public final class HangulTextHelper {

    private static final char SYLLABLE_START = '가';
    private static final char SYLLABLE_END = '힣';
    private static final int JUNG_COUNT = 21;
    private static final int JONG_COUNT = 28;

    private static final char[] CHO = {
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };
    private static final char[] JUNG = {
            'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    };
    private static final char[] JONG = {
            0, 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    private HangulTextHelper() {
    }

    // 띄어쓰기·특수문자 차이로 못 찾는 일이 없도록 글자와 숫자만 남긴다
    public static String normalize(String text) {
        if (text == null) return "";

        StringBuilder sb = new StringBuilder(text.length());
        text.codePoints()
                .filter(Character::isLetterOrDigit)
                .map(Character::toLowerCase)
                .forEach(sb::appendCodePoint);
        return sb.toString();
    }

    public static String chosung(String text) {
        String normalized = normalize(text);
        StringBuilder sb = new StringBuilder(normalized.length());
        for (char c : normalized.toCharArray()) {
            sb.append(isSyllable(c) ? CHO[(c - SYLLABLE_START) / (JUNG_COUNT * JONG_COUNT)] : c);
        }
        return sb.toString();
    }

    public static String jamo(String text) {
        String normalized = normalize(text);
        StringBuilder sb = new StringBuilder(normalized.length() * 3);
        for (char c : normalized.toCharArray()) {
            if (!isSyllable(c)) {
                sb.append(c);
                continue;
            }
            int offset = c - SYLLABLE_START;
            sb.append(CHO[offset / (JUNG_COUNT * JONG_COUNT)]);
            sb.append(JUNG[(offset % (JUNG_COUNT * JONG_COUNT)) / JONG_COUNT]);
            char jong = JONG[offset % JONG_COUNT];
            if (jong != 0) sb.append(jong);
        }
        return sb.toString();
    }

    public static boolean isChosungOnly(String text) {
        String normalized = normalize(text);
        if (normalized.isEmpty()) return false;

        for (char c : normalized.toCharArray()) {
            if (c < 'ㄱ' || c > 'ㅎ') return false;
        }
        return true;
    }

    private static boolean isSyllable(char c) {
        return c >= SYLLABLE_START && c <= SYLLABLE_END;
    }
}
