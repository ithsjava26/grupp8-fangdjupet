package se.fangdjupet.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

// Spelar musiken och ljuden i datorns högtalare – syntat i Java Sound, inga ljudfiler.
// En egen tråd räknar fram ljudet i små bitar: sekvensern lägger ut låtens toner, och varje ton
// (fyrkantsvåg, sågtand, triangel, sinus eller brus) räknas fram prov för prov och blandas ihop.
// Av med -Dfangdjupet.ljud=av eller FANGDJUPET_LJUD=av (webbservern startar spelet så: webbläsaren har eget ljud).
public final class RetroSpeaker implements Speaker {

    static final int RATE = 44100;
    private static final int CHUNK = 1024;                 // ~23 ms åt gången
    private static final double MUSIC = 0.32, SFX = 0.8, MASTER = 0.55;

    private final SourceDataLine line;
    private final List<Voice> voices = new ArrayList<>();
    private final Random random = new Random();
    private long now;                                       // antal prov som spelats

    // sekvensern
    private Song song;
    private int step;
    private long nextStep;
    private double lastFreq;
    private Song once;                                      // jingel som spelas en gång
    private int onceStep;

    private RetroSpeaker(SourceDataLine line) {
        this.line = line;
    }

    // Öppnar ljudet, eller tomt om det är avstängt eller datorn inte har något ljud (t.ex. på en server)
    public static Optional<Speaker> open() {
        String setting = System.getProperty("fangdjupet.ljud", System.getenv().getOrDefault("FANGDJUPET_LJUD", "på"));
        if (setting.equalsIgnoreCase("av") || setting.equalsIgnoreCase("off")) return Optional.empty();
        try {
            AudioFormat format = new AudioFormat(RATE, 16, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format, CHUNK * 2 * 4);
            line.start();
            RetroSpeaker speaker = new RetroSpeaker(line);
            Thread.ofPlatform().daemon().name("fangdjupet-ljud").start(speaker::loop);
            return Optional.of(speaker);
        } catch (Exception | LinkageError e) {
            return Optional.empty();   // inget ljudkort: spelet fungerar som vanligt, utan ljud
        }
    }

    // ---------- Speaker ----------

    @Override
    public synchronized void music(String name) {
        Song next = name == null ? null : Song.SONGS.get(name);
        if (next == song) return;
        song = next;
        step = 0;
        lastFreq = 0;
        nextStep = Math.max(nextStep, now + CHUNK);
    }

    @Override
    public synchronized void jingle(String name, String then) {
        once = Song.JINGLES.get(name);
        onceStep = 0;
        song = then == null ? null : Song.SONGS.get(then);
        step = 0;
        nextStep = now + CHUNK;
    }

