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

### Updates

##### V1.1
- The storage directory has been changed from `clanchatlogs` to `chatlogs`
- The plugin is now capable of logging public & private chat channels. Each chat channel has its own directory inside the `chatlogs` directory 
- Each individual chat channel logging can be toggled on/off
