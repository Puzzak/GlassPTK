# Glass PTK

This is a port of my [PTK App](https://github.com/Puzzaks/PTK) for the Google Glass XE. It is not finished and probably won't be as developing for Glass is extremely hard and requires more knowledge than I ever had. With that out of the way, here's a short resume of this app.

---

### Features

 - **Telemetry**: Displays a card with server telemetry, containing:
    - Network Speed
    - Ping
    - CPU load
    - Memory utilization and free memory
    - CPU Temperature
    - Server uptime
 - **Glass SDK** is used for this app
 - **Integration** into the system and support for voice commands

#### Telemetry
Using [https://github.com/Puzzak/AIO-Monitor](AIO Script) the server can report it's telemetry to any client that can utilize it's data. Data is encoded as a JSON object and is received using regular GET request. While you can update it at whatever interval you want, both PTK and Glass PTK are updating once per second. This is to ensure close to realtime interaction.
Card contains