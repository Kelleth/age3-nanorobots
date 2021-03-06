# AgE 3

AgE 3 is a new version and complete rewrite of the distributed agent-based computational platform
[AgE](https://www.age.agh.edu.pl/).

**This is beta software** and is still under heavy development. Although this version is released, it is only
a *snapshot* of a current development branch and there is no guarantee that future version will provide the same
functionality or API.

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

## Links

* [Maven repository](https://repository.age.agh.edu.pl/content/groups/public/)
* [Javadocs](https://www.age.agh.edu.pl/docs/v0.1/javadoc/)