package org.acejump.session

import com.intellij.openapi.editor.Editor
import org.acejump.ExternalUsage
import org.acejump.search.TaggingResult

/**
 * Manages active [Session]s in [Editor]s. There may only be
 * one [Session] per [Editor], but multiple [Session]s across
 * multiple [Editor]s may be active at once.
 *
 * It is possible for an [Editor] to be disposed with an active
 * [Session]. In such case, the reference to both will remain
 * until a new [Session] starts, at which point the
 * [SessionManager.cleanup] method will purge disposed [Editor]s.
 */

@ExternalUsage
object SessionManager {
  private val sessions = HashMap<Editor, Session>(4)

  /**
   * Starts a new [Session], or returns an existing [Session]
   * if the specified [Editor] already has one.
   */
  fun start(editor: Editor): Session =
    sessions.getOrPut(editor) { cleanup(); Session(editor) }

  /**
   * Returns the active [Session] in the specified [Editor],
   * or null if the [Editor] has no active session.
   */
  operator fun get(editor: Editor): Session? = sessions[editor]

  /**
   * Ends the active [Session] in the specified [Editor],
   * or does nothing if the [Editor] has no active session.
   */
  fun end(editor: Editor, taggingResult: TaggingResult?) =
    sessions.remove(editor)?.dispose(taggingResult) ?: Unit

  private fun cleanup() = sessions.keys.filter { it.isDisposed }
    .forEach { disposedEditor -> sessions.remove(disposedEditor)?.dispose(null) }
}
