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


SELECT PACKAGE_NAME, MAX(VERSION) FROM O_DEBFILE GROUP BY PACKAGE_NAME

SELECT * from O_DEBPACKAGE JOIN O_DEBFILE  ;

SELECT * from O_DEBFILE where PACKAGE_NAME, BUILD_TYPE_ID, BUILD_ID IN (
SELECT PACKAGE_NAME, BUILD_TYPE_ID, MAX(BUILD_ID) as BUILD_ID FROM O_DEBFILE where ARCH = 'all'  GROUP BY PACKAGE_NAME, BUILD_TYPE_ID
)


SELECT COMPONENT, DIST, PACKAGE_NAME, BUILD_TYPE_ID, MAX(BUILD_ID)  FROM O_DEBFILE
LEFT JOIN O_DEBPACKAGE ON O_DEBFILE.ID = O_DEBPACKAGE.DEB_FILE_ID WHERE REPOSITORY_ID = 1 AND DIST='jessie' 
GROUP BY PACKAGE_NAME, BUILD_TYPE_ID;

SELECT  * from O_DEBFILE where ARCH='all'

SELECT * from O_DEB_PACKAGES_FILE where ID IN (
SELECT Max(ID) as ID FROM O_DEB_PACKAGES_FILE WHERE REPOSITORY_ID = 1 GROUP BY PATH ORDER BY PATH
)

/* Find most recent 4 of each set */
SELECT i1.*, 
FROM O_DEBPACKAGE  i1
LEFT OUTER JOIN O_DEBPACKAGE  i2
  ON (i1.DIST = i2.DIST AND i1.COMPONENT = i2.COMPONENT  AND i1.ID  < i2.ID )
GROUP BY i1.ID 
HAVING COUNT(*) < 4
ORDER BY DIST, COMPONENT, ID;

SELECT i1.*, O_DEBFILE.*
FROM O_DEBFILE JOIN O_DEBPACKAGE  i1 
LEFT OUTER JOIN O_DEBPACKAGE  i2
  ON (O_DEBFILE.ID = i1.DEB_FILE_ID AND i1.DIST = i2.DIST AND i1.COMPONENT = i2.COMPONENT  AND i1.ID  < i2.ID )
GROUP BY i1.ID 
HAVING COUNT(*) < 4
ORDER BY DIST, COMPONENT, ID;

SELECT i1.*
FROM V_FULL_PACKAGE  i1 
LEFT OUTER JOIN V_FULL_PACKAGE i2
  ON ( i1.DIST = i2.DIST AND i1.COMPONENT = i2.COMPONENT  AND i1.PACKAGE_NAME  = i2.PACKAGE_NAME AND i1.ID  < i2.ID)
WHERE i1.REPOSITORY_ID =2 AND i1.DIST = 'jessie' AND i1.COMPONENT = 'main' AND i1.ARCH = 'i386'
GROUP BY i1.ID 
HAVING COUNT(*) < 4
ORDER BY DIST, COMPONENT, ID DESC;

SELECT I1.*
FROM O_DEBPACKAGE  i1 
LEFT OUTER JOIN O_DEBPACKAGE  i2
  ON (i1.DIST = i2.DIST AND i1.COMPONENT = i2.COMPONENT  AND i1.ID  < i2.ID)
GROUP BY i1.ID 
HAVING COUNT(*) < 4
ORDER BY DIST, COMPONENT, i1.ID;

SELECT I1.*
FROM V_FULL_PACKAGE   i1 
LEFT OUTER JOIN V_FULL_PACKAGE   i2
  ON (i1.DIST = i2.DIST AND i1.COMPONENT = i2.COMPONENT  AND i1.ID  < i2.ID)
WHERE i1.REPOSITORY_ID =2 AND i1.DIST = 'jessie' AND i1.COMPONENT = 'main' AND i1.ARCH = 'i386'
GROUP BY i1.ID 
HAVING COUNT(*) < 4
ORDER BY DIST, COMPONENT, i1.ID DESC;

create VIEW 

CREATE VIEW view_name AS
SELECT column1, column2, ...
FROM table_name
WHERE condition;

select * from V_FULL_PACKAGE i1 WHERE i1.REPOSITORY_ID =2 AND i1.DIST = 'jessie' AND i1.COMPONENT = 'main' AND i1.ARCH = 'i386'
ORDER BY ID DESC


SELECT i1.*
FROM V_FULL_PACKAGE  i1 
LEFT OUTER JOIN V_FULL_PACKAGE i2
  ON ( i1.DIST = i2.DIST AND i1.COMPONENT = i2.COMPONENT  AND i1.PACKAGE_NAME  = i2.PACKAGE_NAME AND i1.ID  < i2.ID)
