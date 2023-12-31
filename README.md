# Copy Commit Reference

[![Build](https://github.com/rybak/intellij-copy-commit-reference/workflows/Build/badge.svg)][GitHubActions]
[![Version](https://img.shields.io/jetbrains/plugin/v/22138-copy-commit-reference.svg)][Marketplace]
[![Downloads](https://img.shields.io/jetbrains/plugin/d/22138-copy-commit-reference.svg)][Marketplace]

Plugin for IntelliJ-based IDEs.

<!-- Plugin description -->
Provides a context menu item "Copy Commit Reference" in all VCS log views. It copies a reference to the commit in
the [same format as `git log --format=reference`](https://git-scm.com/docs/git-log#_pretty_formats).

This format is used to refer to another commit in a commit message.  For example:
[commit `1f0fc1d (pretty: implement 'reference' format, 2019-11-20)`](https://github.com/git/git/commit/1f0fc1db8599f87520494ca4f0e3c1b6fabdf997)
in the git.git repository.
<!-- Plugin description end -->

![Screenshot of changed context menu in tab "Log"](images/tab-log.png "How the menu looks like in tab 'Log'")

Source code of the plugin is distributed under the terms of the MIT Licence.
See [LICENSE.txt](LICENSE.txt) for details.

## Installation

### Using IDE built-in plugin system

#### Stable version

<kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Copy Commit Reference"</kbd> >
<kbd>Install</kbd>

#### Alpha version

1. Add alpha channel as a [Custom plugin repository][CustomPluginRepository].
   - Use `https://plugins.jetbrains.com/plugins/list?channel=alpha&pluginId=dev.andrybak.intellij.copy_commit_reference`
     as the repository URL.
   - You can also use `https://plugins.jetbrains.com/plugins/alpha/list` if you
     would like to get _all_ plugins available in the `alpha` channel.
2. Install the plugin through the Marketplace search as usual.
  
### Manually

Download the latest release from [GitHub][GitHubLatestRelease] or [JetBrains Marketplace][MarketplaceVersions]
and install it manually using
<kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Building from source

1. Run Gradle task `buildPlugin`:
   ```
   ./gradlew buildPlugin
   ```
2. The plugin bundle will be available in directory `build/distributions/`.

## Debugging from IntelliJ IDEA

1. Open the project in IntelliJ IDEA
2. Launch run configuration "Run IDE for UI Tests"

## Plugin TODO list
See file [TODO.md on branch `todo`][TODO]

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[CustomPluginRepository]: https://www.jetbrains.com/help/idea/managing-plugins.html#repos
[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[AnnotateToggleAction]: https://github.com/JetBrains/intellij-community/blob/master/platform/vcs-impl/src/com/intellij/openapi/vcs/actions/AnnotateToggleAction.java#L199-L202
[GitHubActions]: https://github.com/rybak/intellij-copy-commit-reference/actions
[GitHubLatestRelease]: https://github.com/rybak/intellij-copy-commit-reference/releases/latest
[Marketplace]: https://plugins.jetbrains.com/plugin/22138-copy-commit-reference
[MarketplaceVersions]: https://plugins.jetbrains.com/plugin/22138-copy-commit-reference/versions
[TODO]: https://github.com/rybak/intellij-copy-commit-reference/blob/todo/TODO.md