    @Override
    public synchronized void sfx(String name) {
        double t = (now + CHUNK) / (double) RATE;   // så snart som möjligt
        switch (name) {
            case "blip" -> tone("square", 1320, t, 0.05, 0.12).from(880, 0.03).sfx();
            case "shoot" -> {   // pilbågen: ett skränigt, krossat "PJOOO" med en ostämd tvilling och brus
                tone("sawtooth", 70, t, 0.26, 0.5).from(1400, 0.22).crushed();
                tone("square", 85, t, 0.22, 0.3).from(1530, 0.2).crushed();
                noise(t, 0.1, 0.45, 'b', 3500, 600, 2).crushed();
            }
            case "magic" -> {
                tone("triangle", 1800, t, 0.4, 0.22).from(300, 0.3).vibrato(18, 60).sfx();
                int[] up = {0, 4, 7, 12};
                for (int i = 0; i < up.length; i++)
                    tone("square", Song.frequency(84 + up[i]), t + 0.08 * i, 0.08, 0.06).sfx();
            }
            case "slash" -> {
                noise(t, 0.14, 0.55, 'b', 2500, 400, 1.5).sfx();
                tone("square", 50, t, 0.12, 0.3).from(160, 0.1).crushed();
            }
            case "smart" -> {
                int[] up = {0, 2, 4, 7};
                for (int i = 0; i < up.length; i++)
                    tone("square", Song.frequency(76 + up[i]), t + 0.05 * i, 0.06, 0.08).sfx();
            }
            case "whoosh" -> noise(t, 0.28, 0.3, 'b', 3000, 250, 3).sfx();
            case "hurt" -> {
                tone("square", 60, t, 0.22, 0.45).from(190, 0.18).crushed();
                noise(t, 0.12, 0.35, 'l', 1200, 0, 1).crushed();
            }
            case "gulp" -> {
                tone("sine", 620, t, 0.12, 0.35).from(260, 0.1).sfx();
                tone("sine", 620, t + 0.14, 0.12, 0.35).from(260, 0.1).sfx();
            }
            case "pickup" -> {
                Song door = Song.SONGS.get("door");
                for (int i = 0; i < 4; i++)
                    tone("square", Song.frequency(door.pitch(new int[]{0, 2, 4, 5}[i]) + 12), t + 0.06 * i, 0.07, 0.1).sfx();
            }
            case "slam" -> {
                tone("sine", 38, t, 0.5, 0.9).from(110, 0.12).sfx();
                noise(t, 0.35, 0.6, 'l', 700, 0, 1).crushed();
            }
            case "alert" -> {
                tone("square", Song.frequency(81), t, 0.08, 0.12).sfx();
                tone("square", Song.frequency(74), t + 0.09, 0.12, 0.12).sfx();
            }
            case "laugh" -> {   // StädHäxan: ha-ha-ha-ha som sjunker, i en sur sågtand
                for (int i = 0; i < 4; i++)
                    tone("sawtooth", 520 - i * 70, t + i * 0.13, 0.11, 0.18).from(620 - i * 70, 0.1).vibrato(30, 80).crushed();
            }
            case "find" -> {
                int[] up = {0, 4, 7, 12, 16};
                for (int i = 0; i < up.length; i++)
                    tone("triangle", Song.frequency(79 + up[i]), t + 0.07 * i, 0.12, 0.12).sfx();
            }
            // ---- fiendernas slag: varje fiende låter på sitt eget sätt ----
            case "club" -> {   // trollets klubba: en tung, dov duns
                tone("sine", 55, t, 0.25, 0.8).from(180, 0.1).sfx();
                noise(t, 0.2, 0.5, 'l', 400, 0, 1).crushed();
            }
            case "zap" -> {    // trollkarlen: elektriskt surr
                tone("sawtooth", 2000, t, 0.2, 0.22).from(200, 0.15).vibrato(40, 200).crushed();
                tone("square", 2600, t + 0.05, 0.12, 0.06).sfx();
            }
            case "splat" -> {  // tvättsvampen: blöt smäll
                noise(t, 0.25, 0.7, 'l', 1500, 200, 2).crushed();
                tone("sine", 90, t, 0.15, 0.5).from(300, 0.1).sfx();
            }
            case "hiss" -> noise(t, 0.45, 0.35, 'h', 4000, 0, 1).sfx();   // Svinto: sprejet väser
            case "fizz" -> {   // kemikalierna fräser och bubblar
                noise(t, 0.5, 0.3, 'b', 5000, 1500, 6).sfx();
                for (int i = 0; i < 5; i++) tone("sine", 900 + i * 150, t + i * 0.07, 0.05, 0.12).from(400, 0.03).sfx();
            }
            case "blah" -> {   // Martin förklarar: bla-bla-bla-bla
                int[] up = {0, 3, -2, 1, 5, -1};
                for (int i = 0; i < up.length; i++)
                    tone("square", Song.frequency(57 + up[i]), t + i * 0.08, 0.07, 0.12).vibrato(9, 40).lowpass(1200, 0, 2).sfx();
            }
            // ---- hjältens nya saker ----
            case "page" -> {   // en sida i Java-boken vänds
                noise(t, 0.12, 0.3, 'h', 3000, 0, 1).sfx();
                noise(t + 0.1, 0.08, 0.25, 'b', 2000, 0, 1).sfx();
            }
            case "slip" -> {   // slinter på sidan: wiiiuuu – duns
                tone("square", 200, t, 0.55, 0.2).from(900, 0.5).vibrato(12, 50).crushed();
                tone("sine", 50, t + 0.5, 0.2, 0.6).from(150, 0.1).sfx();
            }
            case "chime" -> {  // Dela skärm: tid att andas
                int[] up = {0, 4, 7, 12, 16};
                for (int i = 0; i < up.length; i++)
                    tone("triangle", Song.frequency(72 + up[i]), t + 0.06 * i, 0.3, 0.12).sfx();
            }
            case "boing" -> tone("sine", 300, t, 0.4, 0.4).from(120, 0.08).vibrato(14, 300).sfx();   // stressbollen kastas
            case "squeak" -> {  // stressbollen kläms: iiik-iiik
                tone("square", 1400, t, 0.1, 0.12).from(900, 0.06).sfx();
                tone("square", 1500, t + 0.12, 0.1, 0.1).from(950, 0.06).sfx();
            }
            case "clang" -> {   // ett vapen i handen
                tone("square", 1250, t, 0.4, 0.12).sfx();
                tone("square", 1873, t, 0.35, 0.08).detune(15).sfx();
                noise(t, 0.05, 0.3, 'h', 6000, 0, 1).sfx();
            }
            case "clank" -> {   // rustningen på
                noise(t, 0.15, 0.5, 'b', 900, 0, 4).crushed();
                tone("triangle", 330, t, 0.2, 0.2).sfx();
            }
            case "munch" -> {   // kanelbullen: mums-mums-mums
                for (int i = 0; i < 3; i++) noise(t + i * 0.11, 0.07, 0.45, 'l', 1500, 0, 1).crushed();
            }
            case "lowhp" -> {   // varning: lite hp kvar
                tone("square", 988, t, 0.09, 0.1).sfx();
                tone("square", 988, t + 0.16, 0.09, 0.1).sfx();
            }
            // ---- utanför striden: gå, leta, spara, prata ----
            case "steps" -> {   // fotsteg till nästa rum: klapp-klapp-klapp
                for (int i = 0; i < 4; i++) {
                    noise(t + i * 0.16, 0.05, 0.4, 'l', 700, 0, 2).crushed();
                    tone("sine", 70, t + i * 0.16, 0.06, 0.3).from(140, 0.03).sfx();
                }
            }
            case "rummage" -> {   // letar: prassel i lådor och papper
                for (int i = 0; i < 6; i++) noise(t + i * 0.07, 0.05, 0.25, 'b', 1800 + (i % 3) * 900, 0, 3).sfx();
            }
            case "nope" -> {   // hittar inget: buu-buu
                tone("square", 220, t, 0.16, 0.14).lowpass(1500, 0, 1).sfx();
                tone("square", 165, t + 0.18, 0.3, 0.14).from(185, 0.2).lowpass(1200, 0, 1).sfx();
            }
            case "save" -> {   // sparat: diskettens bip-bip-blipp
                tone("square", 1760, t, 0.05, 0.1).sfx();
                tone("square", 1760, t + 0.08, 0.05, 0.1).sfx();
                tone("triangle", Song.frequency(84), t + 0.18, 0.25, 0.14).sfx();
                noise(t + 0.02, 0.15, 0.08, 'h', 5000, 0, 1).sfx();
            }
            case "buzz" -> tone("square", 110, t, 0.22, 0.16).detune(30).crushed();   // fel val: brrrt
            case "sparkle" -> {   // Frida dyker upp: glitter som stiger
                for (int i = 0; i < 7; i++)
                    tone("triangle", Song.frequency(84 + (i * 5) % 12 + (i / 3) * 12), t + i * 0.05, 0.12, 0.08).sfx();
            }
            case "fanfare" -> {   // äventyret börjar: ta-ta-ta-taaa
                int[] up = {0, 0, 0, 5};
                double[] len = {0.1, 0.1, 0.1, 0.45};
                double at = t;
                for (int i = 0; i < up.length; i++) {
                    tone("square", Song.frequency(67 + up[i]), at, len[i], 0.12).sfx();
                    tone("triangle", Song.frequency(55 + up[i]), at, len[i], 0.14).sfx();
                    at += len[i] + 0.03;
                }
            }
            case "swap" -> {   // byter attack: klick-klack
                tone("square", 900, t, 0.03, 0.1).sfx();
                tone("square", 1350, t + 0.07, 0.03, 0.1).sfx();
            }
            case "locked" -> {   // låst: handtaget skramlar
                for (int i = 0; i < 3; i++) noise(t + i * 0.09, 0.06, 0.35, 'b', 1200, 0, 6).crushed();
                tone("sine", 90, t + 0.3, 0.12, 0.3).sfx();
            }
            case "talk" -> {   // någon pratar: små bubbel-ljud
                int[] up = {0, 4, 2, 5};
                for (int i = 0; i < up.length; i++)
                    tone("square", Song.frequency(69 + up[i]), t + i * 0.07, 0.05, 0.08).lowpass(2500, 0, 1).sfx();
            }
            case "bag" -> {   // utrustningen: blixtlåset dras upp
                noise(t, 0.25, 0.25, 'b', 1500, 5000, 4).sfx();
                tone("square", 400, t, 0.25, 0.04).from(200, 0.25).sfx();
            }
            case "drop" -> tone("sine", 80, t, 0.18, 0.4).from(260, 0.12).sfx();   // lämnar kvar: duns
            case "bye" -> {   // tack för att du spelade: tre toner nedåt
                int[] down = {12, 7, 0};
                for (int i = 0; i < down.length; i++)
                    tone("triangle", Song.frequency(72 + down[i]), t + i * 0.16, 0.2, 0.14).sfx();
            }
            default -> { }
        }
    }

