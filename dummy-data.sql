START TRANSACTION;

-- Admin user management dummy data.
-- Fixed seed id ranges:
-- users: 900001 ~ 900005
-- works: 910001 ~ 910003
-- topic_room: 920001 ~ 920003
-- reader_board: 930001 ~ 930015
-- reader_board_reply: 940001 ~ 940015
-- review: 950001 ~ 950015
-- report_case: 960001 ~ 960004
-- chat_message: 970001 ~ 970015

-- Cleanup only this seed data range so the file can be rerun in local/dev DB.
DELETE FROM feed_reply_report WHERE report_case_id BETWEEN 960001 AND 960004;
DELETE FROM topic_room_report WHERE report_case_id BETWEEN 960001 AND 960004;
DELETE FROM review_report WHERE report_case_id BETWEEN 960001 AND 960004;
DELETE FROM feed_report WHERE report_case_id BETWEEN 960001 AND 960004;
DELETE FROM report_case WHERE report_case_id BETWEEN 960001 AND 960004;

DELETE FROM chat_message WHERE id BETWEEN 970001 AND 970015;
DELETE FROM topic_room_user WHERE topic_room_id BETWEEN 920001 AND 920003;
DELETE FROM topic_room WHERE topic_room_id BETWEEN 920001 AND 920003;

DELETE FROM reader_board_reply WHERE id BETWEEN 940001 AND 940015;
DELETE FROM reader_board WHERE reader_board_id BETWEEN 930001 AND 930015;
DELETE FROM review WHERE review_id BETWEEN 950001 AND 950015;

DELETE FROM user_favorite_works WHERE user_id BETWEEN 900001 AND 900005 OR works_id BETWEEN 910001 AND 910003;
DELETE FROM works_platform WHERE works_id BETWEEN 910001 AND 910003;
DELETE FROM works WHERE works_id BETWEEN 910001 AND 910003;

DELETE FROM user_favorite_genre WHERE user_id BETWEEN 900001 AND 900005;
DELETE FROM libraries WHERE user_id BETWEEN 900001 AND 900005;
DELETE FROM users WHERE user_id BETWEEN 900001 AND 900005;

-- 1. Users: first user is SUPER_ADMIN, others are READER.
INSERT INTO users (
    user_id,
    nick_name,
    profile_description,
    point,
    age_over_14,
    is_adult_verified,
    account_state,
    last_login_at,
    role,
    password,
    oauth_provider,
    oauth_oid,
    oauth_refresh_token,
    oauth_email,
    created_at,
    updated_at
) VALUES
    (900001, '슈퍼관리', '더미 슈퍼 관리자', 0, true, true, 'NORMAL', NOW(), 'SUPER_ADMIN', '$2a$10$uMlgV3xkf2pN9gAGJ09pcORlRGdmKPnxYYVWlBeFUiHmUWuoPpIYC', 'SLACK', 'dummy-super-001', NULL, 'super-admin@storix.test', NOW(), NOW()),
    (900002, '유저002', '더미 일반 유저 2', 0, true, true, 'NORMAL', NOW(), 'READER', NULL, 'KAKAO', 'dummy-reader-002', NULL, 'user002@storix.test', NOW(), NOW()),
    (900003, '유저003', '더미 일반 유저 3', 0, true, true, 'NORMAL', NOW(), 'READER', NULL, 'KAKAO', 'dummy-reader-003', NULL, 'user003@storix.test', NOW(), NOW()),
    (900004, '유저004', '더미 일반 유저 4', 0, true, true, 'NORMAL', NOW(), 'READER', NULL, 'KAKAO', 'dummy-reader-004', NULL, 'user004@storix.test', NOW(), NOW()),
    (900005, '유저005', '더미 일반 유저 5', 0, true, true, 'NORMAL', NOW(), 'READER', NULL, 'KAKAO', 'dummy-reader-005', NULL, 'user005@storix.test', NOW(), NOW());

INSERT INTO user_favorite_genre (user_id, genre)
VALUES
    (900001, 'FANTASY'), (900001, 'ROMANCE'),
    (900002, 'FANTASY'), (900002, 'ROMANCE'),
    (900003, 'FANTASY'), (900003, 'ROMANCE'),
    (900004, 'FANTASY'), (900004, 'ROMANCE'),
    (900005, 'FANTASY'), (900005, 'ROMANCE');

