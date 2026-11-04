package se.fangdjupet.sound;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

// En låt i japansk RPG-stil: en pentatonisk skala, en melodi, en bas och trummor.
// Varje token är ett åttondelssteg: en skalton (0 = grundtonen, 5 = oktaven över, -5 = oktaven under),
// "_" håller förra tonen och "." är paus. Trummor: k = bas, s = virvel, h = hihat, p = klibbigt "plopp".
// Samma låtar som webbversionen (fangdjupet-web, sound.js).
public record Song(int[] scale, int root, int bpm, String lead, double vol, double swing, boolean sticky,
                   List<String> melody, List<String> bass, String drums) {

    // Skalorna, i halvtoner från grundtonen
    static final int[] YO = {0, 2, 5, 7, 9};          // ljus, folkvisa
    static final int[] RYUKYU = {0, 4, 5, 7, 11};     // Okinawa: soligt och lite lojt
    static final int[] IWATO = {0, 1, 5, 6, 10};      // skev och obehaglig: städskrubben
    static final int[] INSEN = {0, 1, 5, 7, 10};      // mörk: Lektionssalen och Martin
    static final int[] HIRA = {0, 2, 3, 7, 8};        // hirajōshi: strid

    public static final Map<String, Song> SONGS = Map.of(
            "title", song(YO, 62, 84, "triangle", 0.9, 0, false,
                    "4 _ 3 2 3 _ _ . 2 _ 1 0 1 _ _ . 4 _ 5 6 7 _ 6 4 5 _ 4 3 2 _ _ .",
                    "-5 _ _ _ -5 _ _ _ -7 _ _ _ -7 _ _ _ -6 _ _ _ -6 _ _ _ -5 _ _ _ -3 _ _ _",
                    "....h.......h..."),
            "door", song(YO, 67, 132, "square", 1, 0, false,
                    "0 2 4 5 4 _ 2 _ 3 4 3 2 1 _ . . 0 2 4 5 7 _ 5 4 3 _ 2 _ 1 _ 0 _",
                    "-5 . -5 . -3 . -3 . -4 . -4 . -2 . -2 .",
                    "k.h.s.h.k.k.s.h."),
            "coffee", song(RYUKYU, 65, 96, "triangle", 1, 0.6, false,
                    "4 _ 3 _ 2 3 _ . 1 _ 2 _ 0 _ _ . 2 _ 3 4 5 _ 4 . 3 _ 2 1 2 _ _ .",
                    "-5 _ . -3 -5 _ . -3 -4 _ . -2 -4 _ . -1",
                    "k..h.s.hk..h.s.h"),
            // Städskrubben: klibbig – tonerna glider in i varandra som i såpa, svajar och är lite ostämda
            "sticky", song(IWATO, 60, 76, "square", 1, 0.64, true,
                    "0 _ 1 _ _ . 0 . 2 _ 1 _ 0 _ _ . 3 _ 2 _ 1 _ 2 . 1 _ _ _ 0 _ . .",
                    "-5 . -5 -4 . -5 . . -3 . -3 -4 . -5 . .",
                    "k..p..s.k.p..ps."),
            "stickyFight", song(IWATO, 60, 108, "square", 1, 0.6, true,
                    "0 1 0 _ 2 _ 1 . 3 2 1 _ 0 _ _ . 5 _ 3 2 1 _ 2 1 0 _ -1 _ 0 _ . .",
                    "-5 -5 . -4 -5 . -3 . -5 -5 . -4 -3 . -4 .",
                    "k.ps.kpsk.ps.kss"),
            "lecture", song(INSEN, 57, 100, "triangle", 1, 0, false,
                    "0 _ _ 1 _ _ 2 _ 3 _ 2 _ 1 _ _ . 0 _ _ 1 _ _ 2 _ 4 _ 3 _ 1 _ _ .",
                    "-5 _ _ _ -5 _ _ _ -4 _ _ _ -5 _ _ _",
                    "k.......s.......k.k.....s......."),
            "battle", song(HIRA, 64, 156, "square", 1, 0, false,
                    "5 4 3 2 3 _ 0 _ 1 2 3 _ 2 _ _ . 5 4 3 2 3 _ 5 _ 6 _ 5 4 3 _ _ .",
                    "-5 -5 -5 -5 -3 -3 -3 -3 -4 -4 -4 -4 -2 -2 -1 -1",
                    "k.hsk.hsk.hsk.ss"),
            "boss", song(INSEN, 62, 168, "sawtooth", 0.7, 0, false,
                    "0 1 0 -1 0 _ 3 _ 2 1 2 3 4 _ _ . 5 4 3 4 3 _ 2 _ 1 _ 0 _ -1 _ _ .",
                    "-5 . -5 -5 . -5 -4 . -5 . -5 -5 . -3 -4 .",
                    "k.hsk.hsk.hskkss"),
            // Martins fas 2 (Utterly explained): samma låt, men snabbare och en ton högre
            "bossFast", song(INSEN, 64, 228, "sawtooth", 0.75, 0, false,
                    "0 1 0 -1 0 _ 3 _ 2 1 2 3 4 _ _ . 5 4 3 4 3 _ 2 _ 1 _ 0 _ -1 _ _ .",
                    "-5 . -5 -5 . -5 -4 . -5 . -5 -5 . -3 -4 .",
                    "kkhskkhskkhskkss"));

    // Korta slingor som spelas en gång
    public static final Map<String, Song> JINGLES = Map.of(
            "win", song(YO, 67, 180, "square", 1, 0, false, "0 2 4 5 7 _ _ _", "-5 _ _ _ -3 _ _ _", null),
            "final", song(YO, 67, 150, "square", 1, 0, false,
                    "0 2 4 5 4 5 7 _ 7 9 10 _ _ _ _ _", "-5 _ -3 _ -4 _ -5 _ -5 _ -5 _ _ _ _ _", null),
            "lose", song(INSEN, 57, 90, "sawtooth", 1, 0, false, "5 4 3 2 1 _ 0 _ _ _ . .", "-5 _ _ _ -6 _ _ _ -10 _ _ _", null));

    private static Song song(int[] scale, int root, int bpm, String lead, double vol, double swing, boolean sticky,
                             String melody, String bass, String drums) {
        return new Song(scale, root, bpm, lead, vol, swing, sticky, parse(melody), parse(bass), drums);
    }

    static List<String> parse(String notes) {
        return Arrays.stream(notes.trim().split("\\s+")).toList();
    }

    // MIDI-tonen för en skalton: 5 steg upp i en pentatonisk skala är en oktav
    public int pitch(int degree) {
        int n = scale.length;
        return root + scale[Math.floorMod(degree, n)] + 12 * Math.floorDiv(degree, n);
    }

    public static double frequency(int midi) {
        return 440 * Math.pow(2, (midi - 69) / 12.0);
    }

    // Ett åttondelssteg i sekunder
    public double stepSeconds() {
        return 60.0 / bpm / 2;
    }

    // Efter så många steg börjar melodi, bas och trummor om samtidigt
    public int loopLength() {
        int length = lcm(melody.size(), bass.size());
        return drums == null ? length : lcm(length, drums.length());
    }

    // Hur många steg tonen i steget håller ("_" efter den förlänger)
    static int length(List<String> track, int step) {
        int n = 1;
        while (n < track.size() && track.get((step + n) % track.size()).equals("_")) n++;
        return n;
    }

    private static int lcm(int a, int b) {
        return a / gcd(a, b) * b;
    }

    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }
}
