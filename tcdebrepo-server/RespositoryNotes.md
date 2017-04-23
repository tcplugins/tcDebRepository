# Repository Notes

Lots of this information can be found in more detail here <https://wiki.debian.org/DebianRepository/Format>

## Debian Repository File Types

#### Release
A text file with meta-data about the repository, and - if relevant - may also contain a list of all the 
Release and Package.gz files and their checksums from any sub-directories.<br>
Examples:<br>
    <http://ppa.launchpad.net/graphics-drivers/ppa/ubuntu/dists/zesty/Release><br>
	<http://ppa.launchpad.net/graphics-drivers/ppa/ubuntu/dists/zesty/main/binary-amd64/Release>
 	
##### Release file formats
There are two types Release files.
 1. The Release file at the root of the repository
     This is a full list of the Release and Packages files in the repo, including path and checksums.
 2. A release file in each architecture directory of each component (eg, main/binary-amd64)
     This is a simple description file containing just meta-data about that part of the repository
     eg, 
     
    Archive: stable
    Origin: Debian
    Label: Debian
    Version: 8.7
    Component: main
    Architecture: all

 Field | Presence | Source
 ----- | -------- | -------
 Archive | required | This will be `dist` eg, zesty, stable, experimental, jessie-updates.
 Origin | required | Could be the TC project external ID
 Label | required | Could be the TC project description
 Version | optional | This will not be implemented
 Component | required | This will be the `component`
 Architecture | required | This will be the `arch`
 

##### Generating the Release files
The release file contains meta-data at the top about the repo

    Origin: Debian
    Label: Debian
    Suite: stable
    Version: 8.7
    Codename: jessie
    Date: Sat, 14 Jan 2017 11:03:32 UTC
    Acquire-By-Hash: yes
    Architectures: amd64 arm64 armel armhf i386 mips mipsel powerpc ppc64el s390x
    Components: main contrib non-free
    Description: Debian 8.7 Released 14 January 2017
 
 Field | Presence | Source
 ----- | -------- | -------
 Origin | optional | Could be the TC project external ID
 Label | optional | Could be the TC project description
 Suite | required | This will be `dist` eg, zesty, stable, experimental, jessie-updates. It should be migrated as part of the version upgrade.
 Codename | required | This is often a category into which the `dist` falls or an alternative name for the Suite. However it is often the same as Suite. I think both Suite and Codename could just be the `dist` without causing any issues. These fields appear to be informational, but it will require a review once the `Release` file support is implemented.
 Version | optional | This will not be implemented
 Date | required | The date/time that the Release file was generated in UTC. 
 Architectures | required | A list of architectures this `Release` supports
 Components | required | A list of components this `Release` supports
 Description | optional | Could be the TC project description
 
 
 
#### Release.gpg
A text file containing the hash of the Release file. 
Produced by GPG signing the Release file.<br>
Example:<br>
    <http://ppa.launchpad.net/graphics-drivers/ppa/ubuntu/dists/zesty/Release.gpg>

#### InRelease
A PGP clear-signed version of the Release file. Use by newer deb clients. Takes the place of the Release
and Release.gpg files.<br>
Example:<br>
    <http://ppa.launchpad.net/graphics-drivers/ppa/ubuntu/dists/zesty/InRelease>

## Debian Repository Layout diagram.
	 {RepositoryName}
	  |
	  -- pool
	  |  |
	  |  -- {componentName}, eg, main
	  |     |
	  |     -- /some/dir/debfilename1.deb
	  |     -- /some/dir/debfilename2.deb
	  |     -- /some/dir/debfilename3.deb
	  |
	  |
	  -- dists 
	     |
	     -- {distName}, eg, trusty
	     |  |
	     |  -- InRelease
	     |  -- Release
	     |  -- Release.gpg
	     |  -- {componentName}, eg main
	     |     |
	     |     -- {archName}, eg binary-amd64
	     |        |
	     |        -- Packages.bz2
	     |        -- Packages.gz
	     |        -- Packages (not required, but useful for debugging)
	     |        -- Release (optional)
	     |
	     |     -- {archName}, eg binary-i386
	     |        |
	     |        -- Packages.bz2
	     |        -- Packages.gz
	     |        -- Packages (not required, but useful for debugging)
	     |        -- Release (optional)
	     |
	     |     -- {archName}, eg source
	     |        |
	     |        -- Packages.bz2
	     |        -- Packages.gz
	     |        -- Packages (not required, but useful for debugging)
	     |        -- Release (optional)
	     |
	     -- {distname}, eg, zesty
