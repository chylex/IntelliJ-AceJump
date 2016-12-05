package com.johnlindquist.acejump

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.editor.colors.EditorColors.CARET_COLOR
import com.intellij.openapi.editor.event.VisibleAreaListener
import com.intellij.openapi.project.DumbAwareAction.ACTIONS_KEY
import com.intellij.util.SmartList
import com.intellij.util.ui.UIUtil.getClientProperty
import com.intellij.util.ui.UIUtil.putClientProperty
import com.johnlindquist.acejump.search.Finder
import com.johnlindquist.acejump.search.Jumper
import com.johnlindquist.acejump.ui.AceUI.editor
import com.johnlindquist.acejump.ui.AceUI.keyMap
import com.johnlindquist.acejump.ui.AceUI.restoreEditorSettings
import com.johnlindquist.acejump.ui.AceUI.setupCanvas
import com.johnlindquist.acejump.ui.AceUI.setupCursor
import com.johnlindquist.acejump.ui.Canvas
import java.awt.Color.BLUE
import java.awt.Color.RED

object KeyboardHandler {
  @Volatile
  var isEnabled = false
  private var text = ""
  private val handler = EditorActionManager.getInstance().typedAction.rawHandler

  fun activate() = if (!isEnabled) startListening() else toggleTargetMode()
  fun processCommand(keyCode: Int) = keyMap[keyCode]?.invoke()

  fun processBackspaceCommand() {
    text = ""
    Finder.reset()
    updateUIState()
  }

  val returnToNormalIfChanged = VisibleAreaListener { resetUIState() }
  private fun configureEditor() {
    setupCursor()
    setupCanvas()
    interceptKeystrokes()
    editor.scrollingModel.addVisibleAreaListener(returnToNormalIfChanged)
  }

  private fun interceptKeystrokes() {
    EditorActionManager.getInstance().typedAction.setupRawHandler { _, key, _ ->
      text += key
      Finder.findOrJump(text, key)
    }
  }

  private var backup: MutableList<AnAction>? = null

  private fun configureKeyMap() {
    backup = getClientProperty(editor.component, ACTIONS_KEY)
    putClientProperty(editor.component, ACTIONS_KEY, SmartList<AnAction>(AceKeyAction))
    val css = CustomShortcutSet(*keyMap.keys.toTypedArray())
    AceKeyAction.registerCustomShortcutSet(css, editor.component)
  }

  fun startListening() {
    isEnabled = true
    configureEditor()
    configureKeyMap()
  }

  fun updateUIState() {
    if (Jumper.hasJumped) {
      Jumper.hasJumped = false
      resetUIState()
    } else {
      Canvas.jumpLocations = Finder.jumpLocations
      Canvas.repaint()
    }
  }

  fun resetUIState() {
    text = ""
    isEnabled = false
    putClientProperty(editor.component, ACTIONS_KEY, backup)
    AceKeyAction.unregisterCustomShortcutSet(editor.component)
    editor.scrollingModel.removeVisibleAreaListener(returnToNormalIfChanged)
    EditorActionManager.getInstance().typedAction.setupRawHandler(handler)
    Finder.reset()
    Canvas.reset()
    restoreEditorSettings()
  }

  fun toggleTargetMode() {
    if (Finder.toggleTargetMode())
      editor.colorsScheme.setColor(CARET_COLOR, RED)
    else
      editor.colorsScheme.setColor(CARET_COLOR, BLUE)
    Canvas.repaint()
  }
}