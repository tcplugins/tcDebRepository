alter table o_debpackage drop constraint if exists fk_o_debpackage_repository_id;
drop index if exists ix_o_debpackage_repository_id;

drop table if exists o_debpackage;

drop table if exists o_repository;