    // För testerna: en högtalare utan ljudkort, som bara räknar fram ljudet
    static RetroSpeaker offline() {
        return new RetroSpeaker(null);
    }

    // Räknar fram så många sekunder ljud (16 bitar, mono) utan att spela dem
    synchronized short[] render(double seconds) {
        int chunks = (int) Math.ceil(seconds * RATE / CHUNK);
        short[] out = new short[chunks * CHUNK];
        byte[] buffer = new byte[CHUNK * 2];
        for (int c = 0; c < chunks; c++) {
            sequence();
            render(buffer);
            for (int i = 0; i < CHUNK; i++)
                out[c * CHUNK + i] = (short) ((buffer[2 * i] & 0xff) | (buffer[2 * i + 1] << 8));
        }
        return out;
    }

    // ---------- tråden ----------

    private void loop() {
        byte[] buffer = new byte[CHUNK * 2];
        while (true) {
            synchronized (this) {
                sequence();
                render(buffer);
            }
            line.write(buffer, 0, buffer.length);   // väntar tills det finns plats: håller takten
        }
    }

    // Lägger ut låtens toner för nästa bit
    private void sequence() {
        long end = now + CHUNK;
        while (nextStep < end) {
            if (once != null) {
                play(once, onceStep, nextStep);
                nextStep += samples(once.stepSeconds());
                if (++onceStep >= once.melody().size()) { once = null; step = 0; lastFreq = 0; }
            } else if (song != null) {
                play(song, step, nextStep);
                nextStep += samples(song.stepSeconds());
                step = (step + 1) % song.loopLength();
            } else {
                nextStep = end;
            }
        }
    }

