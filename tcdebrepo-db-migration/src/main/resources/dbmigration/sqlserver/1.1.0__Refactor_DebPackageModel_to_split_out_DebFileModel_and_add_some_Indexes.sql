-- apply changes
create table o_debfile (
  id                            numeric(19) identity(1,1) not null,
  package_name                  varchar(255),
  version                       varchar(255),
  arch                          varchar(255),
  build_id                      numeric(19),
  build_type_id                 varchar(255),
  filename                      varchar(255),
  constraint pk_o_debfile primary key (id)
);

alter table o_debpackage add column deb_file_id numeric(19);

create table o_debfile_parameter (
  id                            numeric(19) identity(1,1) not null,
  deb_file_id                   numeric(19),
  name                          varchar(255),
  value                         varchar(255),
  constraint pk_o_debfile_parameter primary key (id)
);

create index ix_o_debfile_build_id_filename on o_debfile (build_id,filename);
alter table o_debpackage add constraint fk_o_debpackage_deb_file_id foreign key (deb_file_id) references o_debfile (id);
create index ix_o_debpackage_deb_file_id on o_debpackage (deb_file_id);

alter table o_debfile_parameter add constraint fk_o_debfile_parameter_deb_file_id foreign key (deb_file_id) references o_debfile (id);
create index ix_o_debfile_parameter_deb_file_id on o_debfile_parameter (deb_file_id);

