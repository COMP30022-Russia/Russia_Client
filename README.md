# Russia Client


## Prerequisite
 - Android Studio

## Building

### From within Android Studio
 - Clone this repository locally.
 - In Android Studio, select menu item `File -> Open...` and open the root of your local repository.
 - Android Studio will prompt you to install missing SDKs. On Windows, you will need to run Android Studio as Administrator to install SDKs.
   ![](docs/android-studio-install-sdk.png)

 - To build and run the app, select menu item `Run -> Run` and then in the popup dialog, select `app`.
 
### From command line (Linux)
TBD

## Running Tests
There are two types of tests: 

1. Unit tests, located in `src/test/java/com/comp30022/team_russia/assist`

1. Android Instrumented Tests, located in `src/androidTest/java/com/comp30022/team_russia/assist` (Runs on a physical phone or an Android emulator.)

### In Android Studio
To run either type of tests, right click on the corresponding folder in the "Project" panel, then select `Run 'Tests in 'assist''`.

![](docs/android-studio-run-tests.png)