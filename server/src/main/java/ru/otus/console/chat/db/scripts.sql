create sequence users_id_seq start 1;
create sequence roles_id_seq start 1;

create table users_tab (
	id integer default nextval('users_id_seq') primary key,
	login varchar(30) not null,
	"password" varchar(30) not null,
	user_name varchar(30),
	block_fl char(1) check (block_fl in ('Y','N')) default 'N',
	created_at timestamp default current_timestamp,
	constraint user_login unique (login)
);

create table roles_tab (
	id integer default nextval('roles_id_seq') primary key,
	name varchar(50) not null,
	description varchar(200),
	created_at timestamp default current_timestamp,
	constraint role_name unique (name)
);

create table users_roles_tab (
	user_id integer not null,
	role_id integer not null,
	foreign key (user_id) references users_tab (id),
	foreign key (role_id) references roles_tab (id),
	constraint uid_rid_pk primary key (user_id, role_id)
);


insert into users_tab (login,"password",user_name)
values ('admin','admin','admin'),('user','user','user');

insert into roles_tab ("name",description)
values ('admin','Администратор'),('user','Пользователь');

insert into users_roles_tab (user_id, role_id)
values (1,1),(1,2),(2,2);