-- apply changes
create table o_packages_file_hash (
  id                            number(19) not null,
  packages_file_id              number(19),
  hash_type                     varchar2(255),
  hash_value                    varchar2(255),
  constraint pk_o_packages_file_hash primary key (id)
);
create sequence o_packages_file_hash_seq;

create table o_deb_packages_file (
  id                            number(19) not null,
  repository_id                 number(19),
  packages_file_name            varchar2(255),
  packages_file                 blob,
  dist                          varchar2(255),
  component                     varchar2(255),
  arch                          varchar2(255),
  path                          varchar2(255),
  modified_time                 timestamp not null,
  constraint pk_o_deb_packages_file primary key (id)
);
create sequence o_deb_packages_file_seq;

create table o_deb_release_file (
  id                            number(19) not null,
  repository_id                 number(19),
  dist                          varchar2(255),
  modified_time                 timestamp,
  release_file                  clob,
  in_release_file               clob,
  release_file_gpg              clob,
  constraint uq_o_deb_release_file_repository_id_dist unique (repository_id,dist),
  constraint pk_o_deb_release_file primary key (id)
);
create sequence o_deb_release_file_seq;

create table o_release_file_simple_hash (
  id                            number(19) not null,
  release_file_simple_id        number(19),
  hash_type                     varchar2(255),
  hash_value                    varchar2(255),
  constraint pk_o_release_file_simple_hash primary key (id)
);
create sequence o_release_file_simple_hash_seq;

create table o_deb_release_file_simple (
  id                            number(19) not null,
  repository_id                 number(19),
  release_file_name             varchar2(255),
  release_file                  clob,
  dist                          varchar2(255),
  component                     varchar2(255),
  arch                          varchar2(255),
  path                          varchar2(255),
  modified_time                 timestamp not null,
  constraint pk_o_deb_release_file_simple primary key (id)
);
create sequence o_deb_release_file_simple_seq;

alter table o_packages_file_hash add constraint fk_o_packages_file_hash_packages_file_id foreign key (packages_file_id) references o_deb_packages_file (id);
create index ix_o_packages_file_hash_packages_file_id on o_packages_file_hash (packages_file_id);

alter table o_deb_packages_file add constraint fk_o_deb_packages_file_repository_id foreign key (repository_id) references o_repository (id);
create index ix_o_deb_packages_file_repository_id on o_deb_packages_file (repository_id);

alter table o_deb_release_file add constraint fk_o_deb_release_file_repository_id foreign key (repository_id) references o_repository (id);
create index ix_o_deb_release_file_repository_id on o_deb_release_file (repository_id);

alter table o_release_file_simple_hash add constraint fk_o_release_file_simple_hash_release_file_simple_id foreign key (release_file_simple_id) references o_deb_release_file_simple (id);
create index ix_o_release_file_simple_hash_release_file_simple_id on o_release_file_simple_hash (release_file_simple_id);

alter table o_deb_release_file_simple add constraint fk_o_deb_release_file_simple_repository_id foreign key (repository_id) references o_repository (id);
create index ix_o_deb_release_file_simple_repository_id on o_deb_release_file_simple (repository_id);

