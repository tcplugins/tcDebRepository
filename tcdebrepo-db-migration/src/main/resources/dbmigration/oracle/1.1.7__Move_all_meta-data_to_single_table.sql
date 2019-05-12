-- apply changes
create table o_deb_metadata_file (
  id                            number(19) generated always as identity not null,
  repository_id                 number(19),
  file_name                     varchar2(255),
  file_content                  blob,
  dist                          varchar2(255),
  component                     varchar2(255),
  arch                          varchar2(255),
  path                          varchar2(255),
  md5                           varchar2(255),
  sha1                          varchar2(255),
  sha256                        varchar2(255),
  modified_time                 timestamp not null,
  constraint pk_o_deb_metadata_file primary key (id)
);

create index ix_o_db_mtdt_fl_rpstry__fygonl on o_deb_metadata_file (repository_id,dist,path,modified_time);
create index ix_o_db_mtdt_fl_fl_nm on o_deb_metadata_file (file_name);
create index ix_o_deb_metadata_file_dist on o_deb_metadata_file (dist);
create index ix_o_deb_metadata_file_path on o_deb_metadata_file (path);
create index ix_o_db_mtdt_fl_mdfd_tm on o_deb_metadata_file (modified_time);
drop index ix_o_db_pckgs_fl_rpstry_wygjyn;
drop index ix_o_deb_packages_file_dist;
drop index ix_o_deb_packages_file_path;
drop index ix_o_db_pckgs_fl_mdfd_tm;
create index ix_o_db_mtdt_fl_rpstry_d on o_deb_metadata_file (repository_id);
alter table o_deb_metadata_file add constraint fk_o_db_mtdt_fl_rpstry_d foreign key (repository_id) references o_repository (id);

