# Repository Notes

## Debian Repository File Types

#### Release
A text file with meta-data about the repository, and - if relevant - may also contain a list of all the 
Release and Package.gz files and their checksums from any subdirectories.
Examples:
  http://ppa.launchpad.net/graphics-drivers/ppa/ubuntu/dists/zesty/Release
  http://ppa.launchpad.net/graphics-drivers/ppa/ubuntu/dists/zesty/main/binary-amd64/Release

#### Release.gpg
The a text file containing the hash of the Release file. 
Produced by GPG signing the Release file.
Example:
  http://ppa.launchpad.net/graphics-drivers/ppa/ubuntu/dists/zesty/Release.gpg

#### InRelease
A PGP clear-signed version of the Release file. Use by newer deb clients. Takes the place of the Release
and Release.gpg files.
Example:
  http://ppa.launchpad.net/graphics-drivers/ppa/ubuntu/dists/zesty/InRelease

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