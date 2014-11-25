# AgE 3

AgE 3 is a new version and complete rewrite of the distributed agent-based computational platform
[AgE](https://www.age.agh.edu.pl/).

## Quickstart

To run the computational node, use Gradle Wrapper provided in the repo.
On Linux:

```
./gradlew node
```

Or on Windows:

```
gradlew.bat node
```

It will automatically download an appropriate Gradle version, compile the project and start the node.
You can run multiple nodes from the same directory on a single computer or multiple computers in a single local network.
Remember to turn off or configure the firewall first!

To run the console node (that provides a basic CLI for the AgE) just run:
On Linux:

```
./gradlew console
```

Or on Windows:

```
gradlew.bat console
```
