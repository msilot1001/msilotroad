package io.github.msilot1001.msilotroad.command

import io.github.monun.tap.math.toRadians
import io.github.msilot1001.msilotroad.Messages
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class DrawlineCommand : CommandExecutor, TabCompleter {
    // <command> "length" "angle" "elevation"
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) return true

        // check is length | angles arg is present
        if (args == null || args.isEmpty()) {
            sender.sendMessage(Messages.DRAWLINE_LENGTHNOTPROVIDED)
            return true
        }

        val length: Int
        try {
            val templength = args[0].toInt();

            if (templength > 1000) {
                sender.sendMessage(Messages.DRAWLINE_TOOLONGLENGTH)
                return true
            } else {
                length = templength
            }
        } catch (e: NumberFormatException) {
            sender.sendMessage(Messages.DRAWLINE_WRONGFORMAT)
            return true
        }


        var elevation = .0;
        // if elevation if provided
        if (args.size >= 3) {
            try {
                elevation = args[2].toDouble()
            } catch (e: NumberFormatException) {
                sender.sendMessage(Messages.DRAWLINE_WRONGELEVATION);
                return true
            }
        }

        var vector: Vector = sender.eyeLocation.direction.normalize()
        // if angle argument provided
        if (args.size >= 2) {
            try {
                val angle = args[1].toInt().rem(360);
                vector = Vector(cos(angle.toDouble().toRadians()), elevation, sin(angle.toDouble().toRadians()))
            } catch (e: NumberFormatException) {
                sender.sendMessage(Messages.DRAWLINE_WRONGFORMAT)
                return true
            }
        } else {
            val angle = sender.eyeLocation.yaw.roundToInt()
            vector = Vector(cos(angle.toDouble().toRadians()), elevation, sin(angle.toDouble().toRadians()))
        }

        vector = vector.multiply(length)

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
                // <command> <length> <angle> <elevation>
                1 -> {
                    // length
                    return null
                }

                2 -> {
                    // angle
                    if (sender is Player) {
                        val p = sender as Player
                        val angle = sender.eyeLocation.yaw.roundToInt()
                        k.add(angle.toString())
                    }
                }

                3 -> {

                }

                else -> {
                    return null
                }
            }

            return k
        }
        return null
    }
}