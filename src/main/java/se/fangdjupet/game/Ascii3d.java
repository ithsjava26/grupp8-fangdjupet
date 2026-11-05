package se.fangdjupet.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Vinstfilmen: riktig 3D ritad med tecken. Skolan och pokalen består av tusentals punkter med en
// riktning (normal). Varje bildruta vrids punkterna, den närmaste punkten i varje teckenruta vinner,
// och vinkeln mot ljuset väljer tecknet: ljusa ytor blir @ och # och mörka blir . och :.
final class Ascii3d {
    static final int FRAMES = 130;       // bildrutor i filmen
    static final int HEIGHT = 20;        // rader
    static final int SCHOOL_WIDTH = 42;  // skolan till vänster
    static final int TROPHY_WIDTH = 36;  // pokalen till höger

    private static final String SHADES = ".:-=+*#%@";
    private static final double[] LIGHT = normalized(-0.45, 0.65, -0.62);
    private static final double CAMERA = 6;   // kamerans avstånd
    private static final char GOLD = 'g', WALL = 'w', ROOF = 'r', GLASS = 'f', DOOR = 'd', CLOCK = 'c', FLAG = 'o', EDGE = 'e';
    private static final String RED = "\u001B[31m", YELLOW = "\u001B[33m", CYAN = "\u001B[36m",
            MAGENTA = "\u001B[35m", GREEN = "\u001B[32m", RESET = "\u001B[0m";

    private record Dot(double x, double y, double z, double nx, double ny, double nz, char kind) { }

    // En teckenbild: vilket tecken som står i varje ruta och vad det är gjort av (för färgen)
    private record Panel(char[][] shade, char[][] kind) { }

    // Ett sätt att måla en yta: s och t går från 0 till 1 över ytan
    private interface Paint {
        char at(double s, double t);
    }

    private Ascii3d() { }

    // Hela filmen. Varje bildruta är HEIGHT rader med färgkoder, skilda av \n.
    static List<String> film() {
        List<Dot> school = school(), trophy = trophy();
        List<String> frames = new ArrayList<>();
        for (int f = 0; f < FRAMES; f++) {
            double e = Math.min(1, f / (FRAMES - 16.0));        // de sista bildrutorna står allt still
            double slow = 1 - (1 - e) * (1 - e);                // snabbt först, sedan lugnare
            double zoom = 1 - Math.pow(1 - Math.min(1, f / 26.0), 3);   // allt växer fram i början
            frames.add(frame(f,
                    panel(school, SCHOOL_WIDTH, 0.55 - 2 * Math.PI * (1 - slow), 6.8 * zoom, -0.33, -0.26),
                    panel(trophy, TROPHY_WIDTH, -6 * Math.PI * (1 - slow), 7.9 * zoom, 0.04, -0.4)));
        }
        return frames;
    }

