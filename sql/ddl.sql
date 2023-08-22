create table users
(
    id_users           int auto_increment comment '사용자 PK'
        primary key,
    nickname           varchar(20)  null comment '닉네임',
    image_url          text         null comment '프로필 이미지 URL',
    introduction       varchar(255) null comment '한 줄 소개글',
    email              varchar(255) not null comment '이메일',
    created_date_time  datetime     null comment '기록 생성 시간',
    modified_date_time datetime     null comment '기록 수정 시간',
    constraint uk_users_email
        unique (email),
    constraint uk_users_nickname
        unique (nickname)
)
    comment '사용자';

create table feed
(
    id_feed            int auto_increment comment '피드 PK'
        primary key,
    id_users           int          not null comment '사용자 FK',
    image_url          text         null comment '썸네일 이미지 URL',
    description        varchar(255) null comment '설명',
    name               varchar(255) not null comment '페이지 이름',
    start_at           timestamp    null comment '시작 시간',
    end_at             timestamp    null comment '종료 시간',
    companion          varchar(255) null comment '동행자',
    satisfaction       varchar(255) null comment '만족도',
    place              varchar(255) null comment '장소',
    created_date_time  datetime     null comment '기록 생성 시간',
    modified_date_time datetime     null comment '기록 수정 시간',
    constraint fk_feed_users
        foreign key (id_users) references users (id_users)
            on update cascade on delete cascade
)
    comment '피드';

create table record
(
    id_record          int auto_increment comment '기록 PK'
        primary key,
    content            longtext     not null comment '내용',
    date               datetime     not null comment '날짜',
    feeling            varchar(255) not null comment '기분',
    place              varchar(255) not null comment '장소',
    transportation     varchar(255) not null comment '이동 수단',
    title              varchar(255) not null comment '제목',
    weather            varchar(255) not null comment '날씨',
    id_feed            int          not null comment '피드 FK',
    companion          varchar(255) null comment '동행자',
    created_date_time  datetime     null comment '기록 생성 시간',
    modified_date_time datetime     null comment '기록 수정 시간',
    image_url          text         null comment '썸네일 이미지 URL',
    constraint fk_record_feed
        foreign key (id_feed) references feed (id_feed)
            on update cascade on delete cascade
)
    comment '기록';

create table comment
(
    id_comment         int auto_increment comment '댓글 PK'
        primary key,
    content            varchar(255) not null comment '댓글',
    id_record          int          not null comment '기록 FK',
    id_users           int          not null comment '사용자 FK',
    created_date_time  datetime     null comment '댓글 생성 시간',
    modified_date_time datetime     null comment '댓글 수정 시간',
    constraint fk_comment_record
        foreign key (id_record) references record (id_record)
            on delete cascade,
    constraint fk_comment_user
        foreign key (id_users) references users (id_users)
            on delete cascade
)
    comment '댓글';

create table notification
(
    id_notification    int auto_increment comment '알림 아이디'
        primary key,
    type               varchar(50)                             not null comment '알림 타입',
    status             varchar(20) default 'UNREAD'            not null comment '알림 상태(읽음/읽지 않음)',
    created_dated_time datetime    default current_timestamp() not null comment '알림 생성 시간',
    modified_date_time datetime    default current_timestamp() not null comment '알림 수정 시간',
    id_users_to        int                                     null comment '알림 받는 사용자 FK',
    id_users_from      int                                     null comment '알림 보내는 사용자 FK',
    id_comment         int                                     null comment '관련 댓글 FK',
    id_record          int                                     null comment '기록 FK',
    constraint fk_notification_comment
        foreign key (id_comment) references comment (id_comment)
            on delete cascade,
    constraint fk_notification_record
        foreign key (id_record) references record (id_record)
            on delete cascade,
    constraint fk_notification_users_from
        foreign key (id_users_from) references users (id_users)
            on delete cascade,
    constraint fk_notification_users_to
        foreign key (id_users_to) references users (id_users)
            on delete cascade
)
    comment '알림';

create table user_record_like
(
    id_like            int auto_increment comment '좋아요 PK'
        primary key,
    id_users           int                                  not null comment '사용자 FK',
    id_record          int                                  not null comment '기록 FK',
    created_date_time  datetime default current_timestamp() null comment '좋아요 생성 시간',
    modified_date_time datetime default current_timestamp() null comment '좋아요 수정 시간',
    constraint idx_users_record
        unique (id_users, id_record) comment '유저 기록 인덱스',
    constraint fk_user_record_like_record
        foreign key (id_record) references record (id_record)
            on delete cascade,
    constraint fk_user_record_like_users
        foreign key (id_users) references users (id_users)
            on delete cascade
)
    comment '사용자의 기록 좋아요';