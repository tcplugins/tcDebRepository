-- apply changes
alter table o_deb_packages_file add column md5 varchar2(255);
alter table o_deb_packages_file add column sha1 varchar2(255);
alter table o_deb_packages_file add column sha256 varchar2(255);

alter table o_deb_release_file_simple add column md5 varchar2(255);
alter table o_deb_release_file_simple add column sha1 varchar2(255);
alter table o_deb_release_file_simple add column sha256 varchar2(255);

