-- apply changes
alter table o_debpackage drop column package_name;

alter table o_debpackage drop column version;

alter table o_debpackage drop column arch;

alter table o_debpackage drop column build_id;

alter table o_debpackage drop column build_type_id;

alter table o_debpackage drop column filename;

drop table if exists o_debpackage_parameter;