    private void play(Song s, int i, long at) {
        double sd = s.stepSeconds();
        double t = at / (double) RATE + (s.swing() > 0 && i % 2 == 1 ? sd * (s.swing() - 0.5) * 2 : 0);

        String m = s.melody().get(i % s.melody().size());
        if (!m.equals("_") && !m.equals(".")) {
            double f = Song.frequency(s.pitch(Integer.parseInt(m)));
            double dur = Song.length(s.melody(), i % s.melody().size()) * sd * 0.92, v = 0.16 * s.vol();
            if (s.sticky()) {
                // glider från förra tonen, svajar och har en ostämd tvilling: som att trampa i såpa
                double from = lastFreq > 0 ? lastFreq : f * 0.94;
                tone("square", f, t, dur, v * 0.8).from(from, sd * 0.9).vibrato(5.5, 28).lowpass(2400, 700, 6);
                tone("square", f, t, dur, v * 0.45).from(from, sd * 1.2).detune(22).lowpass(1800, 0, 1);
            } else {
                Voice lead = tone(s.lead(), f, t, dur, v);
                if (dur > sd * 2) lead.vibrato(6, 12);
                if (s.lead().equals("square")) tone("square", f * 2, t + sd * 0.5, sd * 0.5, v * 0.18);   // eko
            }
            lastFreq = f;
        }
        String b = s.bass().get(i % s.bass().size());
        if (!b.equals("_") && !b.equals(".")) {
            double f = Song.frequency(s.pitch(Integer.parseInt(b)));
            double dur = Song.length(s.bass(), i % s.bass().size()) * sd * 0.9;
            if (s.sticky()) tone("sawtooth", f, t, dur, 0.2).lowpass(900, 160, 9);   // squelch
            else tone("triangle", f, t, dur, 0.28);
        }
        if (s.drums() != null) {
            switch (s.drums().charAt(i % s.drums().length())) {
                case 'k' -> tone("sine", 45, t, 0.18, 0.9).from(140, 0.08);
                case 's' -> { noise(t, 0.12, 0.35, 'b', 1800, 0, 0.7); tone("triangle", 180, t, 0.08, 0.25); }
                case 'h' -> noise(t, 0.035, 0.18, 'h', 7000, 0, 1);
                case 'p' -> tone("sine", 160, t, 0.16, 0.5).from(700, 0.1);   // "plopp": mopp i hinken
                default -> { }
            }
        }
    }

