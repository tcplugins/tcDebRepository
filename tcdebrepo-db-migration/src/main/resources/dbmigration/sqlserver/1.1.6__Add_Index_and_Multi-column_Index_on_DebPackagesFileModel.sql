-- apply changes
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('o_debfile','U') AND name = 'ix_o_debfile_build_id_filename') drop index ix_o_debfile_build_id_filename ON o_debfile;
