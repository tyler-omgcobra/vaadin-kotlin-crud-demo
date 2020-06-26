create table person (
    id integer not null auto_increment,
    first_name varchar not null,
    last_name varchar not null,
    street varchar not null ,
    city varchar not null ,
    state varchar not null ,
    zip varchar not null ,
    email varchar not null ,
    area_code varchar not null ,
    first_three varchar not null ,
    last_four varchar not null ,
    primary key(id)
);

create table vals (
    id integer not null auto_increment,
    value varchar not null,
    floaty float not null,
    isit boolean not null,
    primary key(id)
);