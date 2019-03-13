-- apply changes
alter table o_deb_release_file drop index uq_o_deb_release_file_repository_id_dist;
alter table o_deb_release_file add constraint uq_o_deb_release_file_repository_id_dist unique  (repository_id,dist);
