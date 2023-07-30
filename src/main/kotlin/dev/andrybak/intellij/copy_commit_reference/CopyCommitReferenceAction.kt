package dev.andrybak.intellij.copy_commit_reference

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.UpdateInBackground
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.vcs.log.VcsCommitMetadata

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
		// non-nullity is enforced by method `update()`
		val project: Project = event.project!!
		val actionName = templateText ?: "Bundle is broken for CopyCommitReferenceAction"
		getCommitMetadata(project, actionName, revisionNumbers, consumer)
	}

	private fun unwrapNull(data: Array<VcsRevisionNumber>?): List<VcsRevisionNumber> {
		return if (data != null) listOf(*data) else emptyList()
	}
}