INSERT INTO libraries (user_id, review_count, board_count)
VALUES
    (900001, 3, 3),
    (900002, 3, 3),
    (900003, 3, 3),
    (900004, 3, 3),
    (900005, 3, 3);

-- 2. Works: 3 works.
INSERT INTO works (
    works_id,
    works_name,
    artist_name,
    author,
    illustrator,
    original_author,
    age_classification,
    description,
    genre,
    thumbnail_url,
    reviews_count,
    avg_rating,
    works_type,
    is_onboarding
) VALUES
    (910001, '더미 작품 1', '더미 작가 1', '더미 글작가 1', '더미 그림작가 1', NULL, '15세 이용가', '관리자 유저 관리 테스트용 더미 작품 1입니다.', '판타지', 'https://example.com/dummy-works-1.jpg', 5, 4.0, '웹툰', false),
    (910002, '더미 작품 2', '더미 작가 2', '더미 글작가 2', '더미 그림작가 2', NULL, '15세 이용가', '관리자 유저 관리 테스트용 더미 작품 2입니다.', '로맨스', 'https://example.com/dummy-works-2.jpg', 5, 4.0, '웹툰', false),
    (910003, '더미 작품 3', '더미 작가 3', '더미 글작가 3', '더미 그림작가 3', NULL, '15세 이용가', '관리자 유저 관리 테스트용 더미 작품 3입니다.', '액션', 'https://example.com/dummy-works-3.jpg', 5, 4.0, '웹툰', false);

INSERT INTO works_platform (id, works_id, platform)
VALUES
    (911001, 910001, '네이버 웹툰'),
    (911002, 910002, '네이버 웹툰'),
    (911003, 910003, '네이버 웹툰');

INSERT INTO user_favorite_works (favorite_works_id, user_id, works_id, created_at, updated_at)
VALUES
    (912001, 900001, 910001, NOW(), NOW()),
    (912002, 900001, 910002, NOW(), NOW()),
    (912003, 900001, 910003, NOW(), NOW()),
    (912004, 900002, 910001, NOW(), NOW()),
    (912005, 900002, 910002, NOW(), NOW()),
    (912006, 900002, 910003, NOW(), NOW()),
    (912007, 900003, 910001, NOW(), NOW()),
    (912008, 900003, 910002, NOW(), NOW()),
    (912009, 900003, 910003, NOW(), NOW()),
    (912010, 900004, 910001, NOW(), NOW()),
    (912011, 900004, 910002, NOW(), NOW()),
    (912012, 900004, 910003, NOW(), NOW()),
    (912013, 900005, 910001, NOW(), NOW()),
    (912014, 900005, 910002, NOW(), NOW()),
    (912015, 900005, 910003, NOW(), NOW());

-- 3. Reviews: every user reviews every work.
INSERT INTO review (
    review_id,
    library_user_id,
    works_id,
    is_spoiler,
    spoiler_script,
    rating,
    content,
    like_count,
    deleted,
    deleted_by,
    deleted_at,
    created_at,
    updated_at
) VALUES
    (950001, 900001, 910001, false, NULL, 5.0, '유저 1이 더미 작품 1에 작성한 리뷰입니다.', 1, false, NULL, NULL, NOW(), NOW()),
    (950002, 900001, 910002, false, NULL, 5.0, '유저 1이 더미 작품 2에 작성한 리뷰입니다.', 1, false, NULL, NULL, NOW(), NOW()),
    (950003, 900001, 910003, false, NULL, 5.0, '유저 1이 더미 작품 3에 작성한 리뷰입니다.', 1, false, NULL, NULL, NOW(), NOW()),
    (950004, 900002, 910001, false, NULL, 4.5, '유저 2가 더미 작품 1에 작성한 리뷰입니다.', 2, false, NULL, NULL, NOW(), NOW()),
    (950005, 900002, 910002, false, NULL, 4.5, '유저 2가 더미 작품 2에 작성한 리뷰입니다.', 2, false, NULL, NULL, NOW(), NOW()),
    (950006, 900002, 910003, false, NULL, 4.5, '유저 2가 더미 작품 3에 작성한 리뷰입니다.', 2, false, NULL, NULL, NOW(), NOW()),
    (950007, 900003, 910001, false, NULL, 4.0, '유저 3이 더미 작품 1에 작성한 리뷰입니다.', 3, false, NULL, NULL, NOW(), NOW()),
    (950008, 900003, 910002, false, NULL, 4.0, '유저 3이 더미 작품 2에 작성한 리뷰입니다.', 3, false, NULL, NULL, NOW(), NOW()),
    (950009, 900003, 910003, false, NULL, 4.0, '유저 3이 더미 작품 3에 작성한 리뷰입니다.', 3, false, NULL, NULL, NOW(), NOW()),
    (950010, 900004, 910001, false, NULL, 3.5, '유저 4가 더미 작품 1에 작성한 리뷰입니다.', 4, false, NULL, NULL, NOW(), NOW()),
    (950011, 900004, 910002, false, NULL, 3.5, '유저 4가 더미 작품 2에 작성한 리뷰입니다.', 4, false, NULL, NULL, NOW(), NOW()),
    (950012, 900004, 910003, false, NULL, 3.5, '유저 4가 더미 작품 3에 작성한 리뷰입니다.', 4, false, NULL, NULL, NOW(), NOW()),
    (950013, 900005, 910001, false, NULL, 3.0, '유저 5가 더미 작품 1에 작성한 리뷰입니다.', 5, false, NULL, NULL, NOW(), NOW()),
    (950014, 900005, 910002, false, NULL, 3.0, '유저 5가 더미 작품 2에 작성한 리뷰입니다.', 5, false, NULL, NULL, NOW(), NOW()),
    (950015, 900005, 910003, false, NULL, 3.0, '유저 5가 더미 작품 3에 작성한 리뷰입니다.', 5, false, NULL, NULL, NOW(), NOW());

