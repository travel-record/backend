create table users
(
    id_users           int auto_increment comment '사용자 PK' primary key,
    nickname           varchar(20)  null comment '닉네임',
    image_url          text         null comment '프로필 이미지 URL',
    introduction       varchar(255) null comment '한 줄 소개글',
    email              varchar(255) not null comment '이메일',
    created_date_time  datetime     null comment '기록 생성 시간',
    modified_date_time datetime     null comment '기록 수정 시간',
    deleted_date_time  datetime     null comment '사용자 삭제 시간',
    constraint uk_users_email unique (email),
    constraint uk_users_nickname unique (nickname)
) comment '사용자';

create table feed
(
    id_feed            int auto_increment comment '피드 PK' primary key,
    id_owner           int                                  not null comment '사용자 FK',
    image_url          text                                 null comment '썸네일 이미지 URL',
    description        varchar(255)                         null comment '설명',
    name               varchar(255)                         not null comment '피드 이름',
    start_at           datetime default current_timestamp() not null on update current_timestamp() comment '시작 시간',
    end_at             datetime                             null comment '종료 시간',
    companion          varchar(255)                         null comment '동행자',
    satisfaction       varchar(255)                         null comment '만족도',
    place              varchar(255)                         null comment '장소',
    longitude          varchar(30)                          null comment '장소 경도',
    latitude           varchar(30)                          null comment '장소 위도',
    created_date_time  datetime                             null comment '기록 생성 시간',
    modified_date_time datetime                             null comment '기록 수정 시간',
    deleted_date_time  datetime                             null comment '피드 삭제 시간',
    constraint fk_feed_users foreign key (id_owner) references users (id_users) on update cascade on delete cascade
) comment '피드';

create table feed_contributor
(
    created_date_time  datetime                     null comment '컨트리뷰터 생성 시간',
    deleted_date_time  datetime                     null comment '컨트리뷰터 삭제 시간',
    id_contributor     int auto_increment comment '컨트리뷰터 PK' primary key,
    id_feed            int                          not null comment '피드 FK',
    id_users           int                          not null comment '사용자 FK',
    modified_date_time datetime                     null comment '컨트리뷰터 수정 시간',
    permission         longtext collate utf8mb4_bin null comment '컨트리뷰터 권한'
        check (json_valid(`permission`)),
    status             varchar(20)                  not null comment '피드 컨트리뷰터 상태',
    constraint fk_contributor_feed foreign key (id_feed) references feed (id_feed),
    constraint fk_contributor_users foreign key (id_users) references users (id_users)
) comment '컨트리뷰터';

create table notification
(
    id_notification    int auto_increment comment '알림 아이디' primary key,
    id_users_to        int                                     null comment '알림 받는 사용자 FK',
    type               varchar(50)                             not null comment '알림 타입',
    status             varchar(20) default 'UNREAD'            not null comment '알림 상태(읽음/읽지 않음)',
    args               longtext collate utf8mb4_bin            null comment '인수' check (json_valid(`args`)),
    created_date_time  datetime    default current_timestamp() not null comment '알림 생성 시간',
    modified_date_time datetime    default current_timestamp() not null comment '알림 수정 시간',
    deleted_date_time  datetime                                null comment '알림 삭제 시간',
    constraint fk_notification_users_to foreign key (id_users_to) references users (id_users) on delete cascade
) comment '알림';

create table record
(
    id_record          int auto_increment comment '기록 PK' primary key,
    content            longtext      not null comment '내용',
    date               datetime      not null comment '날짜',
    feeling            varchar(255)  not null comment '기분',
    place              varchar(255)  not null comment '장소',
    longitude          varchar(30)   not null comment '장소 경도',
    latitude           varchar(30)   not null comment '장소 위도',
    transportation     varchar(255)  not null comment '이동 수단',
    title              varchar(255)  not null comment '제목',
    weather            varchar(255)  not null comment '날씨',
    id_feed            int           not null comment '피드 FK',
    image_url          text          null comment '썸네일 이미지 URL',
    sequence           int default 0 not null comment '순서',
    created_date_time  datetime      null comment '기록 생성 시간',
    modified_date_time datetime      null comment '기록 수정 시간',
    deleted_date_time  datetime      null comment '기록 삭제 시간',
    id_author          int           null comment '사용자 PK',
    constraint fk_record_feed foreign key (id_feed) references feed (id_feed) on update cascade on delete cascade,
    constraint fk_record_users foreign key (id_author) references users (id_users) on delete cascade
) comment '기록';

