![CircleCI Build Status](https://circleci.com/gh/kovalevvlad/chaos-csv.png?style=shield&circle-token=6c77497a4f2cb91cba03e55499353fe2069df573 "CircleCI Build Status")

# Chaos CSV Writer - Writing CSVs Responsibly

### Motivation
Have you ever had a production dependency on data delivered via a CSV? How many times have your production
processes broken because you were using a half-baked CSV reader which could not handle commas or
escaped double quotes inside CSV data cells? *For more gotchas check
[this article](https://tburette.github.io/blog/2014/05/25/so-you-want-to-write-your-own-CSV-code/).*

Chaos CSV has been created to address this problem by generating valid CSV files which are unreadable with half-baked
CSV readers.

*Would you not be happy if all of the CSVs that your read were written with this approach?*

### Assumptions
It is assumed that you want to write CSV files *with* headers and all lines contain the same number of data cells.

### Implementation

#### Ensuring Ability to Handle Quotes, Commas, New Lines in Data Cells
The writer ensures that various special characters inside data cells are handled correctly by creating a dummy
column containing a random number of these characters, ensuring that at least one of each special character is present
per file.

#### Ensuring Clients do not Hardcode Column Integer Index in their Code
The chaos writer randomly permutes columns when saving a CSV. This forces clients to rely on labels defined in the 
header to read the data correctly. This holds even for CSVs with a single data column because a dummy column is
always added.
