package net.revelationmc.gamesigns.model.sign;

import net.revelationmc.gamesigns.GameSignsPlugin;
import net.revelationmc.gamesigns.model.AbstractManager;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class GameSignManager extends AbstractManager<UUID, GameSign> {
    private final GameSignsPlugin plugin;

    public GameSignManager(GameSignsPlugin plugin) {
        super(GameSign::new);
        this.plugin = plugin;
    }

    public void loadAllSigns() {
        final ConfigurationSection section = this.plugin.getServerSignConfiguration()
                .getConfigurationSection("signs");

        if (section == null) {
            this.plugin.getLogger().log(Level.INFO, "No game signs found to load.");
            return;
        }

        final Set<String> signIds = section.getKeys(false);

        for (String signId : signIds) {
            final Location location = section.getLocation(signId + ".location");
            if (location == null) {
                throw new IllegalStateException("Failed to load game sign " + signId + "! Location null.");
            }

            final GameSign sign = this.getOrCreate(UUID.fromString(signId));
            sign.setServer(section.getString(signId + ".destination"));
            sign.setLocation(location);
        }

        this.plugin.getLogger().log(Level.INFO, "Successfully loaded " + signIds.size() + " game signs.");
    }

    public Set<String> getUniqueServerIds() {
        final Set<String> serverIds = new HashSet<>();
        for (GameSign sign : this.getAll().values()) {
            serverIds.add(sign.getServer());
        }
        return serverIds;
    }

    public Set<GameSign> getAllSignsForServer(String serverId) {
        final Set<GameSign> signs = new HashSet<>();
        for (GameSign sign : this.getAll().values()) {
            if (sign.getServer().equals(serverId)) {
                signs.add(sign);
            }
        }
        return signs;
    }

    public Optional<GameSign> getByLocation(Location location) {
        for (GameSign sign : this.getAll().values()) {
            if (sign.getLocation().equals(location)) {
                return Optional.of(sign);
            }
        }
        return Optional.empty();
    }

    @Override
    protected UUID sanitizeId(UUID uniqueId) {
        return uniqueId;
    }
}
