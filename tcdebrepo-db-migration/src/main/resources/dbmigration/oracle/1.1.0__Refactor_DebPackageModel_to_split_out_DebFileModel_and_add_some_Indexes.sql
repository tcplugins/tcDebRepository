-- apply changes
create table o_debfile (
  id                            number(19) not null,
  package_name                  varchar2(255),
  version                       varchar2(255),
  arch                          varchar2(255),
  build_id                      number(19),
  build_type_id                 varchar2(255),
  filename                      varchar2(255),
  constraint pk_o_debfile primary key (id)
);
create sequence o_debfile_seq;

alter table o_debpackage add column deb_file_id number(19);

create table o_debfile_parameter (
  id                            number(19) not null,
  deb_file_id                   number(19),
  name                          varchar2(255),
  value                         varchar2(255),
  constraint pk_o_debfile_parameter primary key (id)
);
create sequence o_debfile_parameter_seq;

create index ix_o_debfile_build_id_filename on o_debfile (build_id,filename);
alter table o_debpackage add constraint fk_o_debpackage_deb_file_id foreign key (deb_file_id) references o_debfile (id);
create index ix_o_debpackage_deb_file_id on o_debpackage (deb_file_id);

alter table o_debfile_parameter add constraint fk_o_debfile_parameter_deb_file_id foreign key (deb_file_id) references o_debfile (id);
create index ix_o_debfile_parameter_deb_file_id on o_debfile_parameter (deb_file_id);

