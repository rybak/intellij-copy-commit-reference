package dev.andrybak.intellij.copy_commit_reference

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.UpdateInBackground
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.vcs.log.Hash
import com.intellij.vcs.log.VcsCommitMetadata
import com.intellij.vcs.log.impl.VcsProjectLog

/**
 * This context menu action provides easy access to the "reference" pretty format of Git.
 * See [Git documentation of `git-log`](https://git-scm.com/docs/git-log#_pretty_formats) for details.
 * It is similar to [com.intellij.openapi.vcs.history.actions.CopyRevisionNumberAction], but with more
 * information: the reference format includes an abbreviated hash of the commit, subject line of the commit (first line
 * of the commit message), and the date in ISO 8601 format.
 */
class CopyCommitReferenceAction : DumbAwareAction(), UpdateInBackground {
	override fun actionPerformed(e: AnActionEvent) {
		getCommitMetadataFromContext(e) { listOfMetadata: List<VcsCommitMetadata> ->
			// non-nullity is enforced by method `update()`
			val project: Project = e.project!!
			copyCommitReference(project, listOfMetadata)
		}
	}

	/**
	 * Determines if this action should be enabled in the GUI or not.
	 * Enforces that [AnActionEvent.getProject] is not `null`.
	 * Checks presence of commits in the selection similarly to how method
	 * [com.intellij.openapi.vcs.history.actions.CopyRevisionNumberAction.update] does it.
	 */
	override fun update(e: AnActionEvent) {
		if (e.project == null) {
			e.presentation.isEnabled = false
			return
		}
		val revisionNumbers: List<VcsRevisionNumber> = unwrapNull(e.getData(VcsDataKeys.VCS_REVISION_NUMBERS))
		e.presentation.isEnabled = revisionNumbers.isNotEmpty()
	}

	/**
	 * Extracts metadata of selected commits (if any are selected) from
	 * [context][com.intellij.openapi.actionSystem.AnActionEvent.getDataContext] of the given [event].
	 * Invokes given [consumer] with the extracted metadata.
	 *
	 * See also [com.intellij.vcs.log.ui.VcsLogPanel.getData].
	 */
	private fun getCommitMetadataFromContext(event: AnActionEvent, consumer: (List<VcsCommitMetadata>) -> Unit) {
		// VcsDataKeys.VCS_REVISION_NUMBERS in the context of the `event` is populated by IDEA's own UI.
		val revisionNumbers: List<VcsRevisionNumber> = unwrapNull(event.getData(VcsDataKeys.VCS_REVISION_NUMBERS))
		val hashes: List<String> = revisionNumbers.map { it.asString() }
		// non-nullity is enforced by method `update()`
		val project: Project = event.project!!

		@Suppress("DialogTitleCapitalization")
		val actionName: String = templateText ?: "Copy Commit Reference bundle is broken?"

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

	private fun unwrapNull(data: Array<VcsRevisionNumber>?): List<VcsRevisionNumber> {
		return if (data != null) listOf(*data) else emptyList()
	}
}
