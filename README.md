# Advent of Code 🎄

Solutions for [Advent of Code](https://adventofcode.com)
in [Kotlin](https://kotlinlang.org).

Got 50 ⭐️ every year since 2023.
And also 23 ⭐️ in 2019 for all [IntCode](https://adventofcode.com/2019/day/2) days.

Features:
*   automatic input file downloading
*   ability to automatically submit an answer right after getting it
    ```
    part1, real: 737 ⭕ (unchecked)
    Submit? [yes/no]
    yes
    > That's the right answer!
    > You are one gold star closer to saving your vacation.
    ```
*   comparison of answers with the already submitted ones
    ```
    part1, real: 1737647416 🔴 (expected 14622549304)
    part2, real: 1735 🟢
    ```
*   declaration of examples with expected answers
    ```kotlin
    example("tiny") {
        answer1(4)
        answer2(13)
        """
            ..90..9
            ...1.98
            ...2..7
            6543456
            765.987
            876....
            987....
        """
    }
    ```
    ```
    part1, example tiny: 4 🟢
    part1, real: 746 🟢
    part2, example tiny: 13 🟢
    part2, real: 1541 
    ```
*   silly performance measurements
    ```
    part1, real: 14622549304 🟢 (took 28, 20, 18, 18, 15, 19, 15, 18, 34, 19, 21 ms)
    part2, real: 1735 🟢 (took 716, 694, 701, 651, 617, 618, 618, 654, 588, 609, 611 ms)
    ```
*   IDEA file template for new solutions
