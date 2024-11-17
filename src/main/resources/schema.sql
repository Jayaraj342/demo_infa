create table post
(
    id      bigint not null auto_increment,
    message varchar(255),
    user_id bigint,
    primary key (id)
) engine = InnoDB;

create table user
(
    id    bigint not null auto_increment,
    email varchar(255),
    name  varchar(255),
    primary key (id)
) engine = InnoDB;

alter table post
    add constraint FK72mt33dhhs48hf9gcqrq4fxte
        foreign key (user_id)
            references user (id);