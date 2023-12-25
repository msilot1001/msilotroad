package io.github.msilot1001.msilotroad.command

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

public class PointCommand : CommandExecutor {
  override fun onCommand(
      sender: CommandSender,
      command: Command,
      label: String,
      args: Array<out String>?
  ): Boolean {
    if (sender is Player) {
      val pos = sender.getLocation().block

      sender.sendMessage("Pos: [X: ${pos.x}, Y: ${pos.y}, Z: ${pos.z} ] / Block ${pos.type}")
    }

    sender.sendMessage("Point CommandddddddddddddddddD!")

    return true
  }
}
