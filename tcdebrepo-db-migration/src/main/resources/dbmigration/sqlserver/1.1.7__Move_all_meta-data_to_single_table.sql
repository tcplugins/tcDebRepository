-- apply changes
create table o_deb_metadata_file (
  id                            numeric(19) identity(1,1) not null,
  repository_id                 numeric(19),
  file_name                     nvarchar(255),
  file_content                  image,
  dist                          nvarchar(255),
  component                     nvarchar(255),
  arch                          nvarchar(255),
  path                          nvarchar(255),
  md5                           nvarchar(255),
  sha1                          nvarchar(255),
  sha256                        nvarchar(255),
  modified_time                 datetime2 not null,
  constraint pk_o_deb_metadata_file primary key (id)
);

create index ix_o_deb_metadata_file_repository_id_dist_path_modified_t_1 on o_deb_metadata_file (repository_id,dist,path,modified_time);
create index ix_o_deb_metadata_file_file_name on o_deb_metadata_file (file_name);
create index ix_o_deb_metadata_file_dist on o_deb_metadata_file (dist);
create index ix_o_deb_metadata_file_path on o_deb_metadata_file (path);
create index ix_o_deb_metadata_file_modified_time on o_deb_metadata_file (modified_time);
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('o_deb_packages_file','U') AND name = 'ix_o_deb_packages_file_repository_id_dist_path_modified_t_1') drop index ix_o_deb_packages_file_repository_id_dist_path_modified_t_1 ON o_deb_packages_file;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('o_deb_packages_file','U') AND name = 'ix_o_deb_packages_file_dist') drop index ix_o_deb_packages_file_dist ON o_deb_packages_file;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('o_deb_packages_file','U') AND name = 'ix_o_deb_packages_file_path') drop index ix_o_deb_packages_file_path ON o_deb_packages_file;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('o_deb_packages_file','U') AND name = 'ix_o_deb_packages_file_modified_time') drop index ix_o_deb_packages_file_modified_time ON o_deb_packages_file;
create index ix_o_deb_metadata_file_repository_id on o_deb_metadata_file (repository_id);
alter table o_deb_metadata_file add constraint fk_o_deb_metadata_file_repository_id foreign key (repository_id) references o_repository (id);

