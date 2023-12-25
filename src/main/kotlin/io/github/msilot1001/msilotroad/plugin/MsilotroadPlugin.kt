package io.github.msilot1001.msilotroad.plugin

import io.github.monun.tap.fake.*
import io.github.msilot1001.msilotroad.RoadCore
import io.github.msilot1001.msilotroad.command.PointCommand
import io.github.msilot1001.msilotroad.command.RoadCommand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin

class MsilotroadPlugin : JavaPlugin(), Listener {
  lateinit var fakeServer: FakeEntityServer
    private set

  override fun onEnable() {
    logger.info("Hellowwwwwwwwwwwwwwwwwwwwwwww world!")

    // tap fakeserver
    fakeServer = FakeEntityServer.create(this)

    // fakeserver update every tick
    server.scheduler.runTaskTimer(this, fakeServer::update, 0L, 1L)

    RoadCore.initCore(this)

    // commands
    getCommand("point")?.setExecutor(PointCommand())
    getCommand("road")?.setExecutor(RoadCommand())
    getCommand("road")?.tabCompleter = RoadCommand()

    // events
    server.pluginManager.registerEvents(this, this)
  }

  @EventHandler
  fun onPlayerJoin(e: PlayerJoinEvent) {
    fakeServer.addPlayer(e.player)
  }

  @EventHandler
  fun onPlayerQuit(e: PlayerQuitEvent) {
    RoadCore.terminateAll(e.player)

    fakeServer.removePlayer(e.player)
  }

  @EventHandler
  fun onPlayerInteract(e: PlayerInteractEvent) {
    val p = e.player
    val item = e.player.inventory.itemInMainHand.type
    p.sendMessage("$item used")

    val session = RoadCore.findSession(e.player, e.player.world)
    if (session != null && item == RoadCore.tool) {
      if (e.action.isLeftClick) {
        session.toolLeftClick(e)
      } else if (e.action.isRightClick) {
        session.toolRightClick(e)
      }
    }
  }

  @EventHandler
  fun onPlayerMove(event: PlayerMoveEvent) {
    val playerstoWatch = RoadCore.activePlayers()

    // if player is in the list of players that are currently on session and also in the same world
    // as initialized
    if (playerstoWatch.find { it == event.player } != null) {
      val session = RoadCore.findSession(event.player, event.player.world)

      if (session != null) {
        event.player.rayTraceBlocks(100.0)?.let { rayTraceResult ->
          if (rayTraceResult.hitBlock != null) {
            rayTraceResult.hitBlock?.let { block ->
              // move fakeblock
              session.moveFakeBlock(block.location.add(0.5, 0.01, 0.5))
            }
          }
        }
      }
    }
  }
}
