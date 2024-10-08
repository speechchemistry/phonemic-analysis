# phonemic-analysis

This repository contains software related to my PhD [Machine-assisted phonemic analysis](http://etheses.whiterose.ac.uk/3122/). One of the main tools is a phonetic distance measure; BFEPP (Binary Feature Edits Per Phone).

## Prerequisites

This tool requires Java runtime environment. For example, for Windows you could download Amazon Corretto.

## Installation

Go to the [list of releases](https://github.com/speechchemistry/phonemic-analysis/releases). Under the latest release at the top of the screen, click on "Assets" then click on the jar file to download it. Alternatively, if you want to build from source, clone this repository and type `mvn package` (requires Apache Maven).

## Running the tools

Example for calculating the BFEPP measure between [tu] and [du]. In Windows Powershell type:

`java "-Dfile.encoding=utf8" -jar phonemic-analysis-0.6.jar "t u" "d u"`

in linux you don't need to include the encoding option. You can also run the individual java classes. For example, another way of running the BFEPP tool in Linux is: 

`java -cp phonemic-analysis-0.6.jar io.github.speechchemistry.BfeppForTwoWordsCli "t u" "d u"`

The output should be:

`t u     d u     0.5`

This shows that the binary feature edits per phone (BFEPP) score is 0.5. There is one binary feature difference between [t] and [d] which is a change from -voice to +voice. There is no change in binary feature for the vowel. So overall the BFEPP score is 0.5.

You can also get some simple help (example from Linux): 

```
java -cp phonemic-analysis-0.6.jar io.github.speechchemistry.BfeppForTwoWordsCli --help
Usage: BfeppForTwoWordsCli [-hV] [-f=<feature_framework>] word1 word2
      word1       Word 1 phonetic transcription in IPA (no tie bars)
      word2       Word 2 phonetic transcription in IPA (no tie bars). This
                    should be the reference transcription / ground-truth. If
                    there is no reference transcription then this should be the
                    longest transcription
  -f, --feature-framework=<feature_framework>
                  Feature framework to use: phoible (default) or hayes
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
```

## Known Issues

If your Unicode text files contain the UTF-8 preamble (also know as the UTF-8 BOM) it can sometimes cause problems with the software. 

## Acknowledgements

I'm grateful to :

- Bruce Hayes who gave me permission to use the data from [his feature spreadsheet](https://linguistics.ucla.edu/people/hayes/IP/#features)
- Dan Ellis who made [his Dynamic Time Warp (DTW) algorithm](https://www.ee.columbia.edu/~dpwe/resources/matlab/dtw/) available as open source
- Steven Moran and Daniel McCloy who produced the open source [Phoible](https://phoible.org/) data
