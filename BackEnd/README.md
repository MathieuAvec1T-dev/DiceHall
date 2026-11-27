## Index

- [DiceHall Backend](#dicehall-backend)
    - [Getting Started](#getting-started)
        - [Prerequisites](#prerequisites)
        - [Database](#database)
        - [Start the API](#start-the-api)
        - [Swagger](#swagger)
- [Rule.operation Light Language (RoLL)](#ruleoperation-light-language-roll)
    - [Format](#format)
    - [Trigger](#trigger)
    - [Condition](#condition)
    - [Result](#result)
    - [Definitive](#definitive)
    - [Examples](#examples)
        - [Compare to a string](#compare-to-a-string)
        - [Compare to a number](#compare-to-a-number)
        - [Compare two stats](#compare-two-stats)
        - [Value with multiple spaces](#value-with-multiple-spaces)
        - [Percentage of a stat](#percentage-of-a-stat)
        - [Triggered by a roll](#triggered-by-a-roll)
        - [Roll with a criterion](#roll-with-a-criterion)
        - [Stat existence](#stat-existence)
        - [Chaining conditions](#chaining-conditions)
        - [Multiple chaining conditions](#multiple-chaining-conditions)
        - [Definitive result](#definitive-result)
    - [Coming Soon](#coming-soon)


# DiceHall Backend

## Getting Started

### Prerequisites

- JDK25
- MySQL 8.0

### Database

To set up your database:
- Create a database using the sql file in Database/
- Put your database url here in BackEnd/src/main/resources/application.properties: `spring.datasource.url=jdbc:mysql://[yoururl]:[yourport]/dicehall`
- In the same file change `spring.datasource.username=[yourUsername]` and `spring.datasource.password=[yourPassword]`
- Start your Database.

### Start the API

From the Backend folder, use the command:\
`./gradlew bootRun`

### Swagger

To open the Swagger simply use the url:\
http://localhost:8080/swagger-ui/index.html#/

<br><br>

# Rule.operation Light Language (RoLL)

## Format

[Trigger](#trigger) [Condition](#condition) : [Result](#result) [Definitive*](#definitive)  
\* (Optional)

## Trigger

`_BEFORE_` = When we send the stats to user and when the roll is done as long as the stat is part of the rule.  
`_ON_` = When a roll is launched on a stat that is part of the comparison only.

## Condition

Start with : a `#StatID` to compare or `ALL` or `NONE`

`ALL` and `NONE` are about stats the character possesses unless used with an `EXIST` or `NOTEXIST` comparison then it checks if character has `ALL` or `NONE` of the stats available in campaign.

Operator : `=` `!=` `<` `<=` `>=` `>` `EXIST` `!EXIST` `ROLLED` `ROLLED(comparator,StatCheckpointID)` `PERCENT(#statID,percentage)`

End with : a `#StatID` a number or a String  
`PERCENT(#statID,percentage)` Gives the percentage asked of the character's stat. (Example: for 50% of max health `PERCENT(#2,50)`).

`!EXIST` can be replaced by `NOTEXIST`.  
`ROLLED` doesn't need a second value after it. The result will trigger if the stat in the first part was rolled.  
`ROLLED(comparator,StatCheckpointID)` helps you check if the stat in front of this was rolled and if it corresponds to your criterion. Available comparators: `=` `!=` `<` `<=` `>=` `>`.

Multiple conditions can be chained using AND and OR. No parenthesis.

## Result

Start with : a `#StatID` to edit or `ALL`.  
Careful with `ALL` if a value isn't possible in one of the stat it will throw an error, use at your own risk.

Operator : `=` `-` `+` `*` `/`

End with : a `#StatID` a number or a String

## Definitive

End with : `_DEF_` if you want the result to be definitive aka stored in DB instead of just temporary.

## Examples

The stat names (Vie, Etat, Force...) are only examples. Use the stats of your own campaign.

| ID | Stat    |
|---|---------|
| `#1` | Vie     |
| `#2` | Vie max |
| `#3` | Etat    |
| `#4` | Force   |
| `#5` | Agilité |
| `#6` | Mana    |

### Compare to a string

`_BEFORE_ #3 = Blessé : #4 - 5`

If Etat is "Blessé", Force loses 5 points.

### Compare to a number

`_BEFORE_ #1 <= 10 : #4 - 2`

If Vie is 10 or less, Force loses 2 points.

### Compare two stats

`_BEFORE_ #1 < #5 : #4 = 0`

If Vie is lower than Agilité, Force is set to 0.

### Value with multiple spaces

`_BEFORE_ #3 = Très  blessé : #4 - 5`

Spaces between the elements are trimmed, but the spaces inside a value are kept. Here the value is "Très  blessé" (with two spaces).

### Percentage of a stat

`_BEFORE_ #1 < PERCENT(#2,50) : #4 - 3`

If Vie is below 50% of Vie max, Force loses 3 points.

### Triggered by a roll

`_ON_ #4 ROLLED : #5 + 1`

When a roll is made on Force, Agilité gains 1 point. `ROLLED` doesn't need a second value.

### Roll with a criterion

`_ON_ #4 ROLLED(<=,5) : #5 + 1`

When a roll is made on Force and it matches the criterion `<=` with the checkpoint `5`, Agilité gains 1 point.

`_ON_ #4 ROLLED(=,3) : #4 - 2`

Same syntax with another comparator. Available comparators: `=` `!=` `<` `<=` `>=` `>`.

`_BEFORE_ #4 ROLLED(>,2) AND #3 = Blessé : #5 - 1`

`ROLLED(...)` can be chained with other conditions using `AND` and `OR`, like any other condition.

### Stat existence

`_BEFORE_ #6 EXIST : #4 + 2`

If the character has the Mana stat, Force gains 2 points.

`_BEFORE_ #6 NOTEXIST : #4 - 2`

If the character doesn't have Mana, Force loses 2 points. `!EXIST` can be used instead of `NOTEXIST`.

`_BEFORE_ ALL EXIST : #4 + 1`

If the character has all the stats available in the campaign, Force gains 1 point.

`_BEFORE_ NONE EXIST : #4 = 0`

If the character has none of the stats available in the campaign, Force is set to 0. This is equivalent to `ALL !EXIST`.

### Chaining conditions

`_BEFORE_ #3 = Blessé AND #1 < 10 : #4 - 5`

Both conditions must be true.

`_BEFORE_ #3 = Blessé OR #3 = Empoisonné : #4 - 2`

At least one condition must be true. No parenthesis.

### Multiple chaining conditions

`_BEFORE_ #3 = Blessé AND #1 < 10 OR #3 = Empoisonné AND #1 < 20 : #4 - 5`

`AND` is evaluated before `OR`, like `&&` and `||` in Java. No parenthesis are allowed, so this is read as:

`(#3 = Blessé AND #1 < 10) OR (#3 = Empoisonné AND #1 < 20)`

Force loses 5 points if Etat is "Blessé" and Vie is below 10, or if Etat is "Empoisonné" and Vie is below 20.

Another one, with the `OR` in the middle:

`_BEFORE_ #3 = Blessé OR #3 = Empoisonné AND #1 < 5 : #4 = 0`

This is read as:

`#3 = Blessé OR (#3 = Empoisonné AND #1 < 5)`

Force is set to 0 if Etat is "Blessé" (whatever Vie is), or if Etat is "Empoisonné" and Vie is below 5.

To write "Empoisonné or Blessé, and Vie below 5", you can't use parenthesis, so repeat the second condition on each side:

`_BEFORE_ #3 = Blessé AND #1 < 5 OR #3 = Empoisonné AND #1 < 5 : #4 = 0`

### Definitive result

`_BEFORE_ #3 = Blessé : #4 - 5 _DEF_`

Same as the first example, but the result is stored in the DB instead of being temporary.

## Coming Soon

RANDOM(min, max) / Taking item stats into consideration