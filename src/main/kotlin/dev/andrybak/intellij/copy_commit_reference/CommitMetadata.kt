package dev.andrybak.intellij.copy_commit_reference

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.vcs.log.Hash
import com.intellij.vcs.log.VcsCommitMetadata
import com.intellij.vcs.log.impl.VcsProjectLog

/**
 * Extracts metadata of given [revisionNumbers] from VCS logs of given [project] and
 * invokes given [consumer] with the extracted metadata.
 *
 * @param actionName the name of the user action for user-facing progress meter of the background tasks
 */
fun getCommitMetadata(project: Project, actionName: String, revisionNumbers: List<VcsRevisionNumber>,
					  consumer: (List<VcsCommitMetadata>) -> Unit) {
	val hashes: List<String> = revisionNumbers.map { it.asString() }

	val task = object : Task.Backgroundable(project, actionName, true) {
		override fun run(indicator: ProgressIndicator) {
			indicator.isIndeterminate = false
			val logProviders = VcsProjectLog.getLogProviders(project)
			val result: MutableMap<Hash, VcsCommitMetadata> = mutableMapOf()
			var current = 0.0
			// +1 for passing into `consumer`
			val progressSize = hashes.size.toDouble() + 1.0
			logProviders.forEach { (root, logProvider) ->
				try {
					logProvider.readMetadata(root, hashes) { metadata: VcsCommitMetadata ->
						result[metadata.id] = metadata
						current++
						if (current < progressSize) {
							indicator.fraction = current / progressSize
						} else {
							indicator.fraction = 1.0
						}
					}
				} catch (e: VcsException) {
					/* If a user has Git submodules or just Git repositories lying around inside the project's
					 * directory, then not every `logProvider` will be able to find the hashes.  Unfortunately,
					 * there is no way to find the correct `logProvider` from the plugin. */
					logger<CopyCommitReferenceAction>().warn("Couldn't load hashes $hashes from $logProvider", e)
				}
			}
			consumer(result.values.toList())
			indicator.fraction = 1.0
		}

		override fun onThrowable(error: Throwable) {
			logger<CopyCommitReferenceAction>().error("Couldn't do '$actionName':", error)
		}
	}
	// very important not to forget to do something with the task ;-)
	task.queue()
}