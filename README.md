# phonemic-analysis
This repository contains software related to my PhD "Machine-assisted phonemic analysis"

BatchFindAndReplace.java When given an input file and a mapping file (in tab separated value format) this program creates a new file with all the appropriate find and replace mappings done together in one batch simultaneously. If there is a sequence to be found that is a subset of another sequence, the longer sequences takes priority. It is particularly helpful when dealing with unicode text. I was surprised after searching I could not find a tool that had this exact functionality, but let me know if there was one I missed. Also let me know if you come across any bugs.

Example usage:

`java BatchFindAndReplace helloWorldInTimitAscii.txt timit2ipa_utf8nfc.txt > helloWorldInIpa_utf8nfc.txt`
