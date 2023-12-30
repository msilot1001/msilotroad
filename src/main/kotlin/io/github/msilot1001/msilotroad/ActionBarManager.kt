package io.github.msilot1001.msilotroad

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedChatComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import org.bukkit.entity.Player

class ActionBarManager {
  fun sendActionBar(player: Player, message: Component) {
    val string = JSONComponentSerializer.json().serialize(message)
    val protocolManager = ProtocolLibrary.getProtocolManager()
    val component = WrappedChatComponent.fromJson(string)
    val packet = protocolManager.createPacket(PacketType.Play.Server.SYSTEM_CHAT)
    val integers = packet.integers

    if (integers.size() == 1) {
      integers.write(0, EnumWrappers.ChatType.GAME_INFO.id.toInt())
    } else {
      packet.booleans.write(0, true)
    }

    packet.strings.write(0, component.json)
    protocolManager.sendServerPacket(player, packet)
  }
}
