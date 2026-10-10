package com.storix.domain.domains.works.domain;

import com.storix.domain.domains.search.helper.HangulTextHelper;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class WorksIdentity {

    private static final Pattern BRACKET = Pattern.compile("[\\[(【<]([^\\])】>]*)[\\])】>]");
    private static final Set<String> LABELS = Set.of("bl", "gl", "독점", "완결", "연재", "단독", "휴재", "선공개", "외전");
    private static final Pattern TRAILING_SIDE_STORY = Pattern.compile("(?<=\\S)\\s+외전\\s*$");
    private static final String BOOK_LABEL = "단행본";
    private static final Pattern NAME_SEPARATOR = Pattern.compile("[,/&·]");
    private static final Pattern PARENTHESIS = Pattern.compile("\\([^)]*\\)");
    private static final Pattern ROLE_SUFFIX = Pattern.compile("[∙·]\\s*(글|그림|원작|각색|작화)\\s*(?=[,/&]|$)");

    private WorksIdentity() {
    }

    public static String titleKey(String worksName) {
        return HangulTextHelper.normalize(stripLabels(worksName, false));
    }

    // 단행본 표기 뺀 제목
    public static String bookBaseKey(String worksName) {
        return HangulTextHelper.normalize(stripLabels(worksName, true));
    }

    public static boolean isBookEdition(String worksName, WorksType worksType) {
        if (worksType == WorksType.BOOK) return true;
        Matcher matcher = BRACKET.matcher(worksName == null ? "" : worksName);
        while (matcher.find()) {
            if (BOOK_LABEL.equals(HangulTextHelper.normalize(matcher.group(1)))) return true;
        }
        return false;
    }

    public static Set<String> artistNames(String... names) {
        return Arrays.stream(names)
                .filter(Objects::nonNull)
                .map(name -> ROLE_SUFFIX.matcher(PARENTHESIS.matcher(name).replaceAll("")).replaceAll(""))
                .flatMap(NAME_SEPARATOR::splitAsStream)
                .map(HangulTextHelper::normalize)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static boolean sharesArtist(Set<String> artists, Works works) {
        Set<String> existing = artistNames(works.getArtistName(), works.getAuthor(), works.getIllustrator(), works.getOriginalAuthor());
        return artists.stream().anyMatch(existing::contains);
    }

    // 카카오 중복 작가명 제거
    public static String dedupeArtistName(String artistName) {
        return Stream.of(artistName.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private static String stripLabels(String worksName, boolean stripBookLabel) {
        if (worksName == null) return "";
        String withoutSideStory = TRAILING_SIDE_STORY.matcher(worksName).replaceAll("");
        return BRACKET.matcher(withoutSideStory).replaceAll(match -> {
            String label = HangulTextHelper.normalize(match.group(1));
            boolean remove = LABELS.contains(label) || (stripBookLabel && BOOK_LABEL.equals(label));
            return remove ? "" : Matcher.quoteReplacement(match.group());
        });
    }
}