    private static Panel panel(List<Dot> dots, int width, double angle, double scale, double lift, double tilt) {
        char[][] shade = new char[HEIGHT][width], kind = new char[HEIGHT][width];
        double[][] nearest = new double[HEIGHT][width];
        for (int r = 0; r < HEIGHT; r++) {
            Arrays.fill(shade[r], ' ');
            Arrays.fill(nearest[r], Double.MAX_VALUE);
        }
        double sin = Math.sin(angle), cos = Math.cos(angle), tiltSin = Math.sin(tilt), tiltCos = Math.cos(tilt);
        for (Dot d : dots) {
            double nx = d.nx * cos + d.nz * sin, nz = d.nz * cos - d.nx * sin;
            double ny = d.ny * tiltCos - nz * tiltSin;
            nz = d.ny * tiltSin + nz * tiltCos;
            if (nz > 0.02 && d.kind != FLAG) continue;   // baksidan syns inte
            double x = d.x * cos + d.z * sin, z = d.z * cos - d.x * sin;
            double y = d.y * tiltCos - z * tiltSin;
            z = d.y * tiltSin + z * tiltCos;
            double near = CAMERA / (CAMERA + z);
            int col = (int) Math.round(width / 2.0 + x * near * scale * 2);
            int row = (int) Math.round(HEIGHT / 2.0 - (y + lift) * near * scale);
            if (col < 0 || col >= width || row < 0 || row >= HEIGHT) continue;
            double depth = d.kind == EDGE ? z - 0.05 : z;   // kanterna ritas ovanpå sin egen yta
            if (depth >= nearest[row][col]) continue;
            nearest[row][col] = depth;
            double facing = nx * LIGHT[0] + ny * LIGHT[1] + nz * LIGHT[2];
            double light = d.kind == FLAG ? Math.abs(facing) : Math.max(0, facing);   // flaggan syns från båda håll
            double shine = d.kind == GOLD ? Math.pow(light, 9) * 0.7 : 0;             // guldet glänser
            shade[row][col] = SHADES.charAt((int) Math.min(SHADES.length() - 1, level(d.kind, light + shine) * SHADES.length()));
            kind[row][col] = d.kind;
        }
        return new Panel(shade, kind);
    }

    // Väggarna håller sig mörka, så att fönstren och kanterna syns mot dem
    private static double level(char kind, double light) {
        return switch (kind) {
            case GLASS, CLOCK -> 0.72;
            case EDGE -> 0.9;
            case WALL -> 0.05 + 0.42 * light;
            default -> 0.14 + 0.8 * light;
        };
    }

    private static String frame(int f, Panel school, Panel trophy) {
        twinkle(f, trophy);
        StringBuilder out = new StringBuilder();
        for (int r = 0; r < HEIGHT; r++) {
            if (r > 0) out.append('\n');
            row(out, school, r);
            row(out, trophy, r);
            out.append(RESET);
        }
        return out.toString();
    }

    // Stjärnor som tänds och släcks runt pokalen
    private static void twinkle(int f, Panel trophy) {
        for (int i = 0; i < 9; i++) {
            int hash = (f / 4 * 31 + i + 1) * 0x9E3779B1;
            int r = Math.floorMod(hash >>> 7, HEIGHT), c = Math.floorMod(hash >>> 15, TROPHY_WIDTH);
            if (trophy.shade[r][c] != ' ') continue;
            trophy.shade[r][c] = f % 4 < 2 ? '+' : '*';
            trophy.kind[r][c] = GOLD;
        }
    }

    private static void row(StringBuilder out, Panel panel, int r) {
        char last = 0;
        for (int c = 0; c < panel.shade[r].length; c++) {
            if (panel.shade[r][c] != ' ' && panel.kind[r][c] != last) {
                last = panel.kind[r][c];
                out.append(color(last));
            }
            out.append(panel.shade[r][c]);
        }
    }

    private static String color(char kind) {
        return switch (kind) {
            case ROOF, FLAG -> RED;
            case GLASS, CLOCK -> CYAN;
            case DOOR -> MAGENTA;
            case EDGE -> YELLOW;
            case WALL -> GREEN;
            default -> YELLOW;
        };
    }

    // ── Pokalen: en profil som snurras runt y-axeln, och två handtag ──

    private static List<Dot> trophy() {
        double[][] outside = {{-1, 0.5}, {-0.9, 0.5}, {-0.84, 0.3}, {-0.74, 0.15}, {-0.58, 0.08}, {-0.46, 0.09},
                {-0.4, 0.2}, {-0.33, 0.09}, {-0.22, 0.13}, {-0.02, 0.36}, {0.22, 0.48}, {0.55, 0.55}, {0.9, 0.6}, {0.97, 0.62}};
        double[][] inside = {{0.97, 0.62}, {0.93, 0.55}, {0.6, 0.42}, {0.3, 0.1}};
        List<Dot> dots = new ArrayList<>();
        spin(dots, outside, true);
        spin(dots, inside, false);
        handle(dots, 1);
        handle(dots, -1);
        return dots;
    }

