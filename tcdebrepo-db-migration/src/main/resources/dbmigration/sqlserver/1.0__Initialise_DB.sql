-- apply changes
create table o_debpackage (
  id                            numeric(19) identity(1,1) not null,
  repository_id                 numeric(19),
  package_name                  varchar(255),
  version                       varchar(255),
  arch                          varchar(255),
  dist                          varchar(255),
  component                     varchar(255),
  build_id                      numeric(19),
  build_type_id                 varchar(255),
  filename                      varchar(255),
  uri                           varchar(255),
  constraint pk_o_debpackage primary key (id)
);

create table o_debpackage_parameter (
  id                            numeric(19) identity(1,1) not null,
  deb_package_id                numeric(19),
  name                          varchar(255),
  value                         varchar(255),
  constraint pk_o_debpackage_parameter primary key (id)
);

create table o_repository (
  id                            numeric(19) identity(1,1) not null,
  name                          varchar(255),
  uuid                          varchar(255),
  project_id                    varchar(255),
  constraint pk_o_repository primary key (id)
);

alter table o_debpackage add constraint fk_o_debpackage_repository_id foreign key (repository_id) references o_repository (id);
create index ix_o_debpackage_repository_id on o_debpackage (repository_id);

alter table o_debpackage_parameter add constraint fk_o_debpackage_parameter_deb_package_id foreign key (deb_package_id) references o_debpackage (id);
create index ix_o_debpackage_parameter_deb_package_id on o_debpackage_parameter (deb_package_id);

