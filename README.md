# ShopPlugin

A modern, efficient, and highly configurable shop plugin for Minecraft Paper servers with dynamic pricing, infinite stock, and player-friendly economy integration.

## Features

- ✅ Modern and intuitive GUI
- ✅ Buy and sell items with customizable prices
- ✅ Dynamic pricing system that adjusts based on player activity
- ✅ Infinite or limited stock options
- ✅ Vault integration for economy plugins
- ✅ SQLite database (MySQL coming soon)
- ✅ Easy-to-use configuration files
- ✅ Admin commands for price management
- ✅ Transaction history tracking
- ✅ Optimized for large servers

## Requirements

- Minecraft Paper 1.21.4+
- Java 21+
- Vault plugin
- An economy plugin (e.g., EssentialsX, CMI)

## Installation

1. Download the latest `.jar` file
2. Place it in your `plugins/` folder
3. Restart your server
4. Configure the plugin in `plugins/ShopPlugin/config.yml`
5. Reload with `/shop reload`

## Quick Start

```bash
# For players
/shop                    # Open the shop
/shop search <item>      # Search for an item

# For admins
/shopadmin reload        # Reload configuration
/shopadmin prices        # Manage prices
/shopadmin transactions  # View transaction history
```

## Configuration

All configuration files are located in `plugins/ShopPlugin/`:

- `config.yml` - Main plugin configuration
- `messages.yml` - Customizable messages
- `items.yml` - Item prices and settings
- `categories.yml` - Shop categories

## Dynamic Pricing

The plugin includes an advanced dynamic pricing system that:

- Increases buy prices when demand is high
- Decreases sell prices when supply is high
- Supports configurable price ranges and reset intervals
- Prevents price manipulation by limiting change per transaction

## Support

For issues, suggestions, or contributions, please visit the GitHub repository.

## License

This plugin is provided as-is for use on Minecraft servers.
