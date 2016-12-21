# Stuff to do

In order of urgency

 - [x] Remove artifacts when build cleanup runs
 - [x] Add pool browsing
 - [x] Tidy up editRepository.jsp heading
 - [x] Add rename and project edit dialog to edit screen and validate repo name to be URL friendly
 - [x] Unpack deb file in an emphemeral dir to avoid conflicts, and then cleanup.
 - [x] Test with packages that are not in the root of the artifact list.
 - [x] Add filter count to statistics
 - [x] Add license file to github
 - [x] Migrate repository create action to action controller.
 - [x] Do name validation on repository create.
 - [x] Remove delete and edit actions from ProjectSettingsTabActionController and associated Javascript. 
 - [ ] Migrate to DB. - Deferred until after the competition (plan to implement as part of v1.1)
 - [ ] Test on windows.
 - [x] Test on TC 8, TC 9 and TC 10.0.3 - TC 8 does not support ActionFactory, TC 9 tested and works, TC 10.0.2 & 10.0.3 tested and works. 
 - [ ] Add Artifact Filter copy dialog to ease creation of similar rules.
 - [ ] Allow ability to re-index a repository.
 - [ ] Add some options to admin section to do things like: re-index/cleanup repo, remove items from dangling repos
 - [ ] Handle case were editing a repository that has been deleted returns empty response (and error popup).
 - [ ] Refactor Dialog classes into single class in javascript.
 - [ ] Add artifact chooser (like build editing screen has)
 - [ ] Add regex test to filter edit dialog.
 - [ ] Packages file should be nice list rather than just text file.
 - [ ] List content counts on "directories" while browsing repo. 
 - [ ] List content size/modified date if a file while browsing repo.