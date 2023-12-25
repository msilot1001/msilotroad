package io.github.msilot1001.msilotroad

import io.github.monun.tap.fake.FakeEntity
import io.github.monun.tap.fake.FakeEntityServer
import io.github.msilot1001.msilotroad.plugin.MsilotroadPlugin
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

enum class RoadShapeType {
  LINE,
  CurveTHREE,
  CurveFOUR,
}

public class RoadSession {
  private val points = ArrayList<Location>()
  public val player: Player
  public val world: World
  public val roadShapeType = RoadShapeType.CurveTHREE
  private lateinit var fakeblock: FakeEntity<FallingBlock>

  constructor(player: Player, world: World) {
    this.player = player
    this.world = world
  }

  lateinit var location: Location
    private set

  fun initialize(fakeServer: FakeEntityServer) {
    // raytrace block that player is heading
    this.player.rayTraceBlocks(100.0)?.let { rayTraceResult ->
      if (rayTraceResult.hitBlock != null) {
        rayTraceResult.hitBlock?.let { block ->
          // spawn block
          val fakeblock =
            fakeServer.spawnFallingBlock(
              block.location.add(0.5, 0.01, 0.5),
              Material.LIGHT_BLUE_STAINED_GLASS.createBlockData()
            )
          fakeblock.updateMetadata { setGravity(false) }
          this.fakeblock = fakeblock
        }
      }
    }
  }

  fun moveFakeBlock(loc: Location): Boolean {
    fakeblock.moveTo(loc)
    return true
  }

  fun equals(player: Player, world: World): Boolean {
    return this.player == player && this.world == world
  }

  fun terminate() {
    fakeblock.remove()
  }

  fun toolLeftClick(e: PlayerInteractEvent) {}

  fun toolRightClick(e: PlayerInteractEvent) {
    // set point
    player.rayTraceBlocks(100.0)?.let { rayTraceResult ->
      if (rayTraceResult.hitBlock != null) {
        rayTraceResult.hitBlock?.let { block ->
          val location = block.location

          // get max amount of points
          val maxPoints = roadShapeType.ordinal + 2

          // check what point is now setting
          val n = maxPoints - points.size

          // if n = max points
          if (n >= maxPoints) {

          }
        }
      }
    }
  }

  fun actionBar(player: Player){
    player.spigot().sendMessage()
  }
}

object RoadCore {
  private val sessions = ArrayList<RoadSession>()
  private val playersinSession = ArrayList<Player>()
  private lateinit var fakeServer: FakeEntityServer

  // constants
  public val tool = Material.BLAZE_ROD

  fun initCore(plugin: MsilotroadPlugin) {
    this.fakeServer = plugin.fakeServer
  }

  fun update() {
    for (session in sessions) {}
  }

  fun registerSession(player: Player, world: World) {
    val session = RoadSession(player, world)
    session.initialize(fakeServer)

    sessions.add(session)
    playersinSession.add(player)
  }

  fun findSession(player: Player, world: World): RoadSession? {
    return sessions.find { it.equals(player, world) }
  }

  fun terminateSession(player: Player, world: World): Boolean {
    val result = this.findSession(player, world)

    if (result != null) {
      playersinSession.remove(player)
      result.terminate()
      sessions.remove(result)
      return true
    }

    return false
  }

  fun terminateAll(player: Player) {
    val session = sessions.find { it.player == player }
    if (session != null) {
      terminateSession(player, session.world)
    }
  }

  fun activePlayers(): ArrayList<Player> {
    return playersinSession
  }
}
