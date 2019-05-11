alter table o_deb_metadata_file drop constraint if exists fk_o_deb_metadata_file_repository_id;
drop index if exists ix_o_deb_metadata_file_repository_id;

alter table o_debpackage drop constraint if exists fk_o_debpackage_repository_id;
drop index if exists ix_o_debpackage_repository_id;

alter table o_debpackage drop constraint if exists fk_o_debpackage_deb_file_id;
drop index if exists ix_o_debpackage_deb_file_id;

alter table o_debfile_parameter drop constraint if exists fk_o_debfile_parameter_deb_file_id;
drop index if exists ix_o_debfile_parameter_deb_file_id;

drop table if exists o_debfile;

drop table if exists o_deb_metadata_file;

drop table if exists o_debpackage;

drop table if exists o_debfile_parameter;

drop table if exists o_repository;

drop index if exists ix_o_debfile_build_id_filename;
drop index if exists ix_o_deb_metadata_file_repository_id_dist_path_modified_t_1;
drop index if exists ix_o_deb_metadata_file_file_name;
drop index if exists ix_o_deb_metadata_file_dist;
drop index if exists ix_o_deb_metadata_file_path;
drop index if exists ix_o_deb_metadata_file_modified_time;
