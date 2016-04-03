Essential Slick Exercises
=========================

Exercises and solutions to accompany [Essential Slick][essential-slick].

Copyright 2016 [Underscore Consulting LLP][underscore].
Exercise code licensed [Apache 2.0][license].

# Quick Start

Follow the instructions below to get set up.
You will need a Java 8 compatible JVM and a familiar programmer's text editor or IDE.
If you have any problems please let me know on our [Gitter channel][gitter].

1. Clone this repo and switch to the root directory:

    ~~~ bash
    $ git clone https://github.com/underscoreio/essential-slick-code.git

    $ cd essential-slick
    ~~~

2. Run SBT:

    ~~~ bash
    $ ./sbt.sh # ".\sbt.bat" on Windows
    ~~~

3. Compile and run the example "helloworld.Main" application.
   This will take a few minutes to run the first time.
   You'll need an internet connection to download dependencies:

   ~~~ bash
   sbt> runMain helloworld.Main
   ~~~

4. If you see a list of albums similar to the following, you're good:

    ~~~
    Album(Keyboard Cat,Keyboard Cat's Greatest Hits,1)
    Album(Spice Girls,Spice,2)
    Album(Rick Astley,Whenever You Need Somebody,3)
    Album(Manowar,The Triumph of Steel,4)
    Album(Justin Bieber,Believe,5)
    ~~~

   If not, let me know on the [Gitter channel][gitter].

5. If you use an IDE that requires further setup, do that now.
   I've included the `sbteclipse` and `ensime-sbt` plugins in the build.

[essential-slick]: http://underscore.io/books/essential-slick
[underscore]: http://underscore.io
[license]: http://www.apache.org/licenses/LICENSE-2.0
[gitter]: https://gitter.im/underscoreio/scala