WHERE i1.REPOSITORY_ID =2 AND i1.DIST = 'jessie' AND i1.COMPONENT = 'main' AND i1.ARCH = 'amd64'
GROUP BY i1.ID 
HAVING COUNT(*) < 4
ORDER BY DIST, COMPONENT, ID DESC;

SELECT I1.*
FROM O_DEBPACKAGE  i1 
LEFT OUTER JOIN O_DEBPACKAGE  i2
  ON (i1.DIST = i2.DIST AND i1.COMPONENT = i2.COMPONENT  AND i1.ID  < i2.ID)
GROUP BY i1.ID 
HAVING COUNT(*) < 4
ORDER BY DIST, COMPONENT, i1.ID;

SELECT I1.*
FROM V_FULL_PACKAGE   i1 
LEFT OUTER JOIN V_FULL_PACKAGE   i2
  ON (i1.DIST = i2.DIST AND i1.COMPONENT = i2.COMPONENT  AND i1.ID  < i2.ID)
WHERE i1.REPOSITORY_ID =2 AND i1.DIST = 'jessie' AND i1.COMPONENT = 'main' AND i1.ARCH = 'i386'
GROUP BY i1.ID 
HAVING COUNT(*) < 4
ORDER BY DIST, COMPONENT, i1.ID DESC;

create VIEW 

CREATE VIEW view_name AS
SELECT column1, column2, ...
FROM table_name
WHERE condition;

select * from V_FULL_PACKAGE i1 WHERE i1.REPOSITORY_ID =2 AND i1.DIST = 'jessie' AND i1.COMPONENT = 'main' AND i1.ARCH = 'amd64'
ORDER BY ID DESC

--- Find unique Packages_file (with sub-select)

SELECT id, PACKAGES_FILE from O_DEB_PACKAGES_FILE where ID IN 
( SELECT ID FROM O_DEB_PACKAGES_FILE T1
   WHERE MODIFIED_TIME = 
   ( SELECT max(MODIFIED_TIME) 
    FROM O_DEB_PACKAGES_FILE 
       WHERE O_DEB_PACKAGES_FILE.REPOSITORY_ID = 1
      AND O_DEB_PACKAGES_FILE.DIST = 'jessie'
         AND T1.PATH = O_DEB_PACKAGES_FILE.PATH
      ) 
 ) ORDER BY PATH;

SELECT ID, PACKAGES_FILE 
FROM O_DEB_PACKAGES_FILE T1
WHERE MODIFIED_TIME = (
     SELECT max(MODIFIED_TIME)
     FROM O_DEB_PACKAGES_FILE
        WHERE O_DEB_PACKAGES_FILE.REPOSITORY_ID = 1
        AND O_DEB_PACKAGES_FILE.DIST = 'jessie'
        AND T1.PATH = O_DEB_PACKAGES_FILE.PATH
    )
ORDER BY PATH;


select count(id), REPOSITORY_ID, DIST,COMPONENT, ARCH, PATH 
from O_DEB_PACKAGES_FILE group by REPOSITORY_ID,DIST,COMPONENT,ARCH,PATH;

select ID, REPOSITORY_ID, DIST,COMPONENT, ARCH, PACKAGES_FILE_NAME  
from O_DEB_PACKAGES_FILE where repository_id =1 AND dist = 'jessie' 
	AND component = 'main' AND arch = 'amd64' AND PACKAGES_FILE_NAME  = 'Packages' 
order by MODIFIED_TIME desc limit 5;

select count(ID) from O_DEB_PACKAGES_FILE 
where repository_id =1 AND dist = 'jessie' AND component = 'main' 
AND arch = 'amd64' AND PACKAGES_FILE_NAME  = 'Packages'  AND ID NOT IN
 (SELECT ID from O_DEB_PACKAGES_FILE where repository_id =1 AND dist = 'jessie' 
   AND component = 'main' AND arch = 'amd64' AND PACKAGES_FILE_NAME  = 'Packages' 
	order by MODIFIED_TIME desc limit 5
 );

delete from O_DEB_PACKAGES_FILE where repository_id =1 AND dist = 'jessie' 
AND component = 'main' AND arch = 'amd64' AND PACKAGES_FILE_NAME  = 'Packages'  AND ID NOT IN
 (SELECT ID from O_DEB_PACKAGES_FILE where repository_id =1 AND dist = 'jessie' 
   AND component = 'main' AND arch = 'amd64' AND PACKAGES_FILE_NAME  = 'Packages' 
	order by MODIFIED_TIME desc limit 5
 );