package dev.andrybak.intellij.copy_commit_reference

import com.intellij.DynamicBundle
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

private const val COPY_COMMIT_REFERENCE_BUNDLE_PATH = "messages.CopyCommitReferenceBundle"

object CopyCommitReferenceBundle : DynamicBundle(COPY_COMMIT_REFERENCE_BUNDLE_PATH) {
	fun messagePointer(@PropertyKey(resourceBundle = COPY_COMMIT_REFERENCE_BUNDLE_PATH) key: String): Supplier<String> {
		return getLazyMessage(key)
	}
}