-- apply changes
alter table o_debpackage drop column package_name;

alter table o_debpackage drop column version;

alter table o_debpackage drop column arch;

alter table o_debpackage drop column build_id;

alter table o_debpackage drop column build_type_id;

alter table o_debpackage drop column filename;

IF OBJECT_ID('o_debpackage_parameter', 'U') IS NOT NULL drop table o_debpackage_parameter;
