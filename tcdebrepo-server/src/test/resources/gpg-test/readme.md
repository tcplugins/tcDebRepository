## Instructions for Signing and Verifying files.

### Create a GPG key
Create key as per https://www.devdungeon.com/content/gpg-tutorial

    gpg --gen-key
    gpg --list-secret-keys
    gpg --export-secret-keys --armor XXXXXXXX > ./tcdebrepo-development-key.asc
    gpg --export-secret-keys XXXXXXXX > ./tcdebrepo-development-key.bpg
    
### ClearSign and then Verify a file using GPG

    gpg -o InRelease --clearsign Release
    gpg --verify InRelease 
    
### ClearSign with Java
    java -cp bcpg-jdk15on-1.61.jar:bcprov-jdk15on-1.61.jar \ 
    org.bouncycastle.openpgp.examples.ClearSignedFileProcessor \
    -s Release tcdebrepo-development-key.bpg abcde123

Four options are passed on the commandline (the last line above). Those options are:
 - `-s` to clearsign.
 - file to sign
 - binary key file name
 - passphrase for key

Note that verify with GPG is very picky about the Release file input.
It must end on a newline, with no extra spaces on the last line.
The above outputs a file called `<input_file>.asc`
We will need to output a file called `InRelease`

### Verify with GPG
This step is to validate that the Bouncy Castle output is compatible with gpg (and therefore hopefully APT).

    gpg --verify Release.asc