-- 4. Boards: every user writes one board for every work.
INSERT INTO reader_board (
    reader_board_id,
    user_id,
    is_works_selected,
    works_id,
    content,
    like_count,
    reply_count,
    deleted,
    deleted_by,
    deleted_at,
    is_spoiler,
    spoiler_script,
    theme,
    popularity_score,
    created_at,
    updated_at
) VALUES
    (930001, 900001, true, 910001, '유저 1이 더미 작품 1에 작성한 게시글입니다.', 1, 5, false, NULL, NULL, false, NULL, 'BIRTHDAY', 0, NOW(), NOW()),
    (930002, 900001, true, 910002, '유저 1이 더미 작품 2에 작성한 게시글입니다.', 1, 5, false, NULL, NULL, false, NULL, 'BIRTHDAY', 0, NOW(), NOW()),
    (930003, 900001, true, 910003, '유저 1이 더미 작품 3에 작성한 게시글입니다.', 1, 5, false, NULL, NULL, false, NULL, 'BIRTHDAY', 0, NOW(), NOW()),
    (930004, 900002, true, 910001, '유저 2가 더미 작품 1에 작성한 게시글입니다.', 2, 0, false, NULL, NULL, false, NULL, 'BIRTHDAY', 0, NOW(), NOW()),
    (930005, 900002, true, 910002, '유저 2가 더미 작품 2에 작성한 게시글입니다.', 2, 0, false, NULL, NULL, false, NULL, 'BIRTHDAY', 0, NOW(), NOW()),
    (930006, 900002, true, 910003, '유저 2가 더미 작품 3에 작성한 게시글입니다.', 2, 0, false, NULL, NULL, false, NULL, 'BIRTHDAY', 0, NOW(), NOW()),
    (930007, 900003, true, 910001, '유저 3이 더미 작품 1에 작성한 게시글입니다.', 3, 0, false, NULL, NULL, false, NULL, 'BIRTHDAY', 0, NOW(), NOW()),
    (930008, 900003, true, 910002, '유저 3이 더미 작품 2에 작성한 게시글입니다.', 3, 0, false, NULL, NULL, false, NULL, 'BIRTHDAY', 0, NOW(), NOW()),
    (930009, 900003, true, 910003, '유저 3이 더미 작품 3에 작성한 게시글입니다.', 3, 0, false, NULL, NULL, false, NULL, 'BIRTHDAY', 0, NOW(), NOW()),
    (930010, 900004, true, 910001, '유저 4가 더미 작품 1에 작성한 게시글입니다.', 4, 0, false, NULL, NULL, false, NULL, 'BIRTHDAY', 0, NOW(), NOW()),
    (930011, 900004, true, 910002, '유저 4가 더미 작품 2에 작성한 게시글입니다.', 4, 0, false, NULL, NULL, false, NULL, 'BIRTHDAY', 0, NOW(), NOW()),
    (930012, 900004, true, 910003, '유저 4가 더미 작품 3에 작성한 게시글입니다.', 4, 0, false, NULL, NULL, false, NULL, 'BIRTHDAY', 0, NOW(), NOW()),
    (930013, 900005, true, 910001, '유저 5가 더미 작품 1에 작성한 게시글입니다.', 5, 0, false, NULL, NULL, false, NULL, 'BIRTHDAY', 0, NOW(), NOW()),
    (930014, 900005, true, 910002, '유저 5가 더미 작품 2에 작성한 게시글입니다.', 5, 0, false, NULL, NULL, false, NULL, 'BIRTHDAY', 0, NOW(), NOW()),
    (930015, 900005, true, 910003, '유저 5가 더미 작품 3에 작성한 게시글입니다.', 5, 0, false, NULL, NULL, false, NULL, 'BIRTHDAY', 0, NOW(), NOW());

