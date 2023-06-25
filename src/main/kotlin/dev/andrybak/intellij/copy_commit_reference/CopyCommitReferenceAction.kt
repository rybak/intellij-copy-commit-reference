package dev.andrybak.intellij.copy_commit_reference

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.vcs.log.VcsCommitMetadata
import com.intellij.vcs.log.impl.VcsProjectLog
import java.awt.datatransfer.StringSelection
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * This context menu action provides easy access to the "reference" pretty format of Git.
 * See [Git documentation of `git-log`](https://git-scm.com/docs/git-log#_pretty_formats) for details.
 * It is similar to [com.intellij.openapi.vcs.history.actions.CopyRevisionNumberAction], but with more
 * information: the reference format includes an abbreviated hash of the commit, subject line of the commit (first line
 * of the commit message), and the date in ISO 8601 format.
 */
class CopyCommitReferenceAction : DumbAwareAction() {
	override fun actionPerformed(e: AnActionEvent) {
		getCommitMetadataFromContext(e) { listOfMetadata: List<VcsCommitMetadata> ->
			/* Unlike CopyCommitReferenceAction, preserve the order in which the commits appear in the GUI:
			 * newest commits are at the top.  References are rarely copied en masse, but even when they
			 * are, it will be easier for user to navigate the result in the same order as in the GUI. */
			val references: List<String> = listOfMetadata.map(::commitReference)
			CopyPasteManager.getInstance().setContents(StringSelection(references.joinToString("\n")))
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
	 * Same as [com.intellij.openapi.vcs.history.actions.CopyRevisionNumberAction.getActionUpdateThread],
	 * it is OK to call [update] in the background thread.
	 */
	override fun getActionUpdateThread(): ActionUpdateThread {
		return ActionUpdateThread.BGT
	}

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
				val logProviders = VcsProjectLog.getLogProviders(project)
				val result: MutableList<VcsCommitMetadata> = mutableListOf()
				var current = 0.0
				logProviders.forEach { (root, logProvider) ->
					try {
						logProvider.readMetadata(root, hashes) { metadata: VcsCommitMetadata ->
							result.add(metadata)
							current++
							if (current < hashes.size) {
								indicator.fraction = current / hashes.size
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
				consumer(result)
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

	private fun commitReference(metadata: VcsCommitMetadata): String {
		return commitReference(metadata.id.asString(), metadata.subject, Instant.ofEpochMilli(metadata.timestamp))
	}

	private fun commitReference(hash: String, subject: String, timestamp: Instant): String {
		/* Class VcsCommitMetadata doesn't provide time zone of the commit (Git does store it),
		 * so showing in the local timezone is the only option. */
		val formattedDate: String = DateTimeFormatter.ISO_LOCAL_DATE.format(timestamp.atZone(ZoneId.systemDefault()))
		// TODO figure out how to do proper abbreviation
		val abbrevHash = hash.subSequence(0, 7)
		return "$abbrevHash ($subject, $formattedDate)"
	}
}