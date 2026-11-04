package se.fangdjupet.sound;

// Det som spelar ljudet. RetroSpeaker syntar det i högtalarna; testerna använder en som bara antecknar.
public interface Speaker {
    // Byter låt, t.ex. "sticky" i städskrubben. null = tyst.
    void music(String song);

    // En kort slinga en gång (t.ex. "win"), sedan låten then (null = tyst efteråt)
    void jingle(String name, String then);

    // En ljudeffekt, t.ex. "shoot" när pilbågen skjuter
    void sfx(String name);
}
