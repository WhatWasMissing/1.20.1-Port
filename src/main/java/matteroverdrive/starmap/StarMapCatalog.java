package matteroverdrive.starmap;

import java.util.ArrayList;
import java.util.List;

/** Deterministic modern representation of the legacy Galaxy -> Quadrant -> Star -> Planet hierarchy. */
public final class StarMapCatalog {
    public record Planet(String name, String type, int orbit, int habitability,
                         int temperature, int gravityPercent, int moons, boolean atmosphere) {}
    public record Star(String name, int x, int y, int temperature, List<Planet> planets) {}
    public record Quadrant(String name, int x, int y, List<Star> stars) {}

    private static final List<Quadrant> QUADRANTS = build();

    private StarMapCatalog() {}

    public static List<Quadrant> quadrants() {
        return QUADRANTS;
    }

    private static List<Quadrant> build() {
        List<Quadrant> quadrants = new ArrayList<>();
        String[] qNames = {"Aquila", "Cygnus", "Orion", "Perseus"};
        for (int q = 0; q < 4; q++) {
            List<Star> stars = new ArrayList<>();
            for (int s = 0; s < 6; s++) {
                int seed = q * 97 + s * 31 + 17;
                List<Planet> planets = new ArrayList<>();
                int count = 2 + Math.floorMod(seed, 4);
                for (int p = 0; p < count; p++) {
                    String type = switch (Math.floorMod(seed + p * 7, 4)) {
                        case 0 -> "Terrestrial";
                        case 1 -> "Gas Giant";
                        case 2 -> "Dwarf";
                        default -> "Oceanic";
                    };
                    int habitability = Math.floorMod(seed * 13 + p * 19, 101);
                    int temperature = switch (type) {
                        case "Gas Giant" -> 90 + Math.floorMod(seed * 11 + p * 23, 420);
                        case "Dwarf" -> 35 + Math.floorMod(seed * 7 + p * 17, 260);
                        default -> 180 + Math.floorMod(seed * 17 + p * 29, 520);
                    };
                    int gravity = switch (type) {
                        case "Gas Giant" -> 150 + Math.floorMod(seed * 5 + p * 31, 180);
                        case "Dwarf" -> 15 + Math.floorMod(seed * 3 + p * 13, 45);
                        default -> 45 + Math.floorMod(seed * 9 + p * 21, 105);
                    };
                    int moons = Math.floorMod(seed + p * 5, type.equals("Gas Giant") ? 8 : 4);
                    boolean atmosphere = !type.equals("Dwarf") || habitability > 65;
                    planets.add(new Planet(
                            qNames[q] + "-" + (s + 1) + (char) ('a' + p),
                            type, p + 1, habitability, temperature, gravity, moons, atmosphere));
                }
                stars.add(new Star(qNames[q] + " " + (s + 1),
                        -72 + Math.floorMod(seed * 23, 145),
                        -24 + Math.floorMod(seed * 17, 49),
                        2800 + Math.floorMod(seed * 131, 5200),
                        List.copyOf(planets)));
            }
            quadrants.add(new Quadrant(qNames[q],
                    (q % 2) * 110 - 55,
                    (q / 2) * 40 - 20,
                    List.copyOf(stars)));
        }
        return List.copyOf(quadrants);
    }
}
