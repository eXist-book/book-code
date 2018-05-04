# eXist Book Example Code

[![Build Status](https://travis-ci.org/eXist-book/book-code.png?branch=master)](https://travis-ci.org/eXist-book/book-code)
[![Java 7](https://img.shields.io/badge/java-7-blue.svg)](http://java.oracle.com)
[![License](https://img.shields.io/badge/license-BSD%203-blue.svg)](http://www.opensource.org/licenses/BSD-3-Clause)

This repository contains all (except the [Using eXist 101 chapter](https://github.com/eXist-book/using-exist-101)) of the
code and examples discussed in the [eXist book](http://shop.oreilly.com/product/0636920026525.do) published by O'Reilly.

This version contains code compatible with eXist-db 2.1 which was the latest version at the time the book was authored. Versions for eXist-db [3.0.RC1](https://github.com/eXist-book/book-code/tree/eXist-3.0.RC1), [3.0](https://github.com/eXist-book/book-code/tree/eXist-3.0), and [4.0.0](https://github.com/eXist-book/book-code/tree/eXist-4.0.0) are also available.

The repository has the following layout:

* [chapters/](https://github.com/eXist-book/book-code/tree/master/chapters)
  Under this folder each chapter of the book that has example code is represented.

* [xml-examples-xar/](https://github.com/eXist-book/book-code/tree/master/xml-examples-xar)
  These are the files needed to build an EXPath Package XAR from other files distributed
  in the chapters folders. In particular the content of the XAR package is assembled using
  the the `fileSet`s set out in the assembly [xml-examples-xar/expath-pkg.assembly.xml](https://github.com/eXist-book/book-code/blob/master/xml-examples-xar/expath-pkg.assembly.xml)

All other files are related to the Maven build process.

Building
========
The EXPath Package XAR and the Java projects are all built using Apache Maven. You will need to have Git and at least Maven 3.1.1
installed. Git can be downloaded and installed from http://git-scm.com and you can download and install Maven from http://maven.apache.org.

Once you have Maven installed you can simply run the following from your Unix/Linux/Mac terminal or Windows command prompt:
```bash
git clone https://github.com/eXist-book/book-code.git
cd book-code
mvn clean install
```

You should then find the EXPath PKG XAR located at `xml-examples-xar/target/exist-book.xar`. The Java projects artifacts will be located within the `target` sub-folders
of each Java project respectively.
