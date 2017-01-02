-- apply changes
create table o_debpackage (
  id                            number(19) not null,
  repository_id                 number(19),
  package_name                  varchar2(255),
  version                       varchar2(255),
  arch                          varchar2(255),
  dist                          varchar2(255),
  component                     varchar2(255),
  build_id                      number(19),
  build_type_id                 varchar2(255),
  filename                      varchar2(255),
  uri                           varchar2(255),
  constraint pk_o_debpackage primary key (id)
);
create sequence o_debpackage_seq;

create table o_debpackage_parameter (
  id                            number(19) not null,
  deb_package_id                number(19),
  name                          varchar2(255),
  value                         varchar2(255),
  constraint pk_o_debpackage_parameter primary key (id)
);
create sequence o_debpackage_parameter_seq;

create table o_repository (
  id                            number(19) not null,
  name                          varchar2(255),
  uuid                          varchar2(255),
  project_id                    varchar2(255),
  constraint pk_o_repository primary key (id)
);
create sequence o_repository_seq;

alter table o_debpackage add constraint fk_o_debpackage_repository_id foreign key (repository_id) references o_repository (id);
create index ix_o_debpackage_repository_id on o_debpackage (repository_id);

alter table o_debpackage_parameter add constraint fk_o_debpackage_parameter_deb_package_id foreign key (deb_package_id) references o_debpackage (id);
create index ix_o_debpackage_parameter_deb_package_id on o_debpackage_parameter (deb_package_id);

