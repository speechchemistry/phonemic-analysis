# phonemic-analysis

This repository contains software related to my PhD "Machine-assisted phonemic analysis". One of the main tools is a phonetic distance measure; BFEPP (Binary Feature Edits Per Phone).

## Prerequisites

This tool requires Java runtime which is already installed on many systems. 

## Installation

Download the release zip package that includes jar file. 

## Running the tools

Example for calculating the BFEPP measure between [to] and [po]:

`java -cp target/phonemic-analysis-0.4.jar io.github.speechchemistry.BfeppForTwoWordsCli "t o" "p o"`

Note that in windows power shell you need to force it to use UTF-8 encoding e.g.

`java "-Dfile.encoding=utf8" -cp target/phonemic-analysis-0.4.jar io.github.speechchemistry.BfeppForTwoWordsCli "t o" "p o"`

## Known Issues

If your Unicode text files contain the UTF-8 preamble (also know as the UTF-8 BOM) it can sometimes cause problems with the software. 
