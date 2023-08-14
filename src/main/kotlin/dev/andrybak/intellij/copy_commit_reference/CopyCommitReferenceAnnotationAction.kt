package dev.andrybak.intellij.copy_commit_reference

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vcs.annotate.AnnotationGutterActionProvider
import com.intellij.openapi.vcs.annotate.FileAnnotation
import com.intellij.openapi.vcs.annotate.UpToDateLineNumberListener
import com.intellij.util.PlatformIcons

/**
 * This context menu action provides easy access to the "reference" pretty format of Git in IntelliJ UI annotation
 * gutter of the editor.
 * See [Git documentation of `git-log`](https://git-scm.com/docs/git-log#_pretty_formats) for details.
 * It is similar to [com.intellij.openapi.vcs.actions.CopyRevisionNumberFromAnnotateAction], but with more
 * information: the reference format includes an abbreviated hash of the commit, subject line of the commit (first line
 * of the commit message), and the date in ISO 8601 format.
 */
class CopyCommitReferenceAnnotationAction(private val annotation: FileAnnotation) : DumbAwareAction(
		CopyCommitReferenceBundle.messagePointer("action.devAndrybakCopyCommitReferenceAction.text"),
		CopyCommitReferenceBundle.messagePointer("action.devAndrybakCopyCommitReferenceAction.description"),
		PlatformIcons.COPY_ICON
), UpToDateLineNumberListener {

	private var lineNumber = -1

	override fun actionPerformed(e: AnActionEvent) {
		if (lineNumber < 0) {
			return
		}
		val revisionNumber = annotation.getLineRevisionNumber(lineNumber)
		if (revisionNumber != null) {
			// non-nullity is enforced by method `update()`
			val project = e.project!!
			val actionName = templateText ?: "Bundle is broken for CopyCommitReferenceAnnotationAction"
			getCommitMetadata(project, actionName, listOf(revisionNumber)) { listOfMetadata ->
				copyCommitReference(project, listOfMetadata)
			}
		}
	}

	override fun update(e: AnActionEvent) {
		val enabled = e.project != null && lineNumber >= 0 && annotation.getLineRevisionNumber(lineNumber) != null
		e.presentation.setEnabledAndVisible(enabled)
	}

	/**
	 * Consumer the line number given to [UpToDateLineNumberListener]s, same as
	 * [com.intellij.openapi.vcs.actions.CopyRevisionNumberFromAnnotateAction].
	 */
	override fun consume(upToDateLineNumber: Int) {
		lineNumber = upToDateLineNumber
	}
}

class CopyCommitReferenceGutterActionProvider : AnnotationGutterActionProvider {
	override fun createAction(annotation: FileAnnotation): AnAction = CopyCommitReferenceAnnotationAction(annotation)
}
