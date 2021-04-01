# Chat Logger
This plugin allows logging chat messages from different channels to individual files. Currently, the plugin supports public, private and clan channels and each can be toggled on/off individually.
The log file rotation happens on a daily basis and up to 7 log files are kept.

The logs can be found at RuneLite's home folder under the `chatlogs` directory. To find runelite's home navigate to `%userprofile%\.runelite` on Windows or `$HOME/.runelite` on Linux and macOS.

### Directory structure
The plugin uses the following directory structure:
```
.runelite/
└── chatlogs/
    ├── friends/
    ├── private/
    └── public/
```

Note that the **friends** folder contains the **clan chat** logs and the **private** folder contains **private messages** from friends.

### Remote Submission

This plugins allows you to configure submission of your clan chat messages to a remote endpoint. To enable it the user must configure an endpoint and tick the box on the plugin's configuration.

#### Submitted payload & behavior

#### Behavior

The plugin submits chat messages every 5 seconds, multiple chat entries can be submitted at once up to a max of 30 entries.
It also uses a circuit breaker to avoid making requests to a non-functional endpoint.
The circuit breaker opens if more than 3 requests fail within 30 seconds and will switch to half-open after 5 minutes.

#### Payload structure

The plugin uses the following structure to submit messages:

```json
[
  {
    "timestamp": "2021-01-01T00:00:00.000000000Z",
    "friendChat": "player name",
    "sender": "player name",
    "message": "Dasdasd"
  }
]
```

| field | description |
| --- | --- |
| timestamp | An ISO-8601 compatible timestamp of when the message was received by the client |
| friendChat | The name of the friend chat owner that the player is in |
| sender | The player that sent the message |
| message | The message |

### Updates

##### V1.2
- Added remote submission of friends chat messages

##### V1.1
- The storage directory has been changed from `clanchatlogs` to `chatlogs`
- The plugin is now capable of logging public & private chat channels. Each chat channel has its own directory inside the `chatlogs` directory 
- Each individual chat channel logging can be toggled on/off
