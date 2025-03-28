# Content
* [About](#about)
* [Condition types](#condition-types)
  * [Number comparations](#number-comparations)
  * [Text operations](#text-operations)
  * [Permission](#permission)
* [Multiple condition requirements](#multiple-condition-requirements)
* [Condition output](#condition-output)
* [Configuration](#configuration)
* [Usage](#usage)
  * [Displaying text](#displaying-text)
  * [Display condition of a feature](#display-condition-of-a-feature)
    * [Short format](#short-format)
* [Refresh interval](#refresh-interval)
* [Examples](#examples)
  * [Example 1 - Chaining conditional placeholders](#example-1---chaining-conditional-placeholders)
  * [Example 2 - Conditions in conditions](#example-2---conditions-in-conditions)

# About
Conditions / conditional placeholders allow you
to create output which depends on the output of other placeholders or permission requirement.  
They have 2 main uses in the plugin:
* Display condition which must be met to be able to see something (bossbar, scoreboard, layout)
* Conditional placeholders which return defined outputs in both cases if the condition passes or fails

# Condition types
## Number comparations
| Operation         | Description         | Example |
|-------------------|---------------------|---------|
| `>=` | Greater than or equal to | `%ping%>=100` will pass if the player's ping is greater than or equal to `100` |
| `>` | Greater than | `%ping%>100` will pass if the player's ping is greater than `100` |
| `<=` | Less than or equal to | `%ping%<=100` will pass if the player's ping is less than or equal to `100` |
| `<` | Less than | `%ping%<100` will pass if the player's ping is less than `100` |

## Text operations
| Operation         | Description         | Example |
|-------------------|---------------------|---------|
| `=` | Equal to | `%world%=world` will pass if player is in world `world` |
| `!=` | Not equal to | `%world%!=world` will pass if player is in any world except `world` |
| `<-` | Contains (left side for full text, right side text to contain) | `%world%<-lobby-` will pass if player is in any world that contains `lobby-` (such as `lobby-1` etc.) |
| `\|-` | Starts with (left side for full text, right side text to start with) | `%world%\|-lobby-` will pass if player is in any world that starts with `lobby-` (such as `lobby-1` etc.) |
| `-\|` | Ends with (left side for full text, right side text to end with) | `%world%-\|nether` will pass if player is in any world that ends with `nether` (such as `world_nether` etc.) |

> [!NOTE]
> For `=` and `!=` you can check for empty output of a placeholder using `%my_placeholder%=` and `%my_placeholder%!=`.

> [!NOTE]
> For string operations, the text must match placeholder's output exactly, including color codes.
If you are using [Placeholder output replacements](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Placeholder-output-replacements),
condition must contain the altered output.  
To see the exact output of a placeholder including color codes, use `/tab parse <player> <placeholder>`.

## Permission
| Operation         | Description         | Example |
|-------------------|---------------------|---------|
| `permission:<value>` | Permission requirement | `permission:my.permission` will pass if player has `my.permission` permission |

# Multiple condition requirements
Each condition has a `conditions` parameter, which a list of conditions. If you define more than 1 condition, you must specify the condition type.  
This value can be found under `type` field. Types are:
* `AND` - all sub-conditions must be met for the final condition to pass
* `OR` - at least one sub-condition must be met for the final condition to pass

If you only defined one subcondition, you don't need to define the type at all, since it's not used for anything.

# Condition output
If using condition as a placeholder, you can specify output in both cases using `yes` and `no` values. `yes` is used when condition passes, `no` if not. If using condition only as a view requirement, you can leave these values empty / not specify them at all.

# Configuration
Open **config.yml** and find this section:
```
conditions:
  health:
    conditions:
    - '%health%<21'
    - '%health%>15'
    type: AND
    yes: Healthy!
    no: Damaged!
```
`health` is name of our condition in this case.  
`conditions` is a list of subconditions that must be met for this condition to pass.  
`type` defines whether all subconditions must be met or at least one.  
`yes` & `no` define output in both cases.

# Usage
You have 2 ways to use conditions.

## Displaying text
The first way is to use conditions to display text.
Configure outputs in `yes` and `no` values and then use `%condition:<name>%`,
which will output text defined in `yes` or `no` depending on if condition is met or not.
<details>
  <summary>Example</summary>

```
conditions:
  serverName:
    conditions:
      - "%server%=lobby"
    yes: "You are in the lobby"
    no: "You are not in lobby"
```

Use with `%condition:serverName%`
</details>

## Display condition of a feature
The second way is to use condition's name in places where a condition is accepted.
This includes display conditions for bossbar, scoreboard and layout.
In these cases, yes/no texts are unused; therefore, they do not need to be defined.
<details>
  <summary>Example</summary>

```
conditions:
  MyCondition:
    conditions:
      - "permission:tab.admin"
```
```
scoreboards:
  MyScoreboard:
    display-condition: MyCondition
```

In this example, scoreboard will only be displayed to players with `tab.admin` permission.
</details>

### Short format
If trying to use a condition on place where it's available (bossbar display condition, scoreboard display condition) where you don't need the yes/no values, you can use a short format instead.

This can be used by simply creating all subconditions and separating them with `;` for `AND` condition type. For `OR` type, use `|`.
<details>
  <summary>Example</summary>

Single condition:
```
display-condition: "%server%=lobby"
```

Multiple conditions, all of them must be met:
```
display-condition: "%server%=lobby;%world%=world"
```

Multiple conditions, at least one of them must be met:
```
display-condition: "%server%=lobby|%server%=lobby2"
```

</details>

# Refresh interval
Conditions are just placeholders after all, and, as such, they must be refreshed periodically.
Refresh intervals of conditions are not directly configurable.
They are based on placeholders used inside (subconditions, yes/no values).  
Permission checks count as 1000ms.  
To configure refresh intervals of placeholders,
check out the [Optimization guide](https://github.com/NEZNAMY/TAB/wiki/Optimizing-the-plugin#2---all-platforms-placeholder-refresh-intervals).

# Examples
## Example 1 - Chaining conditional placeholders
Let's create colored ping by chaining conditions using multiple intervals for ping quality. This specific case would be easier done using [replacements](https://github.com/NEZNAMY/TAB/wiki/Feature-guide:-Placeholder-output-replacements), but this is just an example. Let's say we want <50 for good, <100 for medium and 100+ for bad. Let's create the first condition:
```
conditions:
  ping:
    conditions:
    - "%ping%<50"
    true: "&a%ping%"
    false: "%condition:ping2%"
```
When ping is <50, display it as green. Otherwise, use another condition. Put them together and we get
```
conditions:
  ping:
    conditions:
    - "%ping%<50"
    true: "&a%ping%" # Ping is < 50, display as green
    false: "%condition:ping2%" # Ping is 50+, check another condition
  ping2:
    conditions:
    - "%ping%<100"
    true: "&e%ping%" # Ping is < 100, display as yellow
    false: "&c%ping%" # Ping is 100+, display as red
```
Finally, we can use this ping placeholder using `%condition:ping%`.  
This example chained 2 conditions, but more can be used. There is no limit.

# Example 2 - Conditions in conditions
If you want to use a condition in another one,
for example, to use both "AND" and "OR" types, create 2 conditions and use one in the other one.  
For example, if we want to check that player is in server `lobby` **and** in worlds **either** `world1` or `world2`,
it can be achieved in the following way:
```
conditions:
  world:
    conditions:
      - "%world%=world1"
      - "%world%=world2"
    type: OR
  main:
    conditions:
      - "%condition:world%=true"
      - "%server%=lobby"
    type: AND
```
Then, use condition `main` as the display condition. Note that `yes`/`no` values were not defined, as such, they default to `true` and `false`, respectively. Therefore, we use the placeholder from the condition and check if the result is `true`. Then, check if player is also in the specified server.  