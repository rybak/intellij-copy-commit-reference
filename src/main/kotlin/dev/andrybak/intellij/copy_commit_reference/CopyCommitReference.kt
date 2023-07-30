package dev.andrybak.intellij.copy_commit_reference

import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.issueLinks.IssueLinkHtmlRenderer
import com.intellij.util.ui.TextTransferable
import com.intellij.vcs.log.VcsCommitMetadata
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun copyCommitReference(project: Project, listOfMetadata: List<VcsCommitMetadata>) {
	/* Unlike CopyRevisionNumberAction, preserve the order in which the commits appear in the GUI:
	 * newest commits are at the top.  References are rarely copied en masse, but even when they
	 * are, it will be easier for user to navigate the result in the same order as in the GUI. */
	val references: List<String> = listOfMetadata.map(::commitReference)
	val plainTextResult = references.joinToString("\n")
	val htmlResult = IssueLinkHtmlRenderer.formatTextWithLinks(project, plainTextResult)
	CopyPasteManager.getInstance().setContents(TextTransferable(htmlResult, plainTextResult))
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
