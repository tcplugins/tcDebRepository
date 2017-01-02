alter table o_debpackage drop constraint if exists fk_o_debpackage_repository_id;
drop index if exists ix_o_debpackage_repository_id;

alter table o_debpackage_parameter drop constraint if exists fk_o_debpackage_parameter_deb_package_id;
drop index if exists ix_o_debpackage_parameter_deb_package_id;

drop table if exists o_debpackage;

drop table if exists o_debpackage_parameter;

drop table if exists o_repository;

