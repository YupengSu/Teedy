alter table T_FILE alter column FIL_IDUSER_C set not null;

create table T_USER_REGISTRATION_REQUEST (
  URR_ID_C varchar(36) not null,
  URR_USERNAME_C varchar(50) not null,
  URR_EMAIL_C varchar(100) not null,
  URR_PASSWORD_C varchar(100) not null,
  URR_STATUS_C varchar(20) not null,
  URR_CREATEDATE_D timestamp not null,
  URR_PROCESSDATE_D timestamp,
  URR_PROCESSUSERID_C varchar(36),
  URR_PROCESSCOMMENT_C varchar(500),
  primary key (URR_ID_C)
);

update T_CONFIG set CFG_VALUE_C = '5' where CFG_ID_C = 'DB_VERSION';