create table comment
(
    id_comment         int auto_increment comment '댓글 PK' primary key,
    id_parent          int          null comment '원 댓글 PK',
    id_record          int          not null comment '기록 FK',
    id_users           int          not null comment '사용자 FK',
    content            varchar(255) not null comment '댓글',
    created_date_time  datetime     null comment '댓글 생성 시간',
    modified_date_time datetime     null comment '댓글 수정 시간',
    deleted_date_time  datetime     null comment '댓글 삭제 시간',
    constraint fk_comment_comment foreign key (id_parent) references comment (id_comment) on delete cascade,
    constraint fk_comment_record foreign key (id_record) references record (id_record) on delete cascade,
    constraint fk_comment_user foreign key (id_users) references users (id_users) on delete cascade
) comment '댓글';

create table record_sequence
(
    id_sequence        int auto_increment comment '기록 순서 PK' primary key,
    id_feed            int                                  not null comment '피드 FK',
    date               datetime                             not null comment '시간',
    sequence           int                                  not null comment '순서',
    created_date_time  datetime default current_timestamp() null comment '순서 생성 시간',
    modified_date_time datetime default current_timestamp() null comment '순서 수정 시간',
    deleted_date_time  datetime                             null comment '순서 삭제 시간',
    constraint idx_feed_date unique (id_feed, date),
    constraint fk_sequence_feed foreign key (id_feed) references feed (id_feed) on delete cascade
) comment '기록 순서 테이블';

create table user_record_like
(
    id_like            int auto_increment comment '좋아요 PK' primary key,
    id_users           int                                  not null comment '사용자 FK',
    id_record          int                                  not null comment '기록 FK',
    created_date_time  datetime default current_timestamp() null comment '좋아요 생성 시간',
    modified_date_time datetime default current_timestamp() null comment '좋아요 수정 시간',
    deleted_date_time  datetime                             null comment '기록 좋아요 삭제 시간',
    constraint idx_users_record unique (id_users, id_record) comment '유저 기록 인덱스',
    constraint fk_user_record_like_record foreign key (id_record) references record (id_record) on delete cascade,
    constraint fk_user_record_like_users foreign key (id_users) references users (id_users) on delete cascade
) comment '사용자의 기록 좋아요';

create index idx_user_id on user_record_like (id_users) comment '유저 PK 인덱스';

-- users table
CREATE INDEX idx_users_nickname ON users(nickname);

-- user_record_like table
CREATE INDEX idx_user_record_like_user ON user_record_like(id_users);
CREATE INDEX idx_user_record_like_record ON user_record_like(id_record);
CREATE INDEX idx_user_record_like_user_record ON user_record_like(id_users, id_record);

-- record table
CREATE INDEX idx_record_feed ON record(id_feed);
CREATE INDEX idx_record_user ON record(id_author);
CREATE INDEX idx_record_feed_date ON record(id_feed, date);
CREATE INDEX idx_record_feed_user ON record(id_feed, id_author);

-- notification table
CREATE INDEX idx_notification_users ON notification(id_users_to);
CREATE INDEX idx_notification_status ON notification(status);
CREATE INDEX idx_notification_users_type ON notification(id_users_to, type);

-- feed_contributor table
CREATE INDEX idx_contributor_users_feed ON feed_contributor(id_users, id_feed);

-- feed table
CREATE INDEX idx_feed_users ON feed(id_owner);

-- comment table
CREATE INDEX idx_comment_users ON comment(id_users);
CREATE INDEX idx_comment_record ON comment(id_record);
CREATE INDEX idx_comment_parent ON comment(id_parent);
