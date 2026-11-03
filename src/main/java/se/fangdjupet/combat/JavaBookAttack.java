package se.fangdjupet.combat;

import java.util.List;

// Java-boken som hjälten hittar på katedern i Lektionssalen. Varje gång hjälten citerar ur den
// bläddrar hen vidare: kapitel 18 (Polymorphism), sedan kapitel 20 (Interface), sedan från början igen.
// Den bästa attacken mot Martin: 14 i skada, rakt igenom försvaret som all kunskap, och den kostar ingenting.
// Men man kan slinta på sidan: 30 % av citaten missar (vanliga slag missar 10 %).
// Varje hjälte har sin egen bok (Hero.pickUpJavaBook).
public class JavaBookAttack implements AttackStrategy {
    public static final int POWER = 14;
    public static final int SLIP_PERCENT = 30;
    public static final List<String> CHAPTERS = List.of("kapitel 18: Polymorphism", "kapitel 20: Interface");

    private int next;          // kapitlet hjälten läser nästa gång
    private String lastRead;   // kapitlet hen just läste – syns i loggen

    // Kapitlet som står på tur, t.ex. "kapitel 20: Interface"
    public String nextChapter() {
        return CHAPTERS.get(next);
    }

    // Läser kapitlet och bläddrar vidare till nästa
    @Override
    public int calculateDamage(Character attacker, Character target) {
        lastRead = nextChapter();
        next = (next + 1) % CHAPTERS.size();
        return POWER;
    }

    // I attackmenyn: kapitlet som står på tur
    @Override
    public String name() {
        return "Citera från Java-boken, " + nextChapter();
    }

    // I loggen: "Robert citerar kapitel 18: Polymorphism för Lektor Martin skada: 14"
    @Override
    public String verb() {
        return "citerar " + (lastRead == null ? nextChapter() : lastRead) + " för";
    }

    // Battle frågar hur ofta citatet missar
    @Override
    public int missPercent() {
        return SLIP_PERCENT;
    }

    // I loggen vid miss: "Robert slinter på sidan och missar Lektor Martin!"
    @Override
    public String missVerb() {
        return "slinter på sidan och missar";
    }
}