-- 5. Replies: every user comments on the super admin's representative board for every work.
INSERT INTO reader_board_reply (
    id,
    reader_board_id,
    parent_reply_id,
    user_id,
    comment,
    like_count,
    depth,
    child_reply_count,
    deleted,
    deleted_by,
    deleted_at,
    created_at,
    updated_at
) VALUES
    (940001, 930001, NULL, 900001, '유저 1이 더미 작품 1 대표 게시글에 작성한 댓글입니다.', 0, 0, 0, false, NULL, NULL, NOW(), NOW()),
    (940002, 930002, NULL, 900001, '유저 1이 더미 작품 2 대표 게시글에 작성한 댓글입니다.', 0, 0, 0, false, NULL, NULL, NOW(), NOW()),
    (940003, 930003, NULL, 900001, '유저 1이 더미 작품 3 대표 게시글에 작성한 댓글입니다.', 0, 0, 0, false, NULL, NULL, NOW(), NOW()),
    (940004, 930001, NULL, 900002, '유저 2가 더미 작품 1 대표 게시글에 작성한 댓글입니다.', 0, 0, 0, false, NULL, NULL, NOW(), NOW()),
    (940005, 930002, NULL, 900002, '유저 2가 더미 작품 2 대표 게시글에 작성한 댓글입니다.', 0, 0, 0, false, NULL, NULL, NOW(), NOW()),
    (940006, 930003, NULL, 900002, '유저 2가 더미 작품 3 대표 게시글에 작성한 댓글입니다.', 0, 0, 0, false, NULL, NULL, NOW(), NOW()),
    (940007, 930001, NULL, 900003, '유저 3이 더미 작품 1 대표 게시글에 작성한 댓글입니다.', 0, 0, 0, false, NULL, NULL, NOW(), NOW()),
    (940008, 930002, NULL, 900003, '유저 3이 더미 작품 2 대표 게시글에 작성한 댓글입니다.', 0, 0, 0, false, NULL, NULL, NOW(), NOW()),
    (940009, 930003, NULL, 900003, '유저 3이 더미 작품 3 대표 게시글에 작성한 댓글입니다.', 0, 0, 0, false, NULL, NULL, NOW(), NOW()),
    (940010, 930001, NULL, 900004, '유저 4가 더미 작품 1 대표 게시글에 작성한 댓글입니다.', 0, 0, 0, false, NULL, NULL, NOW(), NOW()),
    (940011, 930002, NULL, 900004, '유저 4가 더미 작품 2 대표 게시글에 작성한 댓글입니다.', 0, 0, 0, false, NULL, NULL, NOW(), NOW()),
    (940012, 930003, NULL, 900004, '유저 4가 더미 작품 3 대표 게시글에 작성한 댓글입니다.', 0, 0, 0, false, NULL, NULL, NOW(), NOW()),
    (940013, 930001, NULL, 900005, '유저 5가 더미 작품 1 대표 게시글에 작성한 댓글입니다.', 0, 0, 0, false, NULL, NULL, NOW(), NOW()),
    (940014, 930002, NULL, 900005, '유저 5가 더미 작품 2 대표 게시글에 작성한 댓글입니다.', 0, 0, 0, false, NULL, NULL, NOW(), NOW()),
    (940015, 930003, NULL, 900005, '유저 5가 더미 작품 3 대표 게시글에 작성한 댓글입니다.', 0, 0, 0, false, NULL, NULL, NOW(), NOW());

