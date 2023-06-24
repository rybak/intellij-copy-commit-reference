package dev.andrybak.intellijcopycommitreference

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.AbstractVcs
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsDataKeys
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
 * It is similar to {@link com.intellij.openapi.vcs.history.actions.CopyRevisionNumberAction}, but with more
 * information: the reference format includes an abbreviated hash of the commit, subject line of the commit (first line
 * of the commit message), and the date in ISO 8601 format.
 */
class CopyCommitReferenceAction : DumbAwareAction() {
	override fun actionPerformed(e: AnActionEvent) {
		getCommitMetadataFromContext(e) { listOfMetadata: List<VcsCommitMetadata> ->
			// TODO maybe need to reverse the list
			val references: List<String> = listOfMetadata.map(::commitReference)
			CopyPasteManager.getInstance().setContents(StringSelection(references.joinToString("\n")))
		}
	}

	override fun update(e: AnActionEvent) {
		if (e.project == null) {
			e.presentation.isEnabled = false
			return
		}
		val vcsManager: ProjectLevelVcsManager = ProjectLevelVcsManager.getInstance(e.project!!)
		val singleVCS: AbstractVcs? = vcsManager.singleVCS
		if (singleVCS == null) {
			e.presentation.isEnabled = false
			return
		}
		val revisionNumbers: List<VcsRevisionNumber> = unwrapNull(e.getData(VcsDataKeys.VCS_REVISION_NUMBERS))
		e.presentation.isEnabled = revisionNumbers.isNotEmpty()
	}

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
					logProvider.readMetadata(root, hashes) { metadata: VcsCommitMetadata ->
						result.add(metadata)
						current++
						if (current < hashes.size) {
							indicator.fraction = current / hashes.size
						} else {
							indicator.fraction = 1.0
						}
					}
				}
				consumer.invoke(result)
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