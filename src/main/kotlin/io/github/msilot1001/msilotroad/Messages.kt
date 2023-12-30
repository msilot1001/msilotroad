package io.github.msilot1001.msilotroad

import net.kyori.adventure.text.minimessage.MiniMessage

object Messages {
  private var mm = MiniMessage.miniMessage()

  val ROADSESSION_DEFAULT_MSG =
    mm.deserialize(
      "<bold><aqua><Left Button> </aqua><blue>MENU</blue><dark_blue> | </dark_blue><aqua><Right Button> </aqua><blue>SET/RESET POINT</blue>"
    )

  val ROADSESSION_MAXPOINTS =
    mm.deserialize("<red><bold>Max Points Reached! Change Road Type to Add More</bold></red>")

  val ROADSESSION_BUILD_SUCCESS =
    mm.deserialize("<bold><red>Road Building Success!</red></bold>")
}
