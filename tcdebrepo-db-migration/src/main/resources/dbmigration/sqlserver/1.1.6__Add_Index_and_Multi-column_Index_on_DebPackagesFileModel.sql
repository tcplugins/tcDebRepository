-- apply changes
create index ix_o_deb_packages_file_repository_id_dist_path_modified_t_1 on o_deb_packages_file (repository_id,dist,path,modified_time);
create index ix_o_deb_packages_file_dist on o_deb_packages_file (dist);
create index ix_o_deb_packages_file_path on o_deb_packages_file (path);
create index ix_o_deb_packages_file_modified_time on o_deb_packages_file (modified_time);
