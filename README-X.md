<!-- This file is rendered by https://github.com/BlvckBytes/readme_helper -->

# HeadCatalog

A simple-to-use catalog for browsing through massive amounts of heads in an efficient manner.

![Pagination Screen](readme_images/pagination.png)

![Live Browser](readme_images/live_browser.png)

<!-- #toc -->

## Configuration File

The configuration file makes use of [BukkitEvaluable](https://github.com/BlvckBytes/BukkitEvaluable), so it is advised
to also get familiar with the *README* of that project in order to fully understand the process of customizing the plugin.

### Section "command"

See [BukkitCommands](https://github.com/BlvckBytes/BukkitCommands).

### Section "permissions"

See [BukkitEvaluable](https://github.com/BlvckBytes/BukkitEvaluable).

| Internal Name | Description                                       |
|---------------|---------------------------------------------------|
| open          | Open the UI                                       |
| request       | Request a head by clicking on it                  |
| priceBypass   | Bypass the price of a head and never pay anything |

### Section "messages"

#### headsNotReadyYet

The heads are still loading, which either means that the persistence is not done reading yet or that
the process of mapping head models to head representative items has not yet completed.

#### inventoryFull

The requested head item could not be placed in the player's inventory, as it has no more space to hold it.

#### requestedHeadPriceBypassed

Printed after requesting a head if the player has the `priceBypass` permission and thus didn't have to pay money.

| Environment Variable | Description                   |
|----------------------|-------------------------------|
| head                 | The [head model](#head-model) |

#### requestedHead

Printed after requesting a head which was either free (price = 0) or which cost some amount of money.

| Environment Variable | Description                   |
|----------------------|-------------------------------|
| head                 | The [head model](#head-model) |

#### missingBalance

The player is missing some amount of balance in order to purchase the selected head.

| Environment Variable | Description                       |
|----------------------|-----------------------------------|
| balance              | The current balance of the player |
| head                 | The [head model](#head-model)     |

#### economyError

An error within the economy system occurred.

| Environment Variable | Description                                  |
|----------------------|----------------------------------------------|
| error_message        | Error message provided by the economy system |
| head                 | The [head model](#head-model)                |

### Head Model

The head model is sometimes passed as an environment variable to certain config properties, and contains
the following accessible members:

| Member     | Type        | Description                                   |
|------------|-------------|-----------------------------------------------|
| name       | String      | Name of the head                              |
| skinUrl    | String      | URL of the head's skin                        |
| uuid       | UUID        | UUID corresponding to this head               |
| categories | Set<String> | The categories this head is in                |
| tags       | Set<String> | The tags which have been added to this head   |
| price      | Double      | The price of the head                         |
| lastUpdate | Date        | Last update or (if not updated) creation date |