    private void render(byte[] buffer) {
        for (int i = 0; i < CHUNK; i++) {
            long n = now + i;
            double music = 0, sfx = 0;
            for (Voice voice : voices) {
                if (n < voice.start || n >= voice.end) continue;
                double x = voice.sample(n, random);
                if (voice.music) music += x; else sfx += x;
            }
            double mix = Math.tanh((music * MUSIC + sfx * SFX) * MASTER * 1.4);   // mjuk begränsning
            short s = (short) (mix * 32000);
            buffer[2 * i] = (byte) s;
            buffer[2 * i + 1] = (byte) (s >> 8);
        }
        now += CHUNK;
        voices.removeIf(voice -> voice.end <= now);
    }

    private static long samples(double seconds) {
        return Math.round(seconds * RATE);
    }

    private Voice tone(String wave, double freq, double at, double dur, double vol) {
        Voice voice = new Voice(wave, freq, samples(at), samples(dur), vol);
        voices.add(voice);
        return voice;
    }

    private Voice noise(double at, double dur, double vol, char filter, double cutoff, double cutoffTo, double q) {
        return tone("noise", 0, at, dur, vol).filter(filter, cutoff, cutoffTo, q);
    }

    // En ton eller ett brus: våg, glidning, vibrato, filter och ett envelopp som klingar av
    static final class Voice {
        final String wave;
        final double freq, vol;
        final long start, end, length;
        double from, glide, vibratoHz, depth, detune;
        char filter = 'n';
        double cutoff, cutoffTo, q = 1;
        boolean music = true, crushed;
        private double phase, low, band;

        Voice(String wave, double freq, long start, long length, double vol) {
            this.wave = wave;
            this.freq = freq;
            this.start = start;
            this.length = Math.max(1, length);
            this.end = start + this.length + RATE / 50;
            this.vol = vol;
        }

        Voice from(double f, double seconds) { from = f; glide = seconds * RATE; return this; }
        Voice vibrato(double hz, double cents) { vibratoHz = hz; depth = cents; return this; }
        Voice detune(double cents) { detune = cents; return this; }
        Voice lowpass(double f, double to, double q) { return filter('l', f, to, q); }
        Voice filter(char type, double f, double to, double q) { filter = type; cutoff = f; cutoffTo = to; this.q = q; return this; }
        Voice sfx() { music = false; return this; }
        Voice crushed() { crushed = true; music = false; return this; }   // grov 8-bitarstrappa: medvetet fult

        double sample(long n, Random random) {
            double x = n - start;
            double raw;
            if (wave.equals("noise")) {
                raw = random.nextDouble() * 2 - 1;
            } else {
                double f = from > 0 && glide > 0 ? from * Math.pow(freq / from, Math.min(1, x / glide)) : freq;
                double cents = detune + (vibratoHz > 0 ? depth * Math.sin(2 * Math.PI * vibratoHz * x / RATE) : 0);
                if (cents != 0) f *= Math.pow(2, cents / 1200);
                phase = (phase + f / RATE) % 1;
                raw = switch (wave) {
                    case "square" -> phase < 0.5 ? 1 : -1;
                    case "sawtooth" -> 2 * phase - 1;
                    case "triangle" -> 4 * Math.abs(phase - 0.5) - 1;
                    default -> Math.sin(2 * Math.PI * phase);
                };
            }
            if (filter != 'n') raw = filtered(raw, x);
            double attack = 0.005 * RATE;
            double env = x < attack ? vol * x / attack
                    : x >= length ? 0 : vol * Math.pow(0.0001 / vol, (x - attack) / Math.max(1, length - attack));
            double out = raw * env;
            return crushed ? Math.round(out * 4) / 4.0 : out;
        }

        // Tillståndsvariabelfilter (Chamberlin): lågpass, bandpass eller högpass, med resonans
        private double filtered(double in, double x) {
            double c = cutoffTo > 0 ? cutoff * Math.pow(cutoffTo / cutoff, Math.min(1, x / length)) : cutoff;
            double k = 2 * Math.sin(Math.PI * Math.min(c, RATE / 6.0) / RATE);
            double damp = Math.min(2, 1 / Math.max(0.5, q));
            low += k * band;
            double high = in - low - damp * band;
            band += k * high;
            return switch (filter) {
                case 'l' -> low;
                case 'h' -> high;
                default -> band;
            };
        }
    }
}
