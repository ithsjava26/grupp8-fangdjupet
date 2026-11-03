package se.fangdjupet.sound;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SongTest {

    @Test
    void everyRoomSongExists() {
        for (String name : SoundListener.ROOM_SONGS) assertTrue(Song.SONGS.containsKey(name), name);
        for (String name : List.of("title", "battle", "stickyFight", "boss")) assertTrue(Song.SONGS.containsKey(name), name);
        for (String name : List.of("win", "final", "lose")) assertTrue(Song.JINGLES.containsKey(name), name);
    }

    @Test
    void fiveStepsUpIsAnOctaveInAPentatonicScale() {
        Song door = Song.SONGS.get("door");
        assertEquals(door.root(), door.pitch(0));
        assertEquals(door.root() + 12, door.pitch(5));
        assertEquals(door.root() - 12, door.pitch(-5));
        assertEquals(door.root() + 2, door.pitch(1));      // yo-skalan: 0 2 5 7 9
        assertEquals(door.root() - 3, door.pitch(-1));     // 9 − 12
    }

    @Test
    void a4Is440Hz() {
        assertEquals(440, Song.frequency(69), 1e-9);
        assertEquals(880, Song.frequency(81), 1e-9);
    }

    @Test
    void theLoopRepeatsWhenMelodyBassAndDrumsMeet() {
        Song door = Song.SONGS.get("door");   // melodi 32, bas 16, trummor 16
        assertEquals(32, door.loopLength());
    }

    @Test
    void underscoresHoldTheNote() {
        List<String> track = Song.parse("1 _ _ . 2");
        assertEquals(3, Song.length(track, 0));
        assertEquals(1, Song.length(track, 4));
    }

    @Test
    void everyNoteIsANumberAHoldOrARest() {
        for (Song song : List.copyOf(Song.SONGS.values())) {
            for (String note : song.melody()) assertTrue(note.matches("-?\\d+|_|\\."), note);
            for (String note : song.bass()) assertTrue(note.matches("-?\\d+|_|\\."), note);
            if (song.drums() != null) assertTrue(song.drums().matches("[kshp.]+"), song.drums());
        }
    }

    @Test
    void theStickySongGlidesAndSwings() {
        Song sticky = Song.SONGS.get("sticky");
        assertTrue(sticky.sticky());
        assertTrue(sticky.swing() > 0.5);
        assertTrue(sticky.bpm() < Song.SONGS.get("door").bpm(), "städskrubben är långsam och seg");
    }

    @Test
    void soundCanBeTurnedOff() {
        String before = System.getProperty("fangdjupet.ljud");
        System.setProperty("fangdjupet.ljud", "av");
        try {
            assertEquals(Optional.empty(), RetroSpeaker.open());
        } finally {
            if (before == null) System.clearProperty("fangdjupet.ljud"); else System.setProperty("fangdjupet.ljud", before);
        }
    }

    @Test
    void everySongAndSoundIsHeardWithoutClipping() {
        for (String name : Song.SONGS.keySet()) {
            RetroSpeaker speaker = RetroSpeaker.offline();
            speaker.music(name);
            assertAudible(name, speaker.render(2));
        }
        for (String name : List.of("blip", "shoot", "magic", "slash", "smart", "whoosh", "hurt", "gulp",
                "pickup", "slam", "alert", "laugh", "find", "club", "zap", "splat", "hiss", "fizz", "blah",
                "page", "slip", "chime", "boing", "squeak", "clang", "clank", "munch", "lowhp")) {
            RetroSpeaker speaker = RetroSpeaker.offline();
            speaker.sfx(name);
            assertAudible(name, speaker.render(0.6));
        }
    }

    @Test
    void silenceIsSilent() {
        RetroSpeaker speaker = RetroSpeaker.offline();
        speaker.music(null);
        for (short s : speaker.render(0.5)) assertEquals(0, s);
    }

    private static void assertAudible(String name, short[] samples) {
        int peak = 0;
        for (short s : samples) peak = Math.max(peak, Math.abs(s));
        assertTrue(peak > 1000, name + " hörs inte: " + peak);
        assertTrue(peak < 32700, name + " klipper: " + peak);
    }

    @Test
    void aVoiceStaysWithinRangeAndFadesOut() {
        RetroSpeaker.Voice voice = new RetroSpeaker.Voice("square", 440, 0, RetroSpeaker.RATE / 10, 0.5)
                .from(880, 0.05).vibrato(6, 20).lowpass(2000, 500, 4);
        Random random = new Random(1);
        double last = 0;
        for (long n = 0; n < voice.end; n++) {
            last = voice.sample(n, random);
            assertTrue(Math.abs(last) <= 2, "provet " + n + " = " + last);
        }
        assertEquals(0, last, 0.01);
    }
}