    private static void spin(List<Dot> dots, double[][] profile, boolean ribs) {
        for (int i = 0; i + 1 < profile.length; i++) {
            double y0 = profile[i][0], r0 = profile[i][1], y1 = profile[i + 1][0], r1 = profile[i + 1][1];
            double dy = y1 - y0, dr = r1 - r0, length = Math.hypot(dy, dr);
            for (double s = 0; s <= 1; s += 0.025 / length) {
                double y = y0 + dy * s, r = r0 + dr * s;
                for (double a = 0; a < 2 * Math.PI; a += 0.03 / Math.max(r, 0.2)) {
                    double sin = Math.sin(a), cos = Math.cos(a);
                    // Räfflor på bägaren: det är dem man ser snurra
                    double rib = ribs && y > 0.05 ? 0.55 * Math.sin(10 * a) : 0;
                    double[] n = normalized(cos * dy - sin * rib * Math.abs(dy), -dr, sin * dy + cos * rib * Math.abs(dy));
                    dots.add(new Dot(cos * r, y, sin * r, n[0], n[1], n[2], GOLD));
                }
            }
        }
    }

    private static void handle(List<Dot> dots, int side) {
        for (double a = -1.9; a <= 1.9; a += 0.04) {
            double cx = side * (0.52 + 0.34 * Math.cos(a)), cy = 0.5 + 0.3 * Math.sin(a);
            for (double b = 0; b < 2 * Math.PI; b += 0.3) {
                double ox = side * Math.cos(a) * Math.cos(b), oy = Math.sin(a) * Math.cos(b), oz = Math.sin(b);
                dots.add(new Dot(cx + 0.07 * ox, cy + 0.07 * oy, 0.07 * oz, ox, oy, oz, GOLD));
            }
        }
    }

    // ── Skolan: huset med sadeltak, tornet med klockan och flaggan ──

    private static List<Dot> school() {
        List<Dot> dots = new ArrayList<>();
        double w = 1.4, d = 0.6, floor = -0.9, eaves = 0.1, ridge = 0.6;
        Paint plain = (s, t) -> WALL, roof = (s, t) -> ROOF, flag = (s, t) -> FLAG;
        Paint windows = (s, t) -> grid(s, 8, 0.6) && grid(t, 2, 0.5) ? GLASS : WALL;
        Paint endWindows = (s, t) -> grid(s, 3, 0.5) && grid(t, 2, 0.5) ? GLASS : WALL;
        wall(dots, new double[][] {{-w, floor, -d}, {w, floor, -d}, {w, eaves, -d}, {-w, eaves, -d}}, windows);
        wall(dots, new double[][] {{w, floor, d}, {-w, floor, d}, {-w, eaves, d}, {w, eaves, d}}, windows);
        wall(dots, new double[][] {{w, floor, -d}, {w, floor, d}, {w, eaves, d}, {w, eaves, -d}}, endWindows);
        wall(dots, new double[][] {{-w, floor, d}, {-w, floor, -d}, {-w, eaves, -d}, {-w, eaves, d}}, endWindows);
        wall(dots, new double[][] {{w, eaves, -d}, {w, eaves, d}, {w, ridge, 0}, {w, ridge, 0}}, plain);
        wall(dots, new double[][] {{-w, eaves, d}, {-w, eaves, -d}, {-w, ridge, 0}, {-w, ridge, 0}}, plain);
        double over = w + 0.06, edge = d + 0.08;   // taket sticker ut lite
        wall(dots, new double[][] {{-over, eaves, -edge}, {over, eaves, -edge}, {over, ridge, 0}, {-over, ridge, 0}}, roof);
        wall(dots, new double[][] {{over, eaves, edge}, {-over, eaves, edge}, {-over, ridge, 0}, {over, ridge, 0}}, roof);

        double tw = 0.34, front = -0.74, back = 0.12, top = 0.92, peak = 1.32, mid = (front + back) / 2;
        wall(dots, new double[][] {{-tw, floor, front}, {tw, floor, front}, {tw, top, front}, {-tw, top, front}}, Ascii3d::towerFront);
        wall(dots, new double[][] {{tw, floor, back}, {-tw, floor, back}, {-tw, top, back}, {tw, top, back}}, plain);
        wall(dots, new double[][] {{tw, floor, front}, {tw, floor, back}, {tw, top, back}, {tw, top, front}}, plain);
        wall(dots, new double[][] {{-tw, floor, back}, {-tw, floor, front}, {-tw, top, front}, {-tw, top, back}}, plain);
        double[] tip = {0, peak, mid};
        wall(dots, new double[][] {{-tw, top, front}, {tw, top, front}, tip, tip}, roof);
        wall(dots, new double[][] {{tw, top, back}, {-tw, top, back}, tip, tip}, roof);
        wall(dots, new double[][] {{tw, top, front}, {tw, top, back}, tip, tip}, roof);
        wall(dots, new double[][] {{-tw, top, back}, {-tw, top, front}, tip, tip}, roof);
        wall(dots, new double[][] {{-0.02, peak, mid}, {0.02, peak, mid}, {0.02, 1.62, mid}, {-0.02, 1.62, mid}}, flag);
        wall(dots, new double[][] {{0.02, 1.42, mid}, {0.5, 1.42, mid}, {0.5, 1.62, mid}, {0.02, 1.62, mid}}, flag);
        return dots;
    }

