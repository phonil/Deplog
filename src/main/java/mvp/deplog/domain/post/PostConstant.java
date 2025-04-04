package mvp.deplog.domain.post;

public final class PostConstant {
    private PostConstant() {}

    public static final class AnonymousPostDetailConstant {
        private AnonymousPostDetailConstant() {}
        public static final String POST_COOKIE_NAME_PREFIX = "viewed_post";
        public static final String POST_COOKIE_VALUE = "true";
        public static final String COOKIE_PATH = "/";
        public static final int COOKIE_AGE = 60 * 60 * 2;
    }

    public static final class MemberPostDetailConstant {
        private MemberPostDetailConstant() {}
        public static final String REDIS_KEY_MEMBER_PREFIX = "viewed_member:";
        public static final String REDIS_KEY_POST_PREFIX = "_post:";
        public static final String POST_REDIS_VALUE = "true";
        public static final int REDIS_DURATION = 60 * 60 * 2;
    }
}
