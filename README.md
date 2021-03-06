# phonemic-analysis

This repository contains software related to my PhD "Machine-assisted phonemic analysis". One of the main tools is a phonetic distance measure.

## Prerequisites

This tool requires Java runtime which is already installed on many systems. 

## Installation

Download the release zip package that includes the binaries and source (click on releases). 

## Running the tools

In the command line navigate to the "bin" directory e.g. `cd bin` You can then test one of the tools e.g. in linux terminal type: `java Phone` . Note that in windows power shell you need to force it to use UTF-8 encoding e.g. `java "-Dfile.encoding=utf8" PhoneInventory > PhoneInventory_output.txt`

## TestPhone

This will give you the closest phone for each of the phones in an inventory. 

Example usage:

`java TestPhone ../resources/ker/kera_components_utf8nfc.tsv ../resources/ces/czech_components_utf8nfc.tsv`

## BatchFindAndReplace 

When given an input file and a mapping file (in tab separated value format) this program creates a new file with all the appropriate find and replace mappings done together in one batch simultaneously. If there is a sequence to be found that is a subset of another sequence, the longer sequences takes priority. It is particularly helpful when dealing with unicode text. I was surprised after searching I could not find a tool that had this exact functionality, but let me know if there was a tool I missed.

Example usage:

`java BatchFindAndReplace helloWorldInTimitAscii.txt timit2ipa_utf8nfc.txt > helloWorldInIpa_utf8nfc.txt`

## Known Issues

If your Unicode text files contain the UTF-8 preamble (also know as the UTF-8 BOM) it can sometimes cause problems with the software. Current versions of Windows Notepad save text files with the UTF-8 preamble. It is best to use an alternative text editor.  
