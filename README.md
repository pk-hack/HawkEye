## HawkEye

**HawkEye** is a program that creates a line-by-line diff of text between different releases or versions of video games. It was developed to support the creation of [Legends of Localization Book 2: EarthBound](http://www.legendsoflocalization.com/ebbook) by [Tomato](https://www.twitter.com/ClydeMandelin) and [Fangamer](https://www.fangamer.com). If you enjoy in-depth looks at video games and/or you're at all a fan of EarthBound, you'll find a lot to enjoy in this book.

[![IMAGE ALT TEXT HERE](http://img.youtube.com/vi/P_9c5ulgdG8/0.jpg)](http://www.youtube.com/watch?v=P_9c5ulgdG8)<br>
[*Click to watch the promo video for Legends of Localization Book 2: EarthBound*](http://www.youtube.com/watch?v=P_9c5ulgdG8)

Currently only the various versions of MOTHER 2, EarthBound, and MOTHER 1+2 are supported, but the program was designed to be extensible to other games in the future.

### How to Use

After building a Java Jar from the source code, it can be run like so:

```bash
java -jar HawkEye.jar -r roms/EarthBound_SNES.smc,roms/MOTHER_2_SFC.smc -o eb_vs_m2_comparison
```

The example above will generate a comparison between the two specified ROM files and output it to a directory named `eb_vs_m2_comparison` in the form of a full HTML website.

### Example output

Here's an sample of HawkEye's output. This comparison look at a specific NPC (#93) in MOTHER 2 and EarthBound.

| M2 (SFC)	    | EB (SNES)     |
| ------------- | ------------- |
| ＠ねぇ　きいてきいて<br>　わたし　「マザー２」<br>　エンディングまで　いったのよ。 | •Listen to this!  I finished EarthBound! |
| ＠「マザー２」って<br>　もう　はつばいになったのかな。<br>　ずいぶん　おくれてたけど……。 | •I wonder if “EarthBound” has been released yet. |

Note that in the example above, all of the NPC's possible text is present, even though this NPC has branching dialogue based on whether a certain event flag is set.
