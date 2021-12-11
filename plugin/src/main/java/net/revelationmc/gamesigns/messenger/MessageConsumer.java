package net.revelationmc.gamesigns.messenger;

import net.revelationmc.gamesigns.GameSignsPlugin;
import net.revelationmc.gamesigns.model.server.ServerData;
import net.revelationmc.gamesigns.model.sign.GameSign;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;

import java.util.List;
import java.util.function.Consumer;

public class MessageConsumer implements Consumer<ServerData> {
    private final GameSignsPlugin plugin;

    public MessageConsumer(GameSignsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void accept(ServerData server) {
        for (GameSign gameSign : this.plugin.getGameSignManager().getAllSignsForServer(server.getName())) {
            final Sign sign = gameSign.getBukkitSign();

            final List<String> lines = this.plugin.getConfig().getStringList("sign-format");
            for (int i = 0; i < lines.size(); i++) {
                sign.setLine(i, ChatColor.translateAlternateColorCodes('&', lines.get(i)
                        .replace("%sign-header%", this.plugin.getConfig().getString("sign-header"))
                        .replace("%server-id%", server.getName())
                        .replace("%status%", server.getState().getName())
                        .replace("%online-players%", String.valueOf(server.getOnlinePlayers()))
                        .replace("%max-players%", String.valueOf(server.getMaxPlayers()))
                ));
            }

            sign.update();
        }
    }
}
