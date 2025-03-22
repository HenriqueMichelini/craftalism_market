package io.github.HenriqueMichelini.craftalism_market.models;

import org.bukkit.Material;

public record Category(Material material, String category, String title, int slot) {
}
