## HawkEye
[![Build Status](https://travis-ci.org/mrtenda/HawkEye.svg?branch=master)](https://travis-ci.org/mrtenda/HawkEye) [![Coverage Status](https://codecov.io/gh/mrtenda/HawkEye/branch/master/graph/badge.svg)](https://codecov.io/gh/mrtenda/HawkEye)


**HawkEye** is a program that creates a line-by-line diff of text between different releases or versions of video games. It was developed to support the creation of [Legends of Localization Book 2: EarthBound](http://www.legendsoflocalization.com/ebbook) by [Tomato](https://www.twitter.com/ClydeMandelin) and [Fangamer](https://www.fangamer.com). If you enjoy in-depth looks at video games and/or you're at all a fan of EarthBound, you'll find a lot to enjoy in this book.

[![Video Thumbnail](http://img.youtube.com/vi/bv6tmRkhJhI/0.jpg)](http://www.youtube.com/watch?v=bv6tmRkhJhI)<br>
[*Click to watch the promo video for Legends of Localization Book 2: EarthBound*](http://www.youtube.com/watch?v=bv6tmRkhJhI)

Currently only the various versions of MOTHER 2, EarthBound, and MOTHER 1+2 are supported, but the program was designed to be extensible to other games in the future.

### How It Works

HawkEye first reads the game script data from the ROM into a graph where each node represents a specific script operation, whether it be to GOTO another place script or to display the letter "A". This graph of raw script operations is then converted into a graph where each node represents a line of text. Graphs of lines of text for corresponding NPCs (or other text sources) are then compared with eachother, matching lines of text against each other that occupy identical locations in the graph. If the two graphs of lines of text are not isomorphic, then unmatched raw listings of lines of text for each game are outputted.

For example, let's say we want to perform an analysis of NPC #93 from EarthBound. First, the program has to determine that this NPC's script data is located at $C731F0 in the ROM. This script data also references more script data at $C73217. The data in these locations is shown below.

```
$C731F0 : 06 49 00 17 32 c7 00 70 15 8f 99 96 50 6c 75 91
          a2 a4 98 72 17 69 6e 15 84 92 17 7a a2 95 16 a2
          94 17 aa a4 5e 13 02
          
$C73217 : 70 7c 17 56 9e 15 90 16 14 51 10 14 16 93 17 c0
          17 c8 15 57 75 91 a2 a4 98 72 17 69 51 13 02
```

These bytes are read and converted into a graph of the script operations that these bytes represent. The graph may look something like this (ellipses used here to make the image a resonable size):

![Control Code Graph for EarthBound NPC #93](https://cloud.githubusercontent.com/assets/1281326/21471324/3c3615f8-ca63-11e6-98b8-86b611730d6f.png)

In the above graph, each node represents an operation in the script. These operations are specific to the scripting language of the game. The next step is to convert this graph into a graph of lines of dialogue which isn't game-specific:

![Dialogue Graph for EarthBound NPC #93](https://cloud.githubusercontent.com/assets/1281326/21471302/318ad144-ca62-11e6-8d98-45d0bd504fe7.png)

The same process described above can also be performed on MOTHER 2, the original Japanese version of EarthBound, resulting in a similar graph:

![Dialogue Graph for MOTHER 2 NPC #93](https://cloud.githubusercontent.com/assets/1281326/21471308/9a9463a8-ca62-11e6-9315-124c8746bfbb.png)

The above two graphs are isomorphic, so the lines of text can be matched against each other by matching corresponding nodes in the two graphs with eachother. Doing so produces the final desired output, a side-by-side diff of the text between the two games:

| MOTHER 2 (SFC)| EarthBound (SNES) |
| ------------- | ------------- |
| ＠ねぇ　きいてきいて<br>　わたし　「マザー２」<br>　エンディングまで　いったのよ。 | •Listen to this!  I finished EarthBound! |
| ＠「マザー２」って<br>　もう　はつばいになったのかな。<br>　ずいぶん　おくれてたけど……。 | •I wonder if “EarthBound” has been released yet. |

In the case that the graphs being compared are not isomorphic, then an unmatched listing of lines is outputted instead.

These side-by-side comparisons are then used to build a full HTML website that contains side-by-side diffs of text for all lines in the entire game using the program's builtin HTML templates.

### How to Use

After building a Java JAR from the source code, it can be run like so:

```bash
java -jar HawkEye.jar -r roms/EarthBound_SNES.smc,roms/MOTHER_2_SFC.smc -o eb_vs_m2_comparison
```

The example above will generate a comparison between the two specified ROM files and output it to a directory named `eb_vs_m2_comparison` in the form of a full HTML website.

