create table o_debfile (
  id                            bigint auto_increment not null,
  package_name                  varchar(255),
  version                       varchar(255),
  arch                          varchar(255),
  build_id                      bigint,
  build_type_id                 varchar(255),
  filename                      varchar(255),
  constraint pk_o_debfile primary key (id)
);

create table o_deb_metadata_file (
  id                            bigint auto_increment not null,
  repository_id                 bigint,
  file_name                     varchar(255),
  file_content                  blob,
  dist                          varchar(255),
  component                     varchar(255),
  arch                          varchar(255),
  path                          varchar(255),
  md5                           varchar(255),
  sha1                          varchar(255),
  sha256                        varchar(255),
  modified_time                 timestamp not null,
  constraint pk_o_deb_metadata_file primary key (id)
);

create table o_debpackage (
  id                            bigint auto_increment not null,
  repository_id                 bigint,
  deb_file_id                   bigint not null,
  dist                          varchar(255),
  component                     varchar(255),
  uri                           varchar(255),
  constraint pk_o_debpackage primary key (id)
);

create table o_debfile_parameter (
  id                            bigint auto_increment not null,
  deb_file_id                   bigint,
  name                          varchar(255),
  value                         varchar(255),
  constraint pk_o_debfile_parameter primary key (id)
);

create table o_deb_packages_file (
  id                            bigint auto_increment not null,
  repository_id                 bigint,
  packages_file_name            varchar(255),
  file_contents                 blob,
  dist                          varchar(255),
  component                     varchar(255),
  arch                          varchar(255),
  path                          varchar(255),
  md5                           varchar(255),
  sha1                          varchar(255),
  sha256                        varchar(255),
  modified_time                 timestamp not null,
  constraint pk_o_deb_packages_file primary key (id)
);

create table o_deb_release_file (
  id                            bigint auto_increment not null,
  repository_id                 bigint,
  dist                          varchar(255),
  modified_time                 timestamp,
  release_file                  clob,
  in_release_file               clob,
  release_file_gpg              clob,
  constraint pk_o_deb_release_file primary key (id)
);

create table o_deb_release_file_simple (
  id                            bigint auto_increment not null,
  repository_id                 bigint,
  release_file_name             varchar(255),
  release_file                  clob,
  dist                          varchar(255),
  component                     varchar(255),
  arch                          varchar(255),
  path                          varchar(255),
  md5                           varchar(255),
  sha1                          varchar(255),
  sha256                        varchar(255),
  modified_time                 timestamp not null,
  constraint pk_o_deb_release_file_simple primary key (id)
);

create table o_repository (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  uuid                          varchar(255),
  project_id                    varchar(255),
  constraint pk_o_repository primary key (id)
);

create index ix_o_debfile_build_id_filename on o_debfile (build_id,filename);
create index ix_o_deb_metadata_file_repository_id_dist_path_modified_t_1 on o_deb_metadata_file (repository_id,dist,path,modified_time);
create index ix_o_deb_metadata_file_file_name on o_deb_metadata_file (file_name);
create index ix_o_deb_metadata_file_dist on o_deb_metadata_file (dist);
create index ix_o_deb_metadata_file_path on o_deb_metadata_file (path);
create index ix_o_deb_metadata_file_modified_time on o_deb_metadata_file (modified_time);
create index ix_o_deb_packages_file_repository_id_dist_path_modified_t_1 on o_deb_packages_file (repository_id,dist,path,modified_time);
create index ix_o_deb_packages_file_dist on o_deb_packages_file (dist);
create index ix_o_deb_packages_file_path on o_deb_packages_file (path);
create index ix_o_deb_packages_file_modified_time on o_deb_packages_file (modified_time);
create index ix_o_deb_metadata_file_repository_id on o_deb_metadata_file (repository_id);
alter table o_deb_metadata_file add constraint fk_o_deb_metadata_file_repository_id foreign key (repository_id) references o_repository (id) on delete restrict on update restrict;

create index ix_o_debpackage_repository_id on o_debpackage (repository_id);
alter table o_debpackage add constraint fk_o_debpackage_repository_id foreign key (repository_id) references o_repository (id) on delete restrict on update restrict;

create index ix_o_debpackage_deb_file_id on o_debpackage (deb_file_id);
alter table o_debpackage add constraint fk_o_debpackage_deb_file_id foreign key (deb_file_id) references o_debfile (id) on delete restrict on update restrict;

create index ix_o_debfile_parameter_deb_file_id on o_debfile_parameter (deb_file_id);
alter table o_debfile_parameter add constraint fk_o_debfile_parameter_deb_file_id foreign key (deb_file_id) references o_debfile (id) on delete restrict on update restrict;

create index ix_o_deb_packages_file_repository_id on o_deb_packages_file (repository_id);
alter table o_deb_packages_file add constraint fk_o_deb_packages_file_repository_id foreign key (repository_id) references o_repository (id) on delete restrict on update restrict;

create index ix_o_deb_release_file_repository_id on o_deb_release_file (repository_id);
alter table o_deb_release_file add constraint fk_o_deb_release_file_repository_id foreign key (repository_id) references o_repository (id) on delete restrict on update restrict;

create index ix_o_deb_release_file_simple_repository_id on o_deb_release_file_simple (repository_id);
alter table o_deb_release_file_simple add constraint fk_o_deb_release_file_simple_repository_id foreign key (repository_id) references o_repository (id) on delete restrict on update restrict;

