## To Do

- Bump versions of GitHub actions
- Bump dependency versions
    > See WIP branch upgrade-dependencies
- Refactor release process
    * [ ] GIT-VERSION convention
    * [ ] Reorder actions to make releases from tags with release notes already updated.
- Figure out GitHub Actions
    * [ ] How does pre-installed temurin Java work? Do we still need the Java setup step?
    * [ ] TBD

## Doing

- Create branch dev-1
    > Versions 1.* will be capped for compatibility. See git commit messages for until-build and other compatibility changes.
    * [x] Wait for 2.0.0 to get released
    * [ ] Name variants: release-1, release-v1, dev-1
- Release process instructions
    > https://github.com/rybak/intellij-copy-commit-reference/issues/5
    * [ ] Review the draft during next release

## Done

- Gif in README
    > Rejected as being too distracting
    * [x] Decide if README with a gif is better than with static screenshot
    * [ ] If gif is better, push it to `main`
- Building instructions
    > https://github.com/rybak/intellij-copy-commit-reference/issues/5
    * [x] double check building from source instructions
    * [x] double check debugging from IDE instructions
    * [x] push to `main`
- 2.0.0
    * [x] Rebase onto master
    * [x] Integrate changes from all branches related to 2.0.0 (two local branches atm)
    * [x] Test changes
    * [x] Copyedit the CHANGELOG
    * [x] Release 2.0.0
    * [x] Move on to create branch dev-1 for 1.*.*
