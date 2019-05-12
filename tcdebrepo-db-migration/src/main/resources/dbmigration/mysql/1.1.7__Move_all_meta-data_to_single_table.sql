-- apply changes
create table o_deb_metadata_file (
  id                            bigint auto_increment not null,
  repository_id                 bigint,
  file_name                     varchar(255),
  file_content                  longblob,
  dist                          varchar(255),
  component                     varchar(255),
  arch                          varchar(255),
  path                          varchar(255),
  md5                           varchar(255),
  sha1                          varchar(255),
  sha256                        varchar(255),
  modified_time                 datetime(6) not null,
  constraint pk_o_deb_metadata_file primary key (id)
);

create index ix_o_deb_metadata_file_repository_id_dist_path_modified_t_1 on o_deb_metadata_file (repository_id,dist,path,modified_time);
create index ix_o_deb_metadata_file_file_name on o_deb_metadata_file (file_name);
create index ix_o_deb_metadata_file_dist on o_deb_metadata_file (dist);
create index ix_o_deb_metadata_file_path on o_deb_metadata_file (path);
create index ix_o_deb_metadata_file_modified_time on o_deb_metadata_file (modified_time);
drop index ix_o_deb_packages_file_repository_id_dist_path_modified_t_1 on o_deb_packages_file;
drop index ix_o_deb_packages_file_dist on o_deb_packages_file;
drop index ix_o_deb_packages_file_path on o_deb_packages_file;
drop index ix_o_deb_packages_file_modified_time on o_deb_packages_file;
create index ix_o_deb_metadata_file_repository_id on o_deb_metadata_file (repository_id);
alter table o_deb_metadata_file add constraint fk_o_deb_metadata_file_repository_id foreign key (repository_id) references o_repository (id) on delete restrict on update restrict;

