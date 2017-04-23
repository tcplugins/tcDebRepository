SELECT count(O_DEBFILE.ID) FROM O_DEBFILE as f 
				left join O_DEBPACKAGE as p 
				on f.ID = p.DEB_FILE_ID 
				where p.DEB_FILE_ID IS NULL; 


select * from o_debpackage ;

select * from o_debfile ;

SELECT * FROM O_DEBFILE as f left join O_DEBFILE_PARAMETER  as p on f.ID = p.DEB_FILE_ID
 where p.DEB_FILE_ID IS NULL; 


delete from o_debfile_parameter  where deb_file_id = 1;

-- delete from table where id in (your select query)

/* Delete the package parameters for DebFile rows that don't
 * have a corresponding DebPackage row.
 * Do this first before deleting the DebFile row due to FK constraints.
 */
delete from o_debfile_parameter where deb_file_id in 
	(
		SELECT O_DEBFILE.ID FROM O_DEBFILE  
				left join O_DEBPACKAGE  
				on O_DEBFILE.ID = O_DEBPACKAGE.DEB_FILE_ID 
				where O_DEBPACKAGE.DEB_FILE_ID IS NULL
	);

/* Delete the DebFile rows that don't
 * have a corresponding DebPackage row.
 */
delete from O_DEBFILE where ID in 
	(
		SELECT O_DEBFILE.ID FROM O_DEBFILE  
				left join O_DEBPACKAGE  
				on O_DEBFILE.ID = O_DEBPACKAGE.DEB_FILE_ID 
				where O_DEBPACKAGE.DEB_FILE_ID IS NULL
	); 
	
	
-- Should be unique DebFile names, although it doesn't appear to work	
select distinct t0.id c0, t0.uri c1, t0.id c2, t0.id c3, t0.id c4, t1.id c5, t1.package_name c6, t1.version c7, t1.arch c8, t1.build_id c9, t1.build_type_id c10, t1.filename c11 
	from o_debpackage t0 join o_debfile t1 on t1.id = t0.deb_file_id  
	left join o_repository t2 on t2.id = t0.repository_id  
	where t2.name = ?  and t0.component = ?  and t1.package_name = ?
	
DEBUG -            org.avaje.ebean.SUM - txn[56322] FindMany type[DebPackageModel] origin[BFetci.CiggGZ.DzVvb_] exeMicros[46875] rows[1224] predicates[t2.name = ?  and t0.component = ?  and t1.package_name = ? ] bind[RootTest01,main,tcDummyDeb]


-- This appears to produce a better result.

SELECT distinct (FILENAME , URI), FILENAME, URI  
	FROM O_DEBFILE 
	JOIN O_DEBPACKAGE 
	JOIN O_REPOSITORY  
	ON O_DEBFILE.ID = O_DEBPACKAGE.DEB_FILE_ID 
		AND O_DEBPACKAGE.REPOSITORY_ID = O_REPOSITORY.ID 
	WHERE O_REPOSITORY.NAME = 'MyStore03' 
		AND COMPONENT ='main' 
		AND PACKAGE_NAME = 'e3';
		
		
-- Finding most recent Packages Files for a Release

SELECT MAX(MODIFIED_TIME), Max(ID) as ID, PATH FROM O_DEB_PACKAGES_FILE WHERE REPOSITORY_ID = 2 GROUP BY PATH ORDER BY PATH


SELECT * from O_DEB_PACKAGES_FILE where ID IN (
SELECT Max(ID) as ID FROM O_DEB_PACKAGES_FILE WHERE REPOSITORY_ID = 2 GROUP BY PATH ORDER BY PATH
)

SELECT PATH,* from O_DEB_PACKAGES_FILE where ID IN (
Select ID
From  O_DEB_PACKAGES_FILE t1
where MODIFIED_TIME = (select max(MODIFIED_TIME) from O_DEB_PACKAGES_FILE where O_DEB_PACKAGES_FILE.REPOSITORY_ID = 2 AND T1.PATH = O_DEB_PACKAGES_FILE.PATH)
) ORDER BY PATH

SELECT * from O_DEB_PACKAGES_FILE where ID IN (
Select ID
From  O_DEB_PACKAGES_FILE t1
where MODIFIED_TIME = (select max(MODIFIED_TIME) from O_DEB_PACKAGES_FILE where O_DEB_PACKAGES_FILE.REPOSITORY_ID = 2 AND T1.PATH = O_DEB_PACKAGES_FILE.PATH)
)

Select ID, PATH, MODIFIED_TIME
From  O_DEB_PACKAGES_FILE t1
where ID = (select max(ID) from O_DEB_PACKAGES_FILE where O_DEB_PACKAGES_FILE.REPOSITORY_ID = 2 AND T1.PATH = O_DEB_PACKAGES_FILE.PATH)

Select ID, PATH, MODIFIED_TIME
From  O_DEB_PACKAGES_FILE t1
where ID = (select max(ID) from O_DEB_PACKAGES_FILE where O_DEB_PACKAGES_FILE.REPOSITORY_ID = 1 AND T1.PATH = O_DEB_PACKAGES_FILE.PATH)

		