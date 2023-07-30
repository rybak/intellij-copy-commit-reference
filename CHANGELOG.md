<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Copy Commit Reference Changelog

## [Unreleased]

### Added
- Added "Commit Commit Reference" action to the context menu of VCS annotations.

## [1.0.0] - 2023-07-10

### Added
- If a commit's subject line contains mentions of issues, which are configured for [navigation in the project](https://www.jetbrains.com/help/idea/settings-version-control-issue-navigation.html), then the reference pasted into a rich text editor will include clickable links.
- [LICENSE.txt](https://github.com/rybak/intellij-copy-commit-reference/blob/main/LICENSE.txt) is now included as part of the plugin's jar file.

### Fixed
- Warning caused by incorrect usage of `com.intellij.openapi.progress.ProgressIndicator` has been fixed.

## [0.3.0-alpha] - 2023-07-07

### Added
- Installation instructions for alpha versions have been added to the
  [README](https://github.com/rybak/intellij-copy-commit-reference/blob/main/README.md).
- "What's New" tab for the plugin now includes a link to the full changelog.

### Changed
- Example of a commit reference has been added to plugin's public description.
- Plugin's compatibility has been expanded to include version 2020.3 and later versions.
- Layout of the changelog in the "What's New" tab for the plugin has been improved.

## [0.2.0-alpha] - 2023-06-29

### Changed
- Plugin's compatibility has been expanded.
- Plugin's artifacts are now [signed](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html).
- Versioning scheme for the alpha releases has been simplified.

## [0.1.0-alpha.1] - 2023-06-25

### Added
- Support for "Copy Commit Reference" action in the following context menus:
  - Tool window "Git":
    - Tab "Log"
    - File history tabs
  - Popup dialog "History for Selection"
- Support for "Copy Commit Reference" action as a shortcut in the keymap

[Unreleased]: https://github.com/rybak/intellij-copy-commit-reference/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/rybak/intellij-copy-commit-reference/compare/v0.3.0-alpha...v1.0.0
[0.3.0-alpha]: https://github.com/rybak/intellij-copy-commit-reference/compare/v0.2.0-alpha...v0.3.0-alpha
[0.2.0-alpha]: https://github.com/rybak/intellij-copy-commit-reference/compare/v0.1.0-alpha.1...v0.2.0-alpha
[0.1.0-alpha.1]: https://github.com/rybak/intellij-copy-commit-reference/commits/v0.1.0-alpha.1
