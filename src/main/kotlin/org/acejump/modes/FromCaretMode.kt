package org.acejump.modes

import org.acejump.action.AceTagAction
import org.acejump.config.AceConfig
import org.acejump.session.SessionMode
import org.acejump.session.SessionState
import org.acejump.session.TypeResult

class FromCaretMode : SessionMode {
  private companion object {
    private val HINT_TYPE_TAG = arrayOf(
      "<b>Type to Search...</b>"
    )
    
    private val HINT_ACTION_MODE = arrayOf(
      "<h>From Caret Mode</h>",
      "<f>[S]</f>elect... / <f>[D]</f>elete...",
      "<f>[X]</f> Cut... / <f>[C]</f>opy... / <f>[P]</f>aste..."
    )
    
    private val HINT_JUMP_MODE = arrayOf(
      "<f>[J]</f> at Tag / <f>[L]</f> past Query",
      "Word <f>[S]</f>tart / Word <f>[E]</f>nd"
    )
    
    private val ACTION_MODE_MAP = mapOf(
      'S' to ({ action: AceTagAction.SelectToCaret -> action }),
      'D' to (AceTagAction::Delete),
      'X' to (AceTagAction::Cut),
      'C' to (AceTagAction::Copy),
      'P' to (AceTagAction::Paste)
    )
    
    private val JUMP_MODE_MAP = mapOf(
      'J' to AceTagAction.JumpToSearchStart,
      'L' to AceTagAction.JumpPastSearchEnd,
      'S' to AceTagAction.JumpToWordStartTag,
      'E' to AceTagAction.JumpToWordEndTag
    )
  }
  
  override val caretColor
    get() = AceConfig.fromCaretModeColor
  
  private var actionMode: ((AceTagAction.SelectToCaret) -> AceTagAction)? = null
  
  override fun type(state: SessionState, charTyped: Char, acceptedTag: Int?): TypeResult {
    val actionMode = actionMode
    if (actionMode == null) {
      this.actionMode = ACTION_MODE_MAP[charTyped.toUpperCase()]
      return TypeResult.Nothing
    }
    
    if (acceptedTag == null) {
      return state.type(charTyped)
    }
    
    val jumpAction = JUMP_MODE_MAP[charTyped.toUpperCase()]
    if (jumpAction == null) {
      return TypeResult.Nothing
    }
    
    state.act(actionMode(AceTagAction.SelectToCaret(jumpAction)), acceptedTag, shiftMode = charTyped.isUpperCase())
    return TypeResult.EndSession
  }
  
  override fun getHint(acceptedTag: Int?): Array<String>? {
    return when {
      actionMode == null  -> HINT_ACTION_MODE
      acceptedTag == null -> HINT_TYPE_TAG
      else                -> HINT_JUMP_MODE
    }
  }
}