package matteroverdrive.starmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Deterministic compatibility-preserving translation of the legacy GalaxyGenerator.
 * The first four quadrants / first six stars retain the original 1.20 catalog indices;
 * the surrounding catalog expands to the recovered legacy 3^3 quadrant / 2048+ star scale.
 */
public final class StarMapCatalog {
    public static final int LEGACY_QUADRANT_AXIS = 3;
    public static final int LEGACY_QUADRANT_COUNT = 27;
    public static final int LEGACY_MIN_STARS = 2048;
    public static final int LEGACY_MAX_STARS = 2304;
    public static final int LEGACY_MIN_PLANETS = 1;
    public static final int LEGACY_MAX_PLANETS = 4;
    private static final int GENERATED_STAR_COUNT = 2176;
    private static final long CATALOG_SEED = 0x4D4F31323031L;

    // Recovered 1.7 StarGen class weights and temperature bands.
    private static final double[] STAR_WEIGHTS = {0.00003D, 0.13D, 0.6D, 3.0D, 7.6D, 12.1D, 76.45D};
    private static final int[][] STAR_TEMPERATURES = {
            {30000, 60000}, {10000, 30000}, {7500, 10000}, {6000, 7500},
            {5200, 6000}, {3700, 5200}, {2400, 3700}
    };
    private static final String[] LEGACY_NAMES = {
            "Felis", "Mali", "Arch'tol", "Apillaris", "Agamir", "Asphylium", "Atryll", "Balartropheus",
            "Brynox", "Borrphylus", "Crom'atar", "Cindrol", "Drollgat", "Elunemar", "Ensavrithal", "Folchost",
            "Gytropia", "Hydraxion", "Illurius", "Infernogerm", "Jarak", "Jyraxor", "Khol'grob", "Kuzdulnar",
            "Luk'galesh", "Nebulus", "Neolucidius", "Orgathol", "Pak'tak", "Q'chan", "Rhexioculus", "Silurius",
            "Sol", "Tallicus", "Taucar"
    };
    private static final String[] PREFIXES = {"Alpha", "Beta", "Gamma", "Delta", "Omega", "Sigma", "Tau", "Exo", "Geo", "Meta"};
    private static final String[] SUFFIXES = {"Prime", "Septim", "Auto", "-ing", "-er", "-en"};

    public record Planet(String name, String type, int orbit, float legacyOrbit, float size,
                         int habitability, int temperature, int gravityPercent, int moons, boolean atmosphere) {}
    public record Star(String name, int x, int y, int temperature, int legacyType, float size, List<Planet> planets) {}
    public record Quadrant(String name, int x, int y, List<Star> stars) {}

    private static final List<Quadrant> QUADRANTS = build();

    private StarMapCatalog() {}

    public static List<Quadrant> quadrants() { return QUADRANTS; }

    public static boolean validPosition(int quadrant, int star, int planet) {
        if (quadrant < 0 || quadrant >= QUADRANTS.size()) return false;
        List<Star> stars = QUADRANTS.get(quadrant).stars();
        return star >= 0 && star < stars.size() && planet >= 0 && planet < stars.get(star).planets().size();
    }

    public static Planet planet(int quadrant, int star, int planet) {
        return validPosition(quadrant, star, planet) ? QUADRANTS.get(quadrant).stars().get(star).planets().get(planet) : null;
    }

    public static int quadrantIndex(Quadrant quadrant) { return quadrant == null ? -1 : QUADRANTS.indexOf(quadrant); }
    public static int starIndex(Quadrant quadrant, Star star) { return quadrant == null || star == null ? -1 : quadrant.stars().indexOf(star); }
    public static int planetIndex(Star star, Planet planet) { return star == null || planet == null ? -1 : star.planets().indexOf(planet); }

    /** Preserves recovered 10-per-AU / 8-per-LY timing while retaining stable 1.20 coordinates. */
    public static int travelTicks(int fromQ, int fromS, int fromP, int toQ, int toS, int toP) {
        if (!validPosition(fromQ, fromS, fromP) || !validPosition(toQ, toS, toP)) return 0;
        if (fromQ == toQ && fromS == toS) {
            int au = Math.abs(planet(fromQ, fromS, fromP).orbit() - planet(toQ, toS, toP).orbit());
            return Math.max(20, au * 10);
        }
        Quadrant fq = QUADRANTS.get(fromQ), tq = QUADRANTS.get(toQ);
        Star fs = fq.stars().get(fromS), ts = tq.stars().get(toS);
        double dx = (fq.x() + fs.x()) - (tq.x() + ts.x());
        double dy = (fq.y() + fs.y()) - (tq.y() + ts.y());
        int ly = Math.max(1, (int) Math.ceil(Math.sqrt(dx * dx + dy * dy)));
        return Math.max(20, ly * 8);
    }

    private static List<Quadrant> build() {
        List<List<Star>> starsByQuadrant = new ArrayList<>(LEGACY_QUADRANT_COUNT);
        for (int q = 0; q < LEGACY_QUADRANT_COUNT; q++) starsByQuadrant.add(new ArrayList<>());

        // Preserve every existing 1.20 star index in q0..q3, s0..s5.
        String[] compatibilityNames = {"Aquila", "Cygnus", "Orion", "Perseus"};
        for (int q = 0; q < 4; q++) for (int s = 0; s < 6; s++) {
            int seed = q * 97 + s * 31 + 17;
            starsByQuadrant.get(q).add(buildStar(q, s, seed, 2 + Math.floorMod(seed, 4), compatibilityNames[q] + " " + (s + 1), true));
        }

        Random distribution = new Random(CATALOG_SEED);
        int remaining = GENERATED_STAR_COUNT - 24;
        int serial = 24;
        while (remaining-- > 0) {
            int q = Math.floorMod(serial * 17 + distribution.nextInt(LEGACY_QUADRANT_COUNT), LEGACY_QUADRANT_COUNT);
            int s = starsByQuadrant.get(q).size();
            int seed = mix(q, s, serial);
            Random r = new Random(seed);
            // Legacy source calls nextInt(max-min), so defaults 1..3 despite config max 4.
            int planets = LEGACY_MIN_PLANETS + r.nextInt(LEGACY_MAX_PLANETS - LEGACY_MIN_PLANETS);
            starsByQuadrant.get(q).add(buildStar(q, s, seed, planets, legacyStarName(serial, r), false));
            serial++;
        }

        List<Quadrant> result = new ArrayList<>(LEGACY_QUADRANT_COUNT);
        for (int q = 0; q < LEGACY_QUADRANT_COUNT; q++) {
            if (q < 4) {
                int x = (q % 2) * 110 - 55;
                int y = (q / 2) * 40 - 20;
                result.add(new Quadrant(compatibilityNames[q], x, y, List.copyOf(starsByQuadrant.get(q))));
            } else {
                int qx = q % 3, qy = q / 9, qz = q / 3 % 3;
                int x = (qx - 1) * 90 + (qz - 1) * 16;
                int y = (qy - 1) * 42 + (qz - 1) * 8;
                result.add(new Quadrant("Quadrant " + (q + 1), x, y, List.copyOf(starsByQuadrant.get(q))));
            }
        }
        return List.copyOf(result);
    }

    private static Star buildStar(int q, int s, int seed, int planetCount, String name, boolean compatibility) {
        Random r = new Random(seed * 341873128712L + CATALOG_SEED);
        int type = weightedStarType(r.nextDouble());
        int minT = STAR_TEMPERATURES[type][0], maxT = STAR_TEMPERATURES[type][1];
        int temperature = minT + r.nextInt(Math.max(1, maxT - minT + 1));
        float starSize = starSize(type, r.nextFloat());
        int x = compatibility ? -72 + Math.floorMod(seed * 23, 145) : -82 + r.nextInt(165);
        int y = compatibility ? -24 + Math.floorMod(seed * 17, 49) : -29 + r.nextInt(59);
        List<Planet> planets = new ArrayList<>(planetCount);
        for (int p = 0; p < planetCount; p++) planets.add(buildPlanet(q, s, p, seed, r));
        return new Star(name, x, y, temperature, type, starSize, List.copyOf(planets));
    }

    private static Planet buildPlanet(int q, int s, int p, int seed, Random parent) {
        long pseed = ((long) seed << 32) ^ p * 0x9E3779B97F4A7C15L ^ CATALOG_SEED;
        Random r = new Random(pseed);
        float legacyOrbit = r.nextFloat();
        String type = weightedPlanetType(legacyOrbit, r.nextDouble());
        float size = switch (type) {
            case "Gas Giant" -> 2.0F + r.nextFloat();
            case "Dwarf" -> 0.2F + r.nextFloat() * 0.4F;
            default -> 0.7F + r.nextFloat() * 0.6F;
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
        String starName = q < 4 && s < 6 ? new String[]{"Aquila", "Cygnus", "Orion", "Perseus"}[q] + "-" + (s + 1) : "Q" + (q + 1) + "-S" + (s + 1);
        return new Planet(starName + (char) ('a' + p), type, p + 1, legacyOrbit, size,
                habitability, temperature, gravity, moons, atmosphere);
    }

    private static String weightedPlanetType(float orbit, double roll) {
        double normal = orbit > 0.4F && orbit < 0.6F ? 0.3D : 0.1D;
        double gas = orbit > 0.6F ? 0.3D : 0.1D;
        double dwarf = orbit > 0.6F || orbit < 0.4F ? 0.3D : 0.1D;
        double total = normal + gas + dwarf;
        double x = roll * total;
        if ((x -= gas) < 0) return "Gas Giant";
        if ((x -= dwarf) < 0) return "Dwarf";
        return "Normal";
    }

    private static int weightedStarType(double roll) {
        double total = 0; for (double weight : STAR_WEIGHTS) total += weight;
        double x = roll * total;
        for (int i = 0; i < STAR_WEIGHTS.length; i++) if ((x -= STAR_WEIGHTS[i]) < 0) return i;
        return STAR_WEIGHTS.length - 1;
    }

    private static float starSize(int type, float roll) {
        float[][] ranges = {{6.6F,8.8F},{1.8F,6.6F},{1.4F,1.8F},{1.15F,1.4F},{0.96F,1.15F},{0.7F,0.96F},{0.2F,0.7F}};
        return ranges[type][0] + roll * (ranges[type][1] - ranges[type][0]);
    }

    private static String legacyStarName(int serial, Random r) {
        String base = LEGACY_NAMES[Math.floorMod(serial * 13 + r.nextInt(LEGACY_NAMES.length), LEGACY_NAMES.length)];
        if (r.nextFloat() < 1.0F && r.nextInt(5) == 0) base = PREFIXES[r.nextInt(PREFIXES.length)] + " " + base;
        if (r.nextFloat() < 0.8F && r.nextInt(4) == 0) base = base + " " + SUFFIXES[r.nextInt(SUFFIXES.length)];
        return base + " " + (serial + 1);
    }

    private static int mix(int q, int s, int serial) {
        int x = serial * 0x45d9f3b + q * 0x119de1f3 + s * 0x3449;
        x ^= x >>> 16; x *= 0x45d9f3b; x ^= x >>> 16;
        return x;
    }
}