    // Dörren längst ner och klockan högst upp på tornets framsida
    private static char towerFront(double s, double t) {
        if (Math.abs(s - 0.5) < 0.2 && t < 0.22) return DOOR;
        if (Math.hypot((s - 0.5) * 0.68, (t - 0.8) * 1.82) < 0.17) return CLOCK;
        return WALL;
    }

    // Ligger s i ett av count fönster, där fönstret tar upp size av sitt fack?
    private static boolean grid(double s, int count, double size) {
        double cell = s * count - Math.floor(s * count);
        return Math.abs(cell - 0.5) < size / 2;
    }

    // En fyrhörning med hörnen a, b, c, d motsols sett utifrån. Är c och d samma punkt blir det en triangel.
    private static void wall(List<Dot> dots, double[][] corners, Paint paint) {
        double[] a = corners[0], b = corners[1], c = corners[2], d = corners[3];
        double[] u = {b[0] - a[0], b[1] - a[1], b[2] - a[2]}, v = {d[0] - a[0], d[1] - a[1], d[2] - a[2]};
        double[] n = normalized(u[1] * v[2] - u[2] * v[1], u[2] * v[0] - u[0] * v[2], u[0] * v[1] - u[1] * v[0]);
        double across = Math.sqrt(u[0] * u[0] + u[1] * u[1] + u[2] * u[2]);
        double up = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        for (double t = 0; t <= 1; t += 0.03 / up) {
            for (double s = 0; s <= 1; s += 0.03 / across) {
                double[] p = new double[3];
                for (int i = 0; i < 3; i++) {
                    double left = a[i] + (d[i] - a[i]) * t, right = b[i] + (c[i] - b[i]) * t;
                    p[i] = left + (right - left) * s;
                }
                char kind = paint.at(s, t);
                double border = Math.min(Math.min(s, 1 - s) * across, Math.min(t, 1 - t) * up);
                dots.add(new Dot(p[0], p[1], p[2], n[0], n[1], n[2], kind != FLAG && border < 0.04 ? EDGE : kind));
            }
        }
    }

    private static double[] normalized(double x, double y, double z) {
        double length = Math.sqrt(x * x + y * y + z * z);
        return length == 0 ? new double[] {0, 0, 0} : new double[] {x / length, y / length, z / length};
    }
}
