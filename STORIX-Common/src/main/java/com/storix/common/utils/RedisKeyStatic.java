package com.storix.common.utils;

public final class RedisKeyStatic {

    private RedisKeyStatic() {
    }

    // @RedisHash 엔티티 키스페이스
    public static final class Hash {
        public static final String REFRESH_TOKEN = "refreshToken";
        public static final String ONBOARDING_TOKEN = "onboardingToken";
        public static final String ADMIN_SIGNUP_PENDING = "adminSignupPending";
        public static final String TESTER_SIGNUP_PENDING = "testerSignupPending";
        public static final String USER_BLACKLIST = "userBlacklist";

        private Hash() {
        }
    }

    public static final class Search {
        public static final String TRENDING_PREFIX = "search:trending:";
        public static final String TRENDING_AGGREGATED = "search:trending:aggregated";
        public static final String TRENDING_PREV_AGGREGATED = "search:trending:aggregated:prev";
        public static final String RECENT_PREFIX = "search:recent:";

        private Search() {
        }
    }

    public static final class Event {
        public static final String ACTIVE_POPUP = "event::activePopup::v1";
        public static final String ACTIVE_BANNER = "event::activeBanner::v1";
        public static final String PENDING_APP_EVENTS_PREFIX = "event::pendingAppEvents::v1::";

        private Event() {
        }
    }

    // 선정 알림 dedup — {prefix}{yyyyMMdd}:{id}
    public static final class Notification {
        public static final String FEATURED_FEED_PREFIX = "featured:feed:";
        public static final String FEATURED_TOPIC_ROOM_PREFIX = "featured:topicroom:";

        private Notification() {
        }
    }

    public static final class Library {
        public static final String RECENT_PREFIX = "library:recent:";

        private Library() {
        }
    }

    public static final class Hashtag {
        public static final String TOTAL_WORKS = "hashtag:meta:total_works";
        public static final String DF = "hashtag:df";

        private Hashtag() {
        }
    }

    public static final class Image {
        public static final String BOARD_PREFIX = "image:public:board:";
        public static final String PROFILE_PREFIX = "image:public:profile:";

        private Image() {
        }
    }

    public static final class Onboarding {
        public static final String WORKS_LIST = "onboarding::onboardingWorksList::v1";

        private Onboarding() {
        }
    }

    public static final class Exploration {
        public static final String CHART_PREFIX = "exploration::chart::total::";
        public static final String DONE_PREFIX = "exploration::done::today::";
        public static final String PENDING_PREFIX = "exploration::pending::detail::";
        public static final String GLOBAL_QUEUE = "exploration::queue";
        public static final String DAILY_COUNT_PREFIX = "exploration::count::today::";

        private Exploration() {
        }
    }

    // pub/sub 채널
    public static final class Channel {
        public static final String CHAT_ROOM_PREFIX = "room:";
        public static final String TOPIC_ROOM_ACTIVE_USERS_PREFIX = "topic-room:active-users:";

        private Channel() {
        }
    }
}
