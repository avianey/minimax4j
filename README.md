# Minimax4j

Minimax4j is a JAVA implementation of the [minimax algorithm](http://en.wikipedia.org/wiki/Minimax).  
It is a simple but complete abstract API that let you concentrate on the evaluation function.

```xml
<dependency>
    <groupId>fr.avianey</groupId>
    <artifactId>minimax4j</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Samples

Samples are available in the minimax-sample project and provide basic implementations for the following IA :
* Tic-Tac-Toe

## Documentation

The [Minimax4j javadoc](http://avianey.github.com/minimax4j/) is available on the github project page.

## Licence

```
The MIT License (MIT)

Copyright (c) 2015 Antoine Vianey

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Contribute

Feel free to contribute by sending pull requests.  
Abstract mechanism that would likely fit into the API are :
* Transposition table
* Quiescence search
* Opening table

## Contributors

* [@avianey](https://github.com/avianey) : creator

## Who's using it ?

List of projects using minimax4j :
* [Reversi](https://play.google.com/store/apps/details?id=net.androgames.reversi) for Android on Google play ([@avianey](https://github.com/avianey))
* [Gomoku](https://github.com/makaw/gomoku) for desktop - Java/Swing ([@makaw](https://github.com/makaw))

Fork the project and send a pull request to add your application in this list.
