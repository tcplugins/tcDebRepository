-- apply changes
alter table o_deb_release_file drop constraint uq_o_db_rls_fl_rpstry_d_dst;
-- NOT YET IMPLEMENTED: alter table o_deb_release_file add constraint uq_o_db_rls_fl_rpstry_d_dst unique  (repository_id,dist);
