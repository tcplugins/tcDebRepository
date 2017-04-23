alter table o_debpackage drop constraint if exists fk_o_debpackage_repository_id;
drop index if exists ix_o_debpackage_repository_id;

alter table o_debpackage drop constraint if exists fk_o_debpackage_deb_file_id;
drop index if exists ix_o_debpackage_deb_file_id;

alter table o_debfile_parameter drop constraint if exists fk_o_debfile_parameter_deb_file_id;
drop index if exists ix_o_debfile_parameter_deb_file_id;

alter table o_packages_file_hash drop constraint if exists fk_o_packages_file_hash_packages_file_id;
drop index if exists ix_o_packages_file_hash_packages_file_id;

alter table o_deb_packages_file drop constraint if exists fk_o_deb_packages_file_repository_id;
drop index if exists ix_o_deb_packages_file_repository_id;

alter table o_deb_release_file drop constraint if exists fk_o_deb_release_file_repository_id;
drop index if exists ix_o_deb_release_file_repository_id;

alter table o_release_file_simple_hash drop constraint if exists fk_o_release_file_simple_hash_release_file_simple_id;
drop index if exists ix_o_release_file_simple_hash_release_file_simple_id;

alter table o_deb_release_file_simple drop constraint if exists fk_o_deb_release_file_simple_repository_id;
drop index if exists ix_o_deb_release_file_simple_repository_id;

drop table if exists o_debfile;

drop table if exists o_debpackage;

drop table if exists o_debfile_parameter;

drop table if exists o_packages_file_hash;

drop table if exists o_deb_packages_file;

drop table if exists o_deb_release_file;

drop table if exists o_release_file_simple_hash;

drop table if exists o_deb_release_file_simple;

drop table if exists o_repository;

drop index if exists ix_o_debfile_build_id_filename;
