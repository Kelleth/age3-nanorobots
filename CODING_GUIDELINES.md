# Coding Guidelines for AgE3

We base our guidelines loosely on [Google Java Guidelines](https://google-styleguide.googlecode.com/svn/trunk/javaguide.html).
Notes below are extensions to them.

## Formatting

* We use tabs for indentation.
* No missing braces - even one-liners should have braces.
* Annotations for fields should be placed in the same line.
* Annotations for methods - chopped down if more than one or two.

## Naming conventions

* For read-only values we drop "get" from method names.

## Annotations

### Nullability

* All fields (non-final, not primitive), methods (non-void, not primitive) and method parameters (not primitive) should
  be annotated. Usually with one of these:
  * org.checkerframework.checker.nullness.qual.MonotonicNonNull,
  * org.checkerframework.checker.nullness.qual.NonNull,
  * org.checkerframework.checker.nullness.qual.Nullable;
* Sometimes it’s worthy to annotate local variables and generics too.
* Nullability annotations should be placed in position closest to the type (i.e. `@Override @NonNull` and not `@NonNull @Override`).
* **Immutable**, **ThreadSafe** and **GuardedBy** are recommended when a class has required properties.
* We use annotations from `org.checkerframework.checker` if possible.
* Other annotations from Checker Framework are encouraged.

## Assertions

* Fail fast: use as much precondition methods (`requireNonNull`, `checkArgument`, `checkState`, etc.) as possible
  (in public methods).
* For non-public methods use assertions (possibly with description). Assertions are enabled by default in Gradle
  configuration and should always be.
