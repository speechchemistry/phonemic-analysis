# phonemic-analysis

This repository contains software related to my PhD "Machine-assisted phonemic analysis". One of the main tools is a phonetic distance measure; BFEPP (Binary Feature Edits Per Phone).

## Prerequisites

This tool requires Java to be already installed. For example, for Windows you could download Amazon Corretto 11 from https://corretto.aws/downloads/latest/amazon-corretto-11-x64-windows-jdk.msi (immediate download).

## Installation

Go to the [list of releases](https://github.com/speechchemistry/phonemic-analysis/releases). Under the latest release at the top of the screen, click on "Assets" then click on the jar file to download it. Alternatively, if you want to build from source, clone this repository and type `mvn package` (requires Apache Maven).

## Running the tools

Example for calculating the BFEPP measure between [to] and [po]. In Windows Powershell type:

`java "-Dfile.encoding=utf8" -jar phonemic-analysis-0.5.jar "t o" "p o"`

in linux you don't need to include the encoding option. You can also run the individual java classes. For example another way of running the BFEPP tool in Linux is: 

`java -cp phonemic-analysis-0.5.jar io.github.speechchemistry.BfeppForTwoWordsCli "t o" "p o"`

## Known Issues

If your Unicode text files contain the UTF-8 preamble (also know as the UTF-8 BOM) it can sometimes cause problems with the software. 

## Acknowledgements

I'm grateful to :

- Bruce Hayes who gave me permission to use the data from [his feature spreadsheet](https://linguistics.ucla.edu/people/hayes/IP/#features)
- Dan Ellis who made [his Dynamic Time Warp (DTW) algorithm](https://www.ee.columbia.edu/~dpwe/resources/matlab/dtw/) available as open source
- Steven Moran and Daniel McCloy who produced the open source [Phoible](https://phoible.org/) data
