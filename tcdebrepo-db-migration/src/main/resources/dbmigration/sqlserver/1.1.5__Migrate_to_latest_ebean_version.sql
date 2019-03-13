-- apply changes
IF (OBJECT_ID('uq_o_deb_release_file_repository_id_dist', 'UQ') IS NOT NULL) alter table o_deb_release_file drop constraint uq_o_deb_release_file_repository_id_dist;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('o_deb_release_file','U') AND name = 'uq_o_deb_release_file_repository_id_dist') drop index uq_o_deb_release_file_repository_id_dist ON o_deb_release_file;
create unique nonclustered index uq_o_deb_release_file_repository_id_dist on o_deb_release_file(repository_id,dist) where repository_id is not null and dist is not null;