-- 6. Topic rooms: one room per work.
INSERT INTO topic_room (
    topic_room_id,
    topic_room_name,
    works_id,
    active_user_number,
    previous_active_user_number,
    last_chat_time,
    last_message,
    last_message_type,
    last_message_sender_id,
    popularity_score,
    popularity_growth_rate,
    created_at,
    updated_at
) VALUES
    (920001, '더미 작품 1 토픽룸', 910001, 5, 0, NOW(), '유저 5가 더미 작품 1 토픽룸에 작성한 채팅입니다.', 'TALK', 900005, 0.0, 0.0, NOW(), NOW()),
    (920002, '더미 작품 2 토픽룸', 910002, 5, 0, NOW(), '유저 5가 더미 작품 2 토픽룸에 작성한 채팅입니다.', 'TALK', 900005, 0.0, 0.0, NOW(), NOW()),
    (920003, '더미 작품 3 토픽룸', 910003, 5, 0, NOW(), '유저 5가 더미 작품 3 토픽룸에 작성한 채팅입니다.', 'TALK', 900005, 0.0, 0.0, NOW(), NOW());

-- 7. Topic room users: every user joins every topic room.
INSERT INTO topic_room_user (
    topic_room_user_id,
    topic_room_id,
    user_id,
    role,
    created_at,
    updated_at
) VALUES
    (921001, 920001, 900001, 'HOST', NOW(), NOW()),
    (921002, 920002, 900001, 'HOST', NOW(), NOW()),
    (921003, 920003, 900001, 'HOST', NOW(), NOW()),
    (921004, 920001, 900002, 'MEMBER', NOW(), NOW()),
    (921005, 920002, 900002, 'MEMBER', NOW(), NOW()),
    (921006, 920003, 900002, 'MEMBER', NOW(), NOW()),
    (921007, 920001, 900003, 'MEMBER', NOW(), NOW()),
    (921008, 920002, 900003, 'MEMBER', NOW(), NOW()),
    (921009, 920003, 900003, 'MEMBER', NOW(), NOW()),
    (921010, 920001, 900004, 'MEMBER', NOW(), NOW()),
    (921011, 920002, 900004, 'MEMBER', NOW(), NOW()),
    (921012, 920003, 900004, 'MEMBER', NOW(), NOW()),
    (921013, 920001, 900005, 'MEMBER', NOW(), NOW()),
    (921014, 920002, 900005, 'MEMBER', NOW(), NOW()),
    (921015, 920003, 900005, 'MEMBER', NOW(), NOW());

-- 8. Chat messages: every user chats in every topic room.
INSERT INTO chat_message (
    id,
    room_id,
    sender_id,
    message,
    message_type,
    deleted,
    deleted_at,
    created_at,
    updated_at
) VALUES
    (970001, 920001, 900001, '유저 1이 더미 작품 1 토픽룸에 작성한 채팅입니다.', 'TALK', false, NULL, NOW(), NOW()),
    (970002, 920002, 900001, '유저 1이 더미 작품 2 토픽룸에 작성한 채팅입니다.', 'TALK', false, NULL, NOW(), NOW()),
    (970003, 920003, 900001, '유저 1이 더미 작품 3 토픽룸에 작성한 채팅입니다.', 'TALK', false, NULL, NOW(), NOW()),
    (970004, 920001, 900002, '유저 2가 더미 작품 1 토픽룸에 작성한 채팅입니다.', 'TALK', false, NULL, NOW(), NOW()),
    (970005, 920002, 900002, '유저 2가 더미 작품 2 토픽룸에 작성한 채팅입니다.', 'TALK', false, NULL, NOW(), NOW()),
    (970006, 920003, 900002, '유저 2가 더미 작품 3 토픽룸에 작성한 채팅입니다.', 'TALK', false, NULL, NOW(), NOW()),
    (970007, 920001, 900003, '유저 3이 더미 작품 1 토픽룸에 작성한 채팅입니다.', 'TALK', false, NULL, NOW(), NOW()),
    (970008, 920002, 900003, '유저 3이 더미 작품 2 토픽룸에 작성한 채팅입니다.', 'TALK', false, NULL, NOW(), NOW()),
    (970009, 920003, 900003, '유저 3이 더미 작품 3 토픽룸에 작성한 채팅입니다.', 'TALK', false, NULL, NOW(), NOW()),
    (970010, 920001, 900004, '유저 4가 더미 작품 1 토픽룸에 작성한 채팅입니다.', 'TALK', false, NULL, NOW(), NOW()),
    (970011, 920002, 900004, '유저 4가 더미 작품 2 토픽룸에 작성한 채팅입니다.', 'TALK', false, NULL, NOW(), NOW()),
    (970012, 920003, 900004, '유저 4가 더미 작품 3 토픽룸에 작성한 채팅입니다.', 'TALK', false, NULL, NOW(), NOW()),
    (970013, 920001, 900005, '유저 5가 더미 작품 1 토픽룸에 작성한 채팅입니다.', 'TALK', false, NULL, NOW(), NOW()),
    (970014, 920002, 900005, '유저 5가 더미 작품 2 토픽룸에 작성한 채팅입니다.', 'TALK', false, NULL, NOW(), NOW()),
    (970015, 920003, 900005, '유저 5가 더미 작품 3 토픽룸에 작성한 채팅입니다.', 'TALK', false, NULL, NOW(), NOW());

