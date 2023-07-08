import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
	id("java") // Java support
	alias(libs.plugins.kotlin) // Kotlin support
	alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
	alias(libs.plugins.changelog) // Gradle Changelog Plugin
	alias(libs.plugins.qodana) // Gradle Qodana Plugin
	alias(libs.plugins.kover) // Gradle Kover Plugin
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

// Configure project's dependencies
repositories {
	mavenCentral()
}

// Dependencies are managed with Gradle version catalog - read more: https://docs.gradle.org/8.1.1/userguide/platforms.html#sub:version-catalog
dependencies {
//    implementation(libs.annotations)
}

// Set the JVM language level used to build the project. Use Java 11 for 2020.3+, and Java 17 for 2022.2+.
kotlin {
	// version should be in sync with GitHub workflow files
	jvmToolchain(11)
}

// Configure Gradle IntelliJ Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
	pluginName = properties("pluginName")
	version = properties("platformVersion")
	type = properties("platformType")
	updateSinceUntilBuild = false
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
	groups.empty()
	repositoryUrl = properties("pluginRepositoryUrl")
}

// Configure Gradle Qodana Plugin - read more: https://github.com/JetBrains/gradle-qodana-plugin
qodana {
	cachePath = provider { file(".qodana").canonicalPath }
	reportPath = provider { file("build/reports/inspections").canonicalPath }
	saveReport = true
	showReport = environment("QODANA_SHOW_REPORT").map { it.toBoolean() }.getOrElse(false)
}

// Configure Gradle Kover Plugin - read more: https://github.com/Kotlin/kotlinx-kover#configuration
koverReport {
	defaults {
		xml {
			onCheck = true
		}
	}
}

tasks {
	wrapper {
		gradleVersion = properties("gradleVersion").get()
		distributionType = Wrapper.DistributionType.ALL
	}

	patchPluginXml {
		version = properties("pluginVersion")
		sinceBuild = properties("pluginSinceBuild")
		// attribute `until-build` is optional
		// https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html#idea-plugin__idea-version
		// Note: default value of `untilBuild` is disabled in `intellij {}` block
		// untilBuild = properties("pluginUntilBuild")

		// Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
		pluginDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
			val start = "<!-- Plugin description -->"
			val end = "<!-- Plugin description end -->"

			with(it.lines()) {
				if (!containsAll(listOf(start, end))) {
					throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
				}
				subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
			}
		}

		val changelog = project.changelog // local variable for configuration cache compatibility
		// Get the latest available change notes from the changelog file
		changeNotes = properties("pluginVersion").map { pluginVersion ->
			"<h2>${pluginVersion}</h2>\n" + with(changelog) {
				val extraItems = mapOf(
						"See also" to setOf("[Full changelog](https://github.com/rybak/intellij-copy-commit-reference/blob/main/CHANGELOG.md)")
				)
				renderItem(
						(getOrNull(pluginVersion) ?: getUnreleased())
								.withHeader(false)
								.withEmptySections(false)
								.plus(Changelog.Item("", "", "", false, extraItems, "", "")),
						Changelog.OutputType.HTML
				)
			}
		}
	}

	// Configure UI tests plugin
	// Read more: https://github.com/JetBrains/intellij-ui-test-robot
	runIdeForUiTests {
		systemProperty("robot-server.port", "8082")
		systemProperty("ide.mac.message.dialogs.as.sheets", "false")
		systemProperty("jb.privacy.policy.text", "<!--999.999-->")
		systemProperty("jb.consents.confirmation.enabled", "false")
	}

	signPlugin {
		certificateChainFile.set(environment("CERTIFICATE_CHAIN_FILE").map { file(it) }.orNull)
		privateKeyFile.set(environment("PRIVATE_KEY_FILE").map { file(it) }.orNull)
		certificateChain.set(environment("CERTIFICATE_CHAIN"))
		privateKey.set(environment("PRIVATE_KEY"))
		password = environment("PRIVATE_KEY_PASSWORD")
	}

	publishPlugin {
		dependsOn("patchChangelog")
		token = environment("PUBLISH_TOKEN")
		// The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels.
		// Examples:
		//   - 0.1.0-alpha.3 -> alpha
		//   - 0.5.0-beta.3 -> beta
		//   - 1.0.0 -> default
		// Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
		// https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
		channels = properties("pluginVersion").map {
			listOf(it.split('-').getOrElse(1) { "default" }.split('.').first())
		}
	}

	listOf(jar, kotlinSourcesJar).forEach { jarTaskProvider ->
		jarTaskProvider.configure {
			from(rootDir) {
				include("LICENSE.txt")
				into("META-INF")
			}
		}
	}
}
