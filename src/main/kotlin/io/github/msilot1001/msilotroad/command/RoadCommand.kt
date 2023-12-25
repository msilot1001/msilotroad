package io.github.msilot1001.msilotroad.command

import io.github.msilot1001.msilotroad.RoadCore
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

public class RoadCommand : CommandExecutor, TabCompleter {
  override fun onCommand(
    sender: CommandSender,
    command: Command,
    label: String,
    args: Array<out String>?
  ): Boolean {
    if (sender is Player) {
      val p = sender as Player
      sender.sendMessage("Road Command | args: ${args.toString()}")

      if (args != null && args.isNotEmpty()) {
        when (args[0]) {
          "build" -> {
            RoadCore.registerSession(p, p.world)
            sender.sendMessage("Registered Session")
          }
          "settings" -> {
            RoadCore.findSession(p, p.world).let {
              sender.sendMessage(if (it == null) "Session Not Found" else "Session Exists!")
            }
          }
          "remove" -> {
            RoadCore.terminateSession(p, p.world).let {
              sender.sendMessage(if (!it) "Session Not Found" else "Session Deleted!")
            }
          }
        }
      }
    }

    return true
  }

  override fun onTabComplete(
    sender: CommandSender,
    command: Command,
    alias: String,
    args: Array<out String>?
  ): MutableList<String>? {
    if (args != null) {
      var k = ArrayList<String>()

      when (args.size) {
        // <command> [build|settings|remove]
        1 -> {
          k.addAll(listOf("build", "settings", "remove"))
        }
        2 -> {
          k = twoArgsAutocompletion(args)
        }
        else -> {
          return null
        }
      }

      return k
    }
    return null
  }

  private fun twoArgsAutocompletion(args: Array<out String>): ArrayList<String> {
    val k = ArrayList<String>()

    when (args[0]) {
      "build" -> {
        k.addAll(listOf("a", "b", "c"))
      }
      "settings" -> {
        k.addAll(listOf("1", "2", "3"))
      }
      "remove" -> {
        k.addAll(listOf("e", "f", "g"))
      }
    }

    return k
  }
}
