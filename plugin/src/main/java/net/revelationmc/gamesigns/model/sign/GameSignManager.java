package net.revelationmc.gamesigns.model.sign;

import net.revelationmc.gamesigns.GameSignsPlugin;
import net.revelationmc.gamesigns.model.AbstractManager;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Set;
import java.util.logging.Level;

public class GameSignManager extends AbstractManager<Location, GameSign> {
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

        final Set<String> signIds = section.getKeys(true);

        for (String signId : signIds) {
            final Location location = (Location) section.get(signId);
            if (location == null) {
                throw new IllegalStateException("Failed to load game sign " + signId + "! Location null.");
            }

            final GameSign sign = this.getOrCreate(location);
            sign.setServer(signId);
        }

        this.plugin.getLogger().log(Level.INFO, "Successfully loaded " + signIds.size() + " game signs.");
    }

    @Override
    protected Location sanitizeId(Location location) {
        return location;
    }
}
