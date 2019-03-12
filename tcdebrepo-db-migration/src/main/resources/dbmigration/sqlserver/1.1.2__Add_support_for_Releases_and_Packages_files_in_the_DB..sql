-- apply changes
create table o_packages_file_hash (
  id                            numeric(19) identity(1,1) not null,
  packages_file_id              numeric(19),
  hash_type                     varchar(255),
  hash_value                    varchar(255),
  constraint pk_o_packages_file_hash primary key (id)
);

create table o_deb_packages_file (
  id                            numeric(19) identity(1,1) not null,
  repository_id                 numeric(19),
  packages_file_name            varchar(255),
  packages_file                 image,
  dist                          varchar(255),
  component                     varchar(255),
  arch                          varchar(255),
  path                          varchar(255),
  modified_time                 datetime2 not null,
  constraint pk_o_deb_packages_file primary key (id)
);

create table o_deb_release_file (
  id                            numeric(19) identity(1,1) not null,
  repository_id                 numeric(19),
  dist                          varchar(255),
  modified_time                 datetime2,
  release_file                  text,
  in_release_file               text,
  release_file_gpg              text,
  constraint uq_o_deb_release_file_repository_id_dist unique (repository_id,dist),
  constraint pk_o_deb_release_file primary key (id)
);

create table o_release_file_simple_hash (
  id                            numeric(19) identity(1,1) not null,
  release_file_simple_id        numeric(19),
  hash_type                     varchar(255),
  hash_value                    varchar(255),
  constraint pk_o_release_file_simple_hash primary key (id)
);

create table o_deb_release_file_simple (
  id                            numeric(19) identity(1,1) not null,
  repository_id                 numeric(19),
  release_file_name             varchar(255),
  release_file                  text,
  dist                          varchar(255),
  component                     varchar(255),
  arch                          varchar(255),
  path                          varchar(255),
  modified_time                 datetime2 not null,
  constraint pk_o_deb_release_file_simple primary key (id)
);

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