-- 9. Reports: users 2, 3, 4, 5 each report a different user.
-- User 2 reports user 3's feed board.
INSERT INTO report_case (
    report_case_id,
    target_type,
    target_id,
    reported_user_id,
    status,
    processed_by_admin_id,
    process_memo,
    process_action,
    processed_at,
    created_at,
    updated_at
) VALUES (
    960001,
    'FEED',
    930007,
    900003,
    'RECEIVED',
    NULL,
    NULL,
    NULL,
    NULL,
    NOW(),
    NOW()
);

INSERT INTO feed_report (
    id,
    reporter_id,
    reported_user_id,
    board_id,
    report_case_id,
    created_at,
    updated_at
) VALUES (
    961001,
    900002,
    900003,
    930007,
    960001,
    NOW(),
    NOW()
);

-- User 3 reports user 4's review.
INSERT INTO report_case (
    report_case_id,
    target_type,
    target_id,
    reported_user_id,
    status,
    processed_by_admin_id,
    process_memo,
    process_action,
    processed_at,
    created_at,
    updated_at
) VALUES (
    960002,
    'REVIEW',
    950011,
    900004,
    'RECEIVED',
    NULL,
    NULL,
    NULL,
    NULL,
    NOW(),
    NOW()
);

INSERT INTO review_report (
    id,
    reporter_id,
    reported_user_id,
    review_id,
    ,
    other_reason,
    report_case_id,
    created_at,
    updated_at
) VALUES (
    962001,
    900003,
    900004,
    950011,
    'ABUSE',
    NULL,
    960002,
    NOW(),
    NOW()
);

-- User 4 reports user 5 in topic room.
INSERT INTO report_case (
    report_case_id,
    target_type,
    target_id,
    reported_user_id,
    status,
    processed_by_admin_id,
    process_memo,
    process_action,
    processed_at,
    created_at,
    updated_at
) VALUES (
    960003,
    'TOPIC_ROOM',
    920003,
    900005,
    'RECEIVED',
    NULL,
    NULL,
    NULL,
    NULL,
    NOW(),
    NOW()
);

INSERT INTO topic_room_report (
    id,
    reporter_id,
    reported_user_id,
    topic_room_id,
    chat_message_id,
    reason,
    other_reason,
    report_case_id,
    created_at,
    updated_at
) VALUES (
    963001,
    900004,
    900005,
    920003,
    970015,
    'SPAM',
    NULL,
    960003,
    NOW(),
    NOW()
);

-- User 5 reports user 2's feed reply.
INSERT INTO report_case (
    report_case_id,
    target_type,
    target_id,
    reported_user_id,
    status,
    processed_by_admin_id,
    process_memo,
    process_action,
    processed_at,
    created_at,
    updated_at
) VALUES (
    960004,
    'FEED_REPLY',
    940004,
    900002,
    'RECEIVED',
    NULL,
    NULL,
    NULL,
    NULL,
    NOW(),
    NOW()
);

INSERT INTO feed_reply_report (
    id,
    reporter_id,
    reported_user_id,
    reply_id,
    report_case_id,
    created_at,
    updated_at
) VALUES (
    964001,
    900005,
    900002,
    940004,
    960004,
    NOW(),
    NOW()
);

COMMIT;